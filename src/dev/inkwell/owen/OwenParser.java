package dev.inkwell.owen;

import java.text.ParseException;
import java.util.*;

@SuppressWarnings("unused")
class OwenParser {
    private final List<String> leadingComments = new ArrayList<>();
    private final Deque<Token> tokens;

    OwenParser(Deque<Token> tokens) {
        this.tokens = tokens;
    }

    public OwenElement parse() throws ParseException {
        return object();
    }

    private OwenElement object() throws ParseException {
        OwenElement element = new OwenElement();

        consume(Token.Type.OBJECT_START);

        withComments(element);

        while (!tokens.isEmpty() && peek().map(t -> matches(t, Token.Type.IDENTIFIER)).orElse(false)) {
            String id = next().getValue();
            consume(Token.Type.EQ);
            OwenElement value = value();

            element.put(id, value);
        }

        consumeComments();

        consume(Token.Type.OBJECT_CLOSE);

        return element;
    }

    private OwenElement array() throws ParseException {
        OwenElement element = new OwenElement();

        consume(Token.Type.ARRAY_START);

        withComments(element);

        while (!tokens.isEmpty() && peek().map(t -> matches(t, Token.Type.OBJECT_START, Token.Type.ARRAY_START, Token.Type.VALUE)).orElse(false)) {
            element.add(value());
        }

        consumeComments();

        consume(Token.Type.ARRAY_CLOSE);

        return element;
    }

    private OwenElement value() throws ParseException {
        Token token = peek().orElseThrow(() ->
                new ParseException("Expected 'OBJECT_START', 'ARRAY_START', or 'VALUE'. Found 'EOF'.", -1)
        );

        switch (token.getType()) {
            case OBJECT_START: return object();
            case ARRAY_START: return array();
            case VALUE: return withComments(Owen.literal(next().getValue()));
            default:
                throw new ParseException("Expected 'OBJECT_START', 'ARRAY_START', or 'VALUE'. Found '" + token + "'.", -1);
        }
    }

    private void consume(Token.Type tokenType) throws ParseException {
        Token token = next();

        if (token.getType() != tokenType) {
            throw new ParseException("Expected '" + tokenType.toString() + "'. Found '" + token.toString() + "'.", -1);
        }
    }

    private Optional<Token> peek() {
        for (Token token : tokens) {
            if (token.getType() != Token.Type.COMMENT) return Optional.of(token);
        }

        return Optional.empty();
    }

    private Token next() throws ParseException {
        while (!tokens.isEmpty() && tokens.peek().getType() == Token.Type.COMMENT) {
            this.leadingComments.add(tokens.pop().getValue());
        }

        if (tokens.isEmpty()) {
            throw new ParseException("Expected token. Found 'EOF'.", -1);
        }

        return tokens.pop();
    }

    private OwenElement withComments(OwenElement element) {
        element.addComments(leadingComments);

        leadingComments.clear();

        return element;
    }

    private void consumeComments() {
        while (!tokens.isEmpty() && tokens.peek().getType() == Token.Type.COMMENT) {
            this.tokens.pop();
        }
    }

    private static boolean matches(Token token, Token.Type type, Token.Type... types) {
        Token.Type tokenType = token.getType();

        if (tokenType == type) return true;

        for (Token.Type t : types) {
            if (t == tokenType) return true;
        }

        return false;
    }
}
