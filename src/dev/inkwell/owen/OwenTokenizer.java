package dev.inkwell.owen;

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;

class OwenTokenizer {
    private final Deque<Token> tokens = new ArrayDeque<>();
    private final String string;
    private final int eof;

    private int p = 0;

    private OwenTokenizer(String string) throws ParseException {
        this.string = string;
        this.eof = string.length();
        seek();
        object();
    }

    private void element() throws ParseException {
        if (p < eof) {
            char c = string.charAt(p);

            switch (c) {
                case '{':
                    consume('{');
                    object();
                    consume('}');
                    break;
                case '[':
                    consume('[');
                    array();
                    consume(']');
                    break;
                default:
                    value();
            }
        } else {
            tokens.addLast(Token.value(""));
        }
    }

    private void object() throws ParseException {
        tokens.addLast(Token.OBJECT_START);

        seek();

        char c;

        while (p < eof && (c = string.charAt(p)) != '}') {
            if (!Character.isAlphabetic(c)) {
                throw new ParseException("Identifier must start with alphabetic character", p);
            }

            final int s = p++;

            while (isIdentifierPart(string.charAt(p))) {
                ++p;
            }

            tokens.addLast(Token.identifier(string.substring(s, p)));

            whitespace();

            consume('=');

            tokens.addLast(Token.EQ);

            nonBreakingWhitespace();

            element();
            seek();
        }

        tokens.addLast(Token.OBJECT_CLOSE);
    }

    private void array() throws ParseException {
        tokens.addLast(Token.ARRAY_START);

        seek();

        while (p < eof && string.charAt(p) != ']') {
            element();
            seek();
        }

        tokens.addLast(Token.ARRAY_CLOSE);
    }

    private void value() throws ParseException {
        StringBuilder builder = new StringBuilder();

        char c;

        while (p < eof && (c = string.charAt(p)) != '\n') {
            if (c == '\\') {
                c = string.charAt(++p);

                switch (c) {
                    case 'n': builder.append('\n'); break;
                    case 'r': builder.append('\r'); break;
                    case 't': builder.append('\t'); break;
                    case '\\': builder.append('\\'); break;
                    case '\n': ++p; break;
                    default: throw new ParseException("Unexpected escaped token '" + c + "'", p);
                }

            } else {
                builder.append(c);
            }

            ++p;
        }

        tokens.addLast(Token.value(builder.toString()));
    }

    private void seek() {
        while (p < eof) {
            char c = string.charAt(p);
            if (Character.isWhitespace(c)) {
                ++p;
            } else if (c == '#') {
                comment();
            } else {
                break;
            }
        }
    }

    private void comment() {
        ++p;
        whitespace();

        final int s = p;

        while (p < eof && string.charAt(p) != '\n') {
            ++p;
        }

        tokens.addLast(Token.comment(string.substring(s, p)));
    }

    private void nonBreakingWhitespace() {
        char c;

        while (p < eof && ((c = string.charAt(p)) == ' ' || c == '\t')) {
            ++p;
        }
    }

    private void whitespace() {
        while (p < eof && Character.isWhitespace(string.charAt(p))) {
            ++p;
        }
    }

    private void consume(char c) throws ParseException {
        char f = string.charAt(p);
        if (f == c) {
            ++p;
        } else {
            throw new ParseException("Expected '" + c + "'. Found '" + f + "'", p);
        }
    }

    static Deque<Token> tokenize(String string) throws ParseException {
        return new OwenTokenizer(string).tokens;
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '.' || c == '/' || c == '+' || c == '\\' || c == ':' || c == '_' || c == '-';
    }
}
