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

    i8
    i16
    i32
    i64

    u8
    u16
    u32
    u64

    f32
    f64

### String Types

    string

### Array Types

    [0]string

### List Types

    <string>

### Class Types

    :: {
        x int
        y int
    }

### Function Types

    :: ()
    :: (a int, b int) -> (c int)
    :: (in u8) -> (callback :: (n u8))

### Map types

    <string, int>

## Functions