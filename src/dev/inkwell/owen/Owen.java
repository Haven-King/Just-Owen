package dev.inkwell.owen;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Owen {
    final String indent;
    final String commentSpacing;
    final boolean spacious;
    final boolean preserveComments;

    private final ThreadLocal<Integer> indentation = ThreadLocal.withInitial(() -> 0);
    private final ThreadLocal<Deque<Deque<OwenElement>>> previous = ThreadLocal.withInitial(() -> {
        Deque<Deque<OwenElement>> deque = new ArrayDeque<>();
        deque.push(new ArrayDeque<>());

        return deque;
    });

    public Owen(String indent, String commentSpacing, boolean spacious, boolean preserveComments) {
        this.indent = indent;
        this.commentSpacing = commentSpacing;
        this.spacious = spacious;
        this.preserveComments = preserveComments;
    }

    public String toString(OwenElement element) {
        if (!element.isObject()) {
            throw new RuntimeException("Root element must be an object!");
        }

        StringBuilder builder = new StringBuilder();

        List<String> comments = element.getComments();

        this.comments(comments, builder);

        writeObject(element, builder);

        return builder.toString();
    }

    private void writeElement(OwenElement element, StringBuilder builder) {
        switch (element.getType()) {
            case LITERAL:
                String value = element.asString();
                value = value.replaceAll("([^\\\\])#", "$1\\\\#");
                value = value.replaceAll("\n", "\\\\n");

                builder.append(value);
                break;
            case ARRAY:
            case OBJECT:
                writeCompound(element, builder);
                break;
            case EMPTY:
                break;
        }
    }

    private void writeCompound(OwenElement element, StringBuilder builder) {
        builder.append(element.isArray() ? '[' : '{');

        if (!element.isEmpty()) {
            builder.append("\n");
        }

        this.indent();

        if (element.getType() == OwenElement.Type.ARRAY) {
            writeArray(element, builder);
        } else {
            writeObject(element, builder);
        }

        this.dedent();

        if (!element.isEmpty()) {
            this.indent(builder);
        }

        builder.append(element.isArray() ? ']' : '}');
    }

    private void writeArray(OwenElement element, StringBuilder builder) {
        Deque<Deque<OwenElement>> deque = this.previous.get();
        Deque<OwenElement> deque1 = new ArrayDeque<>();
        deque.push(deque1);

        for (OwenElement child : element.asList()) {
            List<String> comments = child.getComments();

            this.comments(comments, builder);

            this.indent(builder);
            this.writeElement(child, builder);
            builder.append("\n");

            deque1.push(child);
        }

        deque.pop();
    }

    private void writeObject(OwenElement element, StringBuilder builder) {
        Deque<Deque<OwenElement>> deque = this.previous.get();
        Deque<OwenElement> deque1 = new ArrayDeque<>();
        deque.push(deque1);

        for (Map.Entry<String, OwenElement> entry : element.asMap().entrySet()) {
            OwenElement child = entry.getValue();
            List<String> comments = child.getComments();

            if (this.spacious && !deque1.isEmpty() && (!comments.isEmpty() || (deque1.getFirst().isCompound() && !deque1.getFirst().isEmpty()))) {
                builder.append("\n");
            }

            this.comments(comments, builder);

            this.indent(builder);
            builder.append(entry.getKey()).append('=');
            this.writeElement(child, builder);
            builder.append("\n");

            deque1.push(child);
        }

        deque.pop();
    }

    private void comments(List<String> comments, StringBuilder builder) {
        for (String comment : comments) {
            this.indent(builder);
            builder.append('#').append(this.commentSpacing).append(comment).append('\n');
        }
    }

    private void indent() {
        this.indentation.set(this.indentation.get() + 1);
    }

    private void dedent() {
        this.indentation.set(this.indentation.get() - 1);
    }

    private void indent(StringBuilder builder) {
        for (int i = 0; i < this.indentation.get(); ++i) {
            builder.append(this.indent);
        }
    }

    public static OwenElement literal(String string) {
        return string.isEmpty() ? new OwenElement()
                : new OwenElement(OwenElement.Type.LITERAL, string, null, null);
    }

    public static OwenElement object() {
        return new OwenElement(OwenElement.Type.OBJECT, null, null, new LinkedHashMap<>());
    }

    public static OwenElement object(String key1, OwenElement value1) {
        LinkedHashMap<String, OwenElement> map = new LinkedHashMap<>();

        map.put(key1, value1);

        return new OwenElement(OwenElement.Type.OBJECT, null, null, map);
    }

    public static OwenElement object(String key1, OwenElement value1, String key2, OwenElement value2) {
        LinkedHashMap<String, OwenElement> map = new LinkedHashMap<>();

        map.put(key1, value1);
        map.put(key2, value2);

        return new OwenElement(OwenElement.Type.OBJECT, null, null, map);
    }

    public static OwenElement object(String key1, OwenElement value1, String key2, OwenElement value2, String key3, OwenElement value3) {
        LinkedHashMap<String, OwenElement> map = new LinkedHashMap<>();

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        return new OwenElement(OwenElement.Type.OBJECT, null, null, map);
    }

    public static OwenElement object(String key1, OwenElement value1, String key2, OwenElement value2, String key3, OwenElement value3, String key4, OwenElement value4) {
        LinkedHashMap<String, OwenElement> map = new LinkedHashMap<>();

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);

        return new OwenElement(OwenElement.Type.OBJECT, null, null, map);
    }

    public static OwenElement object(String key1, String value1) {
        LinkedHashMap<String, OwenElement> map = new LinkedHashMap<>();

        map.put(key1, literal(value1));

        return new OwenElement(OwenElement.Type.OBJECT, null, null, map);
    }

    public static OwenElement object(String key1, String value1, String key2, String value2) {
        LinkedHashMap<String, OwenElement> map = new LinkedHashMap<>();

        map.put(key1, literal(value1));
        map.put(key2, literal(value2));

        return new OwenElement(OwenElement.Type.OBJECT, null, null, map);
    }

    public static OwenElement object(String key1, String value1, String key2, String value2, String key3, String value3) {
        LinkedHashMap<String, OwenElement> map = new LinkedHashMap<>();

        map.put(key1, literal(value1));
        map.put(key2, literal(value2));
        map.put(key3, literal(value3));

        return new OwenElement(OwenElement.Type.OBJECT, null, null, map);
    }

    public static OwenElement object(String key1, String value1, String key2, String value2, String key3, String value3, String key4, String value4) {
        LinkedHashMap<String, OwenElement> map = new LinkedHashMap<>();

        map.put(key1, literal(value1));
        map.put(key2, literal(value2));
        map.put(key3, literal(value3));
        map.put(key4, literal(value4));

        return new OwenElement(OwenElement.Type.OBJECT, null, null, map);
    }

    public static OwenElement array() {
        return new OwenElement(OwenElement.Type.ARRAY, null, new ArrayList<>(), null);
    }

    public static OwenElement array(List<OwenElement> list) {
        return new OwenElement(OwenElement.Type.ARRAY, null, new ArrayList<>(list), null);
    }

    public static OwenElement array(OwenElement... elements) {
        return new OwenElement(OwenElement.Type.ARRAY, null, Arrays.asList(elements), null);
    }

    public static OwenElement array(String... elements) {
        return new OwenElement(OwenElement.Type.ARRAY, null, Arrays.stream(elements).map(Owen::literal).collect(Collectors.toList()), null);
    }

    public static OwenElement empty() {
        return new OwenElement();
    }

    public static OwenElement parse(String string) throws ParseException {
        return new OwenParser(OwenTokenizer.tokenize(string)).parse();
    }

    public static OwenElement parse(Reader reader) throws ParseException {
        return parse(new BufferedReader(reader).lines()
                .collect(Collectors.joining("\n")));
    }

    public static OwenElement parse(InputStream inputStream) throws ParseException {
        return parse(new InputStreamReader(inputStream, StandardCharsets.UTF_16));
    }

    public static class Builder {
        private String indent = "  ";
        private String commentSpacing = " ";
        private boolean spacious = true;
        private boolean preserveComments = false;

        public Builder indent(String indentString) {
            this.indent = indentString;
            return this;
        }

        public Builder commentSpacing(String commentSpacing) {
            this.commentSpacing = commentSpacing;
            return this;
        }

        public Builder compact() {
            this.spacious = false;
            return this;
        }

        public Builder preserveComments() {
            this.preserveComments = true;
            return this;
        }

        public Owen build() {
            return new Owen(this.indent, this.commentSpacing, this.spacious, this.preserveComments);
        }
    }
}
