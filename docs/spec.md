## Lexical Structure

Opus source code is interpreted as a sequence of Unicode code points encoded in UTF-8.

### Keywords

    const
    else
    false
    for
    if
    import
    in
    nil
    package
    return
    this
    true
    var

### Operators

    +  : Add
    -  : Subtract
    *  : Multiply
    /  : Divide
    %  : Modulus
    && : Logical and
    || : Logical or
    -> : Returns
    ++ : Increment
    -- : Decrement
    == : Logical equals
    <  : Less than
    >  : Greater than
    =  : Assignment
    !  : Logical not
    != : Not equals
    <= : Less than or equals
    >= : Greater than or equals
    := : Define
    :: : Function
    {
    }
    [
    ]
    (
    )
    ,

### Numeric Types

    s8  - Signed 8-bit integer  (char)
    s16 - Signed 16-bit integer (short)
    s32 - Signed 32-bit integer (int)
    s64 - Signed 64-bit integer (long)

    u8  - Unsigned 8-bit integer
    u16 - Unsigned 16-bit integer
    u32 - Unsigned 32-bit integer
    u64 - Unsigned 64-bit integer

    f32 - 32-bit floating point number (float)
    f64 - 64-bit floating point number (double)

### String Types

    string - UTF-8 array of characters

### Array Types

    []string - statically sized array [of strings]

### List Types

    <string> - dynamically sized array [of strings]

### Map types

    <string, s32> - hash map [with 'string' key and 's32' value]

### Class Types

    :: {
        x s32
        y s32
    }

### Function Types

    :: ()
    :: (a s32, b s32) -> (c s32)