package net.seancallahan.opus.compiler.jvm;

public final class AccessFlag
{
    public static final short PUBLIC      = 1;
    public static final short PRIVATE     = 1 << 1;
    public static final short PROTECTED   = 1 << 2;
    public static final short STATIC      = 1 << 3;
    public static final short FINAL       = 1 << 4;
    public static final short SYNCRONIZED = 1 << 5;
    public static final short BRIDGE      = 1 << 6;
    public static final short VARARGS     = 1 << 7;
    public static final short NATIVE      = 1 << 8;
    //
    public static final short ABSTRACT    = 1 << 10;
    public static final short STRICT      = 1 << 11;
    public static final short SYNTHETIC   = 1 << 12;
    public static final short ANNOTATION  = 1 << 13;
    public static final short ENUM        = 1 << 14;
    public static final short MODULE      = (short)(1 << 15);

}
