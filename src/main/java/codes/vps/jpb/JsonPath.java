package codes.vps.jpb;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a JSON Path, as defined by
 * <a href="https://datatracker.ietf.org/doc/html/rfc9535">RFC-9535</a>.
 */
public class JsonPath implements Iterable<PathElement> {

    protected final List<PathElement> path;
    protected final String strPath;

    JsonPath(List<PathElement> elements) {

        path = Collections.unmodifiableList(elements);
        strPath = JsonPathBuilder.toString(path);

    }

    @Override
    public String toString() {
        return strPath;
    }

    public JsonPathBuilder toBuilder() {

        JsonPathBuilder b = new JsonPathBuilder();
        for (PathElement e : path) {
            b.addElement(e);
        }

        return b;

    }

    /**
     * Parses a JSON Path. Only the paths that {@link JsonPathBuilder} can represent are accepted:
     * the root identifier, followed by child segments that select a single member name, array index
     * or wildcard, in either bracket or shorthand notation.
     * @param str path to parse
     * @return parsed path
     * @throws ParseException if the path is not valid, or uses a construct that is not supported
     */
    public static JsonPath parse(String str) {
        return new JsonPathParser(str).parse();
    }

    @Override
    public Iterator<PathElement> iterator() {
        return path.iterator();
    }
}
