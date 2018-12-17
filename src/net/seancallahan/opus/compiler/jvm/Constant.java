package net.seancallahan.opus.compiler.jvm;

import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Field;
import net.seancallahan.opus.lang.Member;
import net.seancallahan.opus.lang.Method;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Constant<T>
{
    protected final ConstantPool pool;
    private final Kind kind;
    private final T value;

    public Constant(ConstantPool pool, Kind kind, T value)
    {
        this.pool = pool;
        this.kind = kind;
        this.value = value;
    }

    public void write(DataOutputStream out) throws IOException
    {
        out.writeByte((byte)kind.getTag());
    }

    public Kind getKind()
    {
        return kind;
    }

    public T getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Constant) || ((Constant) obj).getKind() != getKind())
        {
            return false;
        }
        return getValue().equals(((Constant) obj).getValue());
    }

    public enum Kind
    {
        UTF8(1),
        INTEGER(3),
        FLOAT(4),
        LONG(5),
        DOUBLE(6),
        CLASS(7),
        STRING(8),
        FIELD_REF(9),
        METHOD_REF(10),
        // INTERFACE_METHOD_REF(11),
        NAME_AND_TYPE(12),
        METHOD_HANDLE(15),
        METHOD_TYPE(16),
        INVOKE_DYNAMIC(18),
        MODULE(19),
        PACKAGE(20);

        private final char tag;

        Kind(int tag)
        {
            this.tag = (char)tag;
        }

        public char getTag()
        {
            return tag;
        }
    }

    public static class UTF8 extends Constant<java.lang.String>
    {
        public UTF8(ConstantPool pool, java.lang.String value)
        {
            super(pool, Kind.UTF8, value);
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeUTF(getValue());
        }
    }

    public static class Integer extends Constant<java.lang.Integer>
    {
        public Integer(ConstantPool pool, java.lang.Integer value)
        {
            super(pool, Kind.INTEGER, value);
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeInt(getValue());
        }
    }

    public static class Float extends Constant<java.lang.Float>
    {
        public Float(ConstantPool pool, java.lang.Float value)
        {
            super(pool, Kind.FLOAT, value);
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeFloat(getValue());
        }
    }

    public static class Long extends Constant<java.lang.Long>
    {
        public Long(ConstantPool pool, java.lang.Long value)
        {
            super(pool, Kind.LONG, value);
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeLong(getValue());
        }
    }

    public static class Double extends Constant<java.lang.Double>
    {
        public Double(ConstantPool pool, java.lang.Double value)
        {
            super(pool, Kind.DOUBLE, value);
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeDouble(getValue());
        }
    }

    public static class Class extends Constant<net.seancallahan.opus.lang.Class>
    {
        private final short nameIndex;

        public Class(ConstantPool pool, net.seancallahan.opus.lang.Class value)
        {
            super(pool, Kind.CLASS, value);

            java.lang.String name = value.getPackage().getName() + "/" + value.getName().getValue();

            this.nameIndex = pool.add(new UTF8(pool, name));
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(nameIndex);
        }
    }

    public static class String extends Constant<java.lang.String>
    {
        private final short stringIndex;

        public String(ConstantPool pool, java.lang.String value)
        {
            super(pool, Kind.STRING, value);

            this.stringIndex = pool.add(new UTF8(pool, value));
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(stringIndex);
        }
    }

    public static class FieldRef extends Reference
    {
        public FieldRef(ConstantPool pool, Field field)
        {
            super(pool, Kind.FIELD_REF, field);
        }
    }

    public static class MethodRef extends Reference
    {
        public MethodRef(ConstantPool pool, Method value)
        {
            super(pool, Kind.METHOD_REF, value);
        }
    }

    public static class Reference extends Constant<Member>
    {
        private final short classIndex;
        private final short nameAndTypeIndex;

        private Reference(ConstantPool pool, Kind kind, Member value)
        {
            super(pool, kind, value);

            this.classIndex = pool.add(new Class(pool, value.getParent()));
            this.nameAndTypeIndex = pool.add(new NameAndType(pool, value));
        }

        public NameAndType getNameAndType()
        {
            return (NameAndType)pool.get(nameAndTypeIndex);
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(classIndex);
            out.writeShort(nameAndTypeIndex);
        }
    }

    public static class NameAndType extends Constant<Declaration>
    {
        private final short nameIndex;
        private final short descriptorIndex;

        public NameAndType(ConstantPool pool, java.lang.String name, java.lang.String descriptor)
        {
            // TODO: fix value
            super(pool, Kind.NAME_AND_TYPE, null);

            this.nameIndex = pool.add(new UTF8(pool, name));
            this.descriptorIndex = pool.add(new UTF8(pool, descriptor));
        }

        public NameAndType(ConstantPool pool, Declaration value)
        {
            super(pool, Kind.NAME_AND_TYPE, value);

            this.nameIndex = pool.add(new UTF8(pool, value.getName().getValue()));
            this.descriptorIndex = pool.add(new UTF8(pool, Descriptor.from(value).toString()));
        }

        public short getNameIndex()
        {
            return nameIndex;
        }

        public short getDescriptorIndex()
        {
            return descriptorIndex;
        }

        @Override
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(nameIndex);
            out.writeShort(descriptorIndex);
        }
    }
}