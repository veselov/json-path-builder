package codes.vps.jpb;

import codes.vps.jpb.el.Property;
import codes.vps.jpb.el.Root;
import codes.vps.jpb.el.Subscript;
import codes.vps.jpb.el.Wildcard;
import codes.vps.jpb.el.WildcardSubscript;

import java.util.LinkedList;
import java.util.List;

public class JsonPathBuilder {

    protected final LinkedList<PathElement> path = new LinkedList<>();
    protected int size;

    public JsonPathBuilder() {
        addElement(new Root());
    }

    static String toString(List<PathElement> path) {

        StringBuilder sb = new StringBuilder();
        path.forEach(p->p.render(sb));
        return sb.toString();

    }

    @Override
    public String toString() {
        return toString(path);
    }

    protected JsonPathBuilder addElement(PathElement e) {

        if (e.isRoot() && size == 1) { return this; }

        path.add(e);
        size++;
        return this;
    }

    public JsonPath build() {

        return new JsonPath(path);

    }

    public JsonPathBuilder clip() {
        return clip(1);
    }

    public JsonPathBuilder clip(int l) {

        if (size - l < 1) {
            throw new IllegalArgumentException("Can not clip more than "+(size-1)+" elements");
        }

        path.subList(size - l, size).clear();
        size -= l;

        return this;

    }

    /**
     * Adds a child operator with the specified property name to the path.
     * @param name property
     * @return this builder
     */
    public JsonPathBuilder property(String name) {
        return addElement(new Property(name));
    }

    /**
     * Adds a wildcard property operator ({@code .*}) to the path.
     * @return this builder
     */
    public JsonPathBuilder wildCard() {
        return addElement(new Wildcard());
    }

    public JsonPathBuilder arrayElement(int idx) {
        return addElement(new Subscript(idx));
    }

    public JsonPathBuilder allArrayElements() {
        return addElement(new WildcardSubscript());
    }

    public JsonPathBuilder copy() {
        JsonPathBuilder b = new JsonPathBuilder();
        path.forEach(b::addElement);
        return b;
    }

    public int size() {
        return path.size();
    }

}
