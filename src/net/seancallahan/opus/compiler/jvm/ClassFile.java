package net.seancallahan.opus.compiler.jvm;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.jvm.attributes.Attribute;
import net.seancallahan.opus.compiler.jvm.attributes.Code;
import net.seancallahan.opus.lang.Class;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Method;
import net.seancallahan.opus.lang.Variable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassFile
{
    private static final long magic = 0xCAFEBABE;
    private static final int minorVersion = 0;
    private static final int majorVersion = 55;

    private Variable[] fields;
    private Function[] functions;

    private final ConstantPool constantPool = new ConstantPool();

    private final ByteArrayOutputStream internalBuffer = new ByteArrayOutputStream();
    private final DataOutputStream buffer = new DataOutputStream(internalBuffer);

    private final ConstantDeclaration initializer = new ConstantDeclaration
    (
        constantPool.add(new Constant<>(Constant.Kind.UTF8, "<init>")),
        constantPool.add(new Constant<>(Constant.Kind.UTF8, "()V"))
    );

    private final int accessFlags;
    private final short thisClass;
    private final short superClass;

    private final Map<Declaration, ConstantDeclaration> declarations = new HashMap<>();

    public ClassFile(String name, String pkg, Class clazz)
    {
        this(name, pkg, clazz.isPublic() ? AccessFlag.PUBLIC : AccessFlag.PRIVATE);

        this.functions = new Function[clazz.getMethods().size()];
        for (int i = 0; i < functions.length; i++)
        {
            Method method = clazz.getMethods().get(i);
            addDeclaration(method);
            this.functions[i] = method;
        }

        this.fields = new Variable[clazz.getFields().size()];
        for (int i = 0; i < fields.length; i++)
        {
            Variable field = clazz.getFields().get(i);
            addDeclaration(field);
            this.fields[i] = field;
        }
    }

    public ClassFile(String name, String pkg, List<Function> functions)
    {
        this(name, pkg, AccessFlag.PUBLIC); // static classes are always public

        this.functions = new Function[functions.size()];
        for (int i = 0; i < this.functions.length; i++)
        {
            Function function = functions.get(i);
            addDeclaration(function);
            this.functions[i] = function;
        }

        this.fields = new Variable[0];
    }

    private ClassFile(String name, String pkg, short accessFlag)
    {
        short nameIndex = constantPool.add(new Constant<>(Constant.Kind.UTF8, pkg + "/" + name));
        this.thisClass = constantPool.add(new Constant<>(Constant.Kind.CLASS, nameIndex));

        // TODO: write opus base Object class
        short superIndex = constantPool.add(new Constant<>(Constant.Kind.UTF8, "java/lang/Object"));
        this.superClass = constantPool.add(new Constant<>(Constant.Kind.CLASS, superIndex));

        this.accessFlags = accessFlag;
    }

    public void write(DataOutputStream out) throws IOException, CompilerException
    {
        out.writeInt((int)magic);
        out.writeShort((short)minorVersion);
        out.writeShort((short)majorVersion);

        buffer.writeShort((short)accessFlags);
        buffer.writeShort(thisClass);
        buffer.writeShort(superClass);

        buffer.writeShort(0); // interfaces_count

        buffer.writeShort(fields.length);
        for (Variable field : fields)
        {
            writeField(buffer, field, declarations.get(field));
        }

        buffer.writeShort(functions.length + 1); // +1 for the constructor

        // JVM requires a constructor, so make an empty one.
        writeFunction(buffer, null, initializer);

        for (Function function : functions)
        {
            writeFunction(buffer, function, declarations.get(function));
        }

        buffer.writeShort(0); // attributes_count

        constantPool.write(out);

        internalBuffer.writeTo(out);
    }

    private void addDeclaration(Declaration declaration)
    {
        short name = constantPool.add(new Constant<>(Constant.Kind.UTF8, declaration.getName().getValue()));
        short descriptor = constantPool.add(new Constant<>(Constant.Kind.UTF8, Descriptor.from(declaration).toString()));

        declarations.put(declaration, new ConstantDeclaration(name, descriptor));
    }

    private void writeField(DataOutputStream out, Variable field, ConstantDeclaration cd) throws IOException
    {
        out.writeShort(AccessFlag.PUBLIC);
        out.writeShort(cd.getNameIndex());
        out.writeShort(cd.getDescriptorIndex());

        out.writeShort(0); // attributes
    }

    private void writeFunction(DataOutputStream out, Function function, ConstantDeclaration cd) throws IOException, CompilerException
    {
        short accessFlags = AccessFlag.PRIVATE;
        if (function == null || function.isPublic())
        {
            accessFlags = AccessFlag.PUBLIC;
        }

        out.writeShort(accessFlags);
        out.writeShort(cd.getNameIndex());
        out.writeShort(cd.getDescriptorIndex());

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Code(constantPool, function));

        out.writeShort(attributes.size()); // attributes_count
        for (Attribute attribute : attributes)
        {
            attribute.write(out);
        }
    }

    private class ConstantDeclaration
    {
        private final short nameIndex;
        private final short descriptorIndex;

        private ConstantDeclaration(short nameIndex, short descriptorIndex)
        {
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }

        public short getNameIndex()
        {
            return nameIndex;
        }

        public short getDescriptorIndex()
        {
            return descriptorIndex;
        }
    }
}
