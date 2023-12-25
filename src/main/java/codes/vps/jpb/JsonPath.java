package codes.vps.jpb;

import java.util.Collections;
import java.util.List;

public class JsonPath {

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

}
