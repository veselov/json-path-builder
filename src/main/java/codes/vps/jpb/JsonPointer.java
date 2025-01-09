package codes.vps.jpb;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a JSON Pointer, as defined by
 * <a href="https://datatracker.ietf.org/doc/html/rfc6901">RFC-6901</a>.
 */
public class JsonPointer implements Iterable<PointerElement> {

    protected final List<PointerElement> path;
    protected final String strPath;

    JsonPointer(List<PointerElement> elements) {

        path = Collections.unmodifiableList(elements);
        strPath = JsonPointerBuilder.toString(path);

    }

    @Override
    public String toString() {
        return strPath;
    }

    public JsonPointerBuilder toBuilder() {

        JsonPointerBuilder b = new JsonPointerBuilder();
        for (PointerElement e : path) {
            b.addElement(e);
        }

        return b;

    }

    public static JsonPointer parse(String str) {

        JsonPointerBuilder b = new JsonPointerBuilder();
        int ptr = 0;
        int len = str.length();

        while (ptr != len) {

            if (str.charAt(ptr) != '/') {
                // this literally can only happen at [0]
                throw new ParseException(ParseErrorType.PTR_NO_REF_TOKEN_START, ptr);
            }

            int next = str.indexOf("/", ptr + 1);
            String refTok;
            if (next == -1) {
                refTok = str.substring(ptr + 1);
            } else {
                refTok = str.substring(ptr + 1, next);
            }

            ptr++; // skip slash

            StringBuilder decoded = new StringBuilder(refTok.length());
            boolean tilde = false;
            int offset = 0;
            boolean num = true;
            boolean last = true;
            for (char c : refTok.toCharArray()) {
                if (tilde) {
                    tilde = false;
                    if (c == '0') {
                        decoded.append('~');
                    } else if (c == '1') {
                        decoded.append('/');
                    } else {
                        tilde = true;
                        break;
                    }
                } else {
                    if (c == '~') {
                        tilde = true;
                        // can't be a number or a last anymore
                        last = false;
                        num = false;
                    } else {
                        if (offset == 0) {
                            if (c != '-') {
                                last = false;
                            }
                            if (c < '1' || c > '9') {
                                num = false;
                            }
                        } else {
                            last = false;
                            if (c < '0' || c > '9') {
                                num = false;
                            }
                        }
                        decoded.append(c);
                    }
                }
                offset++;
            }

            if (offset == 0) { num = false; last = false; }

            if (tilde) {
                throw new ParseException(ParseErrorType.PTR_STRAY_TILDE, ptr + offset - 1);
            }

            if (last) {
                b.pastLastArray();
            } else if (num) {
                b.index(new BigInteger(decoded.toString()));
            } else {
                b.nonIndexMember(decoded.toString());
            }

            ptr+=offset;

        }

        return b.build();

    }

    @Override
    public Iterator<PointerElement> iterator() {
        return path.iterator();
    }
}
