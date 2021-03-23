package dev.inkwell.owen;

class Token {
    public static final Token EQ = new Token(Type.EQ);
    public static final Token ARRAY_START = new Token(Type.ARRAY_START);
    public static final Token ARRAY_CLOSE = new Token(Type.ARRAY_CLOSE);
    public static final Token OBJECT_START = new Token(Type.OBJECT_START);
    public static final Token OBJECT_CLOSE = new Token(Type.OBJECT_CLOSE);

    private final Type type;
    private final String value;

    private Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    private Token(Type type) {
        this(type, null);
    }

    String getValue() {
        if (!this.type.hasValue) {
            throw new RuntimeException("Cannot get type for builtin token!");
        } else {
            return this.value;
        }
    }

    @Override
    public String toString() {
        if (this.type == Type.IDENTIFIER) {
            return "IDENTIFIER[" + this.value + ']';
        } else if (this.type == Type.VALUE) {
            return "VALUE[" + this.value + ']';
        } else {
            return this.type.toString();
        }
    }

    static Token identifier(String value) {
        return new Token(Type.IDENTIFIER, value);
    }

    static Token value(String value) {
        return new Token(Type.VALUE, value);
    }

    static Token comment(String value) {
        return new Token(Type.COMMENT, value);
    }

    Type getType() {
        return this.type;
    }

    enum Type {
        EQ, ARRAY_START, ARRAY_CLOSE, OBJECT_START, OBJECT_CLOSE,
        IDENTIFIER(true), VALUE(true), COMMENT(true);

        public final boolean hasValue;

        Type(boolean hasValue) {
            this.hasValue = hasValue;
        }

        Type() {
            this(false);
        }
    }
}
