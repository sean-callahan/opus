package net.seancallahan.opus.compiler.jvm;

import java.io.DataOutputStream;
import java.io.IOException;

public class Constant<T>
{
    private final Kind kind;
    private final T value;

    public Constant(Kind kind, T value)
    {
        this.kind = kind;
        this.value = value;
    }

    public void write(DataOutputStream out) throws IOException
    {
        out.writeByte((byte)kind.getTag());
        writeValue(out);
    }

    public Kind getKind()
    {
        return kind;
    }

    public T getValue()
    {
        return value;
    }

    private void writeValue(DataOutputStream out) throws IOException
    {
        switch (kind)
        {
            case CLASS:
            case STRING:
                out.writeShort((short)value);
                break;
            case INTEGER:
                out.writeInt((int)value);
                break;
            case FLOAT:
                out.writeFloat((float)value);
                break;
            case LONG:
                out.writeLong((long)value);
                break;
            case DOUBLE:
                out.writeDouble((double)value);
                break;
            case UTF8:
                out.writeUTF((String)value);
                break;
        }
    }

    public enum Kind
    {
        CLASS(7),
        FIELD_REF(9),
        METHOD_REF(10),
        INTERFACE_METHOD_REF(11),
        STRING(8),
        INTEGER(3),
        FLOAT(4),
        LONG(5),
        DOUBLE(6),
        NAME_AND_TYPE(12),
        UTF8(1),
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
}