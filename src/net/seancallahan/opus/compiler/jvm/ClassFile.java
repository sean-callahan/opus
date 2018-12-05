package net.seancallahan.opus.compiler.jvm;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.SourceFile;
import net.seancallahan.opus.lang.Class;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Method;
import net.seancallahan.opus.lang.Variable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ClassFile
{
    private static final long magic = 0xCAFEBABE;
    private static final int minorVersion = 0;
    private static final int majorVersion = 55;

    private final Class clazz;

    private final ConstantPool constantPool = new ConstantPool();

    private final ConstantDeclaration initializer = new ConstantDeclaration
    (
        constantPool.add(new Constant<>(Constant.Kind.UTF8, "<init>")),
        constantPool.add(new Constant<>(Constant.Kind.UTF8, "()V"))
    );

    private final int accessFlags;
    private final short thisClass;
    private final short superClass;
    private final Map<String, Short> attributeIndices = new HashMap<>();

    private final Map<Declaration, ConstantDeclaration> declarations = new HashMap<>();

    public ClassFile(Class clazz, String name)
    {
        this.clazz = clazz;

        accessFlags = clazz.isPublic() ? AccessFlag.PUBLIC : AccessFlag.PRIVATE;

        short nameIndex = constantPool.add(new Constant<>(Constant.Kind.UTF8, "$test/" + name));
        this.thisClass = constantPool.add(new Constant<>(Constant.Kind.CLASS, nameIndex));

        // TODO: write opus base Object class
        short superIndex = constantPool.add(new Constant<>(Constant.Kind.UTF8, "java/lang/Object"));
        this.superClass = constantPool.add(new Constant<>(Constant.Kind.CLASS, superIndex));

        attributeIndices.put("Code", constantPool.add(new Constant<>(Constant.Kind.UTF8, "Code")));

        for (Method method : clazz.getMethods())
        {
            addDeclaration(method);
        }

        for (Variable field : clazz.getFields())
        {
            addDeclaration(field);
        }
    }

    public void write(DataOutputStream out) throws IOException, CompilerException
    {
        out.writeInt((int)magic);
        out.writeShort((short)minorVersion);
        out.writeShort((short)majorVersion);

        constantPool.write(out);

        out.writeShort((short)accessFlags);
        out.writeShort(thisClass);
        out.writeShort(superClass);

        out.writeShort(0); // interfaces_count

        out.writeShort(clazz.getFields().size());
        for (Variable field : clazz.getFields())
        {
            writeField(out, field, declarations.get(field));
        }

        out.writeShort(clazz.getMethods().size() + 1);

        // JVM requires a constructor, so make an empty one.
        writeMethod(out, null, initializer);

        for (Method method : clazz.getMethods())
        {
            writeMethod(out, method, declarations.get(method));
        }

        out.writeShort(0); // attributes_count
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

    private void writeMethod(DataOutputStream out, Method method, ConstantDeclaration cd) throws IOException, CompilerException
    {
        short accessFlags = AccessFlag.PRIVATE;
        if (method == null || method.isPublic())
        {
            accessFlags = AccessFlag.PUBLIC;
        }

        out.writeShort(accessFlags);
        out.writeShort(cd.getNameIndex());
        out.writeShort(cd.getDescriptorIndex());

        out.writeShort(1); // attributes

        byte[] info = null;
        if (method != null)
        {
            info = createCodeAttribute(method, constantPool);
        }

        writeAttribute(out, "Code", info);
    }

    private void writeAttribute(DataOutputStream out, String name, byte[] info) throws IOException
    {
        assert attributeIndices.containsKey(name);

        short nameIndex = attributeIndices.get(name);
        out.writeShort(nameIndex);

        if (info == null)
        {
            out.writeInt(0); // length
            return;
        }

        out.writeInt(info.length);
        out.write(info);
    }

    private static byte[] createCodeAttribute(Method method, ConstantPool pool) throws CompilerException
    {
        CodeGenerator gen = new CodeGenerator(method, pool);
        byte[] code = gen.getCode();

        ByteBuffer buffer = ByteBuffer.allocate(code.length + 12);

        buffer.putShort(gen.getMaxStack());
        buffer.putShort((short)(gen.getMaxLocalVars() + 1));

        buffer.putInt(code.length);
        buffer.put(code);

        buffer.putShort((short)0); // exceptions
        buffer.putShort((short)0); // attributes

        return buffer.array();
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
