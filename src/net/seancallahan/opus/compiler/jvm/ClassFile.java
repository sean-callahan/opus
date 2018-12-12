package net.seancallahan.opus.compiler.jvm;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.Package;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.TokenType;
import net.seancallahan.opus.compiler.jvm.attributes.Attribute;
import net.seancallahan.opus.compiler.jvm.attributes.Code;
import net.seancallahan.opus.compiler.jvm.attributes.SourceFile;
import net.seancallahan.opus.lang.Class;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Field;
import net.seancallahan.opus.lang.Method;
import net.seancallahan.opus.lang.Variable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
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

    private Class theClass;

    private Variable[] fields;
    private Method[] methods;

    private final ConstantPool constantPool = new ConstantPool();

    private final ByteArrayOutputStream internalBuffer = new ByteArrayOutputStream();
    private final DataOutputStream buffer = new DataOutputStream(internalBuffer);

    private final int accessFlags;
    private final short thisClass;
    private final short superClass;

    private final Constant.MethodRef initializer;
    private final Map<String, Constant.Reference> references = new HashMap<>();
    private final List<Attribute> attributes = new ArrayList<>();

    public ClassFile(File file, Class clazz)
    {
        this(file, clazz, clazz.isPublic() ? AccessFlag.PUBLIC : AccessFlag.PRIVATE);

        this.methods = new Method[clazz.getMethods().size()];
        for (int i = 0; i < methods.length; i++)
        {
            Method method = clazz.getMethods().get(i);
            addDeclaration(method);
            this.methods[i] = method;
        }

        this.fields = new Variable[clazz.getFields().size()];
        for (int i = 0; i < fields.length; i++)
        {
            Variable field = clazz.getFields().get(i);
            addDeclaration(field);
            this.fields[i] = field;
        }
    }

    public ClassFile(File file, String name, String pkg, List<Function> functions)
    {
        this(file, new Class(new Package(pkg), new Token(TokenType.NAME, name)) , AccessFlag.PUBLIC); // static classes are always public

        this.methods = new Method[functions.size()];
        for (int i = 0; i < this.methods.length; i++)
        {
            Function function = functions.get(i);
            Method method = new Method(function, getTheClass(), true);
            addDeclaration(method);
            this.methods[i] = method;
        }

        this.fields = new Variable[0];
    }

    private ClassFile(File file, Class clazz, short accessFlag)
    {
        this.theClass = clazz;

        this.thisClass = constantPool.add(new Constant.Class(constantPool, clazz));

        // TODO: write opus base Object class
        Package basePkg = new Package("java/lang");
        Class base = new Class(basePkg, new Token(TokenType.NAME, "Object"));

        Function init = new Function(new Token(TokenType.NAME, "<init>"), null);

        this.initializer = new Constant.MethodRef(constantPool, new Method(init, base, false));

        this.superClass = constantPool.add(new Constant.Class(constantPool, base));

        this.accessFlags = accessFlag;

        this.attributes.add(new SourceFile(constantPool, file));
    }

    public ConstantPool getConstantPool()
    {
        return constantPool;
    }

    public Class getTheClass()
    {
        return theClass;
    }

    public Map<String, Constant.Reference> getReferences()
    {
        return references;
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
            writeField(buffer, field, references.get(field.getName().getValue()));
        }

        buffer.writeShort(methods.length + 1); // +1 for the constructor

        // JVM requires a constructor, so make an empty one.
        writeFunction(buffer, (Method) initializer.getValue(), initializer);

        for (Method method : methods)
        {
            writeFunction(buffer, method, references.get(method.getName().getValue()));
        }

        buffer.writeShort(attributes.size());
        for (Attribute attribute : attributes)
        {
            attribute.write(buffer);
        }

        constantPool.write(out);

        internalBuffer.writeTo(out);
    }

    private void addDeclaration(Declaration declaration)
    {
        Constant.Reference ref;
        if (declaration instanceof Method)
        {
            ref = new Constant.MethodRef(constantPool, (Method) declaration);
        }
        else if (declaration instanceof Field)
        {
            ref = new Constant.FieldRef(constantPool, (Field) declaration);
        }
        else
        {
            throw new UnsupportedOperationException();
        }

        references.put(declaration.getName().getValue(), ref);
        constantPool.add(ref);
    }

    private void writeField(DataOutputStream out, Variable field, Constant.Reference reference) throws IOException
    {
        out.writeShort(AccessFlag.PUBLIC);
        out.writeShort(reference.getNameAndType().getNameIndex());
        out.writeShort(reference.getNameAndType().getDescriptorIndex());

        out.writeShort(0); // attributes
    }

    private void writeFunction(DataOutputStream out, Method method, Constant.Reference reference) throws IOException, CompilerException
    {
        short accessFlags = AccessFlag.PRIVATE;
        if (method == null || method.isPublic())
        {
            accessFlags = AccessFlag.PUBLIC;
        }
        if (method != null && method.isStatic())
        {
            accessFlags |= AccessFlag.STATIC;
        }

        out.writeShort(accessFlags);
        out.writeShort(reference.getNameAndType().getNameIndex());
        out.writeShort(reference.getNameAndType().getDescriptorIndex());

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Code(this, method));

        out.writeShort(attributes.size()); // attributes_count
        for (Attribute attribute : attributes)
        {
            attribute.write(out);
        }
    }
}
