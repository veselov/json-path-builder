package codes.vps.jpb;

import codes.vps.jpb.el.Property;
import codes.vps.jpb.el.Root;
import codes.vps.jpb.el.Subscript;
import codes.vps.jpb.el.Wildcard;
import codes.vps.jpb.el.WildcardSubscript;

public class JsonPathBuilder extends CommonBuilder<PathElement, JsonPathBuilder> {

    public JsonPathBuilder() {
        addElement(new Root());
    }

    protected JsonPathBuilder addElement(PathElement e) {

        if (e.isRoot() && size == 1) { return this; }
        return super.addElement(e);

    }

    public JsonPath build() {
        return new JsonPath(path);
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

}
