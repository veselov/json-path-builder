package codes.vps.jpb;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a JSON Path, as defined by
 * <a href="https://goessner.net/articles/JsonPath/">Stefan Goessner</a>.
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

    @Override
    public Iterator<PathElement> iterator() {
        return path.iterator();
    }
}
