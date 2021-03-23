package dev.inkwell.owen;

import java.util.*;

@SuppressWarnings("unused")
public class OwenElement {
    private final List<String> comments = new ArrayList<>();

    private Type type;
    private String literalValue;
    private List<OwenElement> list;
    private Map<String, OwenElement> map;

    OwenElement() {
        this(Type.EMPTY, null, null, null);
    }

    OwenElement(Type type, String literalValue, List<OwenElement> list, Map<String, OwenElement> map) {
        this.type = type;
        this.literalValue = literalValue;
        this.list = list;
        this.map = map;
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public void addComments(String... comments) {
        this.comments.addAll(Arrays.asList(comments));
    }

    public void addComments(List<String> comments) {
        this.comments.addAll(comments);
    }

    public OwenElement withComment(String comment) {
        this.comments.add(comment);
        return this;
    }

    public OwenElement withComments(String... comments) {
        this.comments.addAll(Arrays.asList(comments));
        return this;
    }

    public OwenElement withComments(List<String> comments) {
        this.comments.addAll(comments);
        return this;
    }

    public void setLiteralValue(String value) {
        if (!(this.type == Type.LITERAL || this.type == Type.EMPTY)) {
            throw new RuntimeException("Cannot set string value of non-Literal element!");
        }

        this.type = Type.LITERAL;
        this.literalValue = value;
    }

    public String asString() {
        if (this.type != Type.LITERAL) {
            throw new RuntimeException("Cannot get non-Literal element as string!");
        }

        return this.literalValue;
    }

    public void add(OwenElement element) {
        if (this.type == Type.EMPTY) {
            this.type = Type.ARRAY;
            this.list = new ArrayList<>();
        }

        if (this.type != Type.ARRAY) {
            throw new RuntimeException("Cannot add element to non-Array element!");
        }

        this.add(element, this.list.size());
    }

    public void add(OwenElement element, int index) {
        if (this.type == Type.EMPTY) {
            this.type = Type.ARRAY;
            this.list = new ArrayList<>();
        }

        if (this.type != Type.ARRAY) {
            throw new RuntimeException("Cannot add element to non-Array element!");
        }

        this.list.add(index, element);
    }

    public OwenElement get(int index) {
        if (this.type == Type.EMPTY) {
            return null;
        }

        if (this.type != Type.ARRAY) {
            throw new RuntimeException("Cannot get indexed element from non-Array element!");
        }

        return this.list.get(index);
    }

    public List<OwenElement> asList() {
        if (this.type == Type.EMPTY) {
            this.type = Type.ARRAY;
            this.list = new ArrayList<>();
        }

        if (this.type != Type.ARRAY) {
            throw new RuntimeException("Cannot get non-Array element as list!");
        }

        return this.list;
    }

    public void put(String key, String value) {
        this.put(key, Owen.literal(value));
    }

    public void put(String key, OwenElement element) {
        if (this.type == Type.EMPTY) {
            this.type = Type.OBJECT;
            this.map = new LinkedHashMap<>();
        }

        if (this.type != Type.OBJECT) {
            throw new RuntimeException("Cannot put element in non-Object element!");
        }

        this.map.put(key, element);
    }

    public OwenElement get(String key) {
        if (this.type == Type.EMPTY) {
            return null;
        }

        if (this.type != Type.OBJECT) {
            throw new RuntimeException("Cannot get keyed element from non-Object element!");
        }

        return this.map.get(key);
    }

    public Map<String, OwenElement> asMap() {
        if (this.type == Type.EMPTY) {
            this.type = Type.OBJECT;
            this.map = new LinkedHashMap<>();
        }

        if (this.type != Type.OBJECT) {
            throw new RuntimeException("Cannot get non-Object element as map.");
        }

        return this.map;
    }

    public boolean isObject() {
        return this.type == Type.OBJECT;
    }

    public boolean isArray() {
        return this.type == Type.ARRAY;
    }

    public boolean isLiteral() {
        return this.type == Type.LITERAL;
    }

    public boolean isCompound() {
        return this.type == Type.OBJECT || this.type == Type.ARRAY;
    }

    public boolean isEmpty() {
        switch (this.type) {
            case LITERAL:    return this.literalValue.isEmpty();
            case ARRAY:     return this.list.isEmpty();
            case OBJECT:    return this.map.isEmpty();
            case EMPTY:     return true;
        }

        throw new RuntimeException("Impossible type!");
    }

    public List<String> getComments() {
        return this.comments;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        LITERAL, ARRAY, OBJECT, EMPTY
    }
}
