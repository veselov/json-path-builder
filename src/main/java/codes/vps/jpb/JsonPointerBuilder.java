package codes.vps.jpb;

import java.math.BigInteger;

public class JsonPointerBuilder extends CommonBuilder<PointerElement, JsonPointerBuilder> {

    public JsonPointer build() {
        return new JsonPointer(path);
    }

    public JsonPointerBuilder member(String val) {
        return addElement(new PointerElement(val));
    }

    JsonPointerBuilder nonIndexMember(String val) {
        return addElement(new PointerElement(val, false));
    }

    public JsonPointerBuilder index(long index) {
        return addElement(new PointerElement(index));
    }

    public JsonPointerBuilder index(BigInteger index) {
        return addElement(new PointerElement(index));
    }

    public JsonPointerBuilder index(int index) {
        return addElement(new PointerElement(index));
    }

    public JsonPointerBuilder pastLastArray() {
        return addElement(PointerElement.eoaInstance);
    }

    public JsonPointerBuilder copy() {
        JsonPointerBuilder b = new JsonPointerBuilder();
        path.forEach(b::addElement);
        return b;
    }

}
