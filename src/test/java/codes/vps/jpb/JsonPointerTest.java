package codes.vps.jpb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.function.Consumer;

public class JsonPointerTest {

    @Test
    public void parseFail() {

        ParseException pe = Assertions.assertThrows(ParseException.class, ()->JsonPointer.parse("boo"));
        Assertions.assertEquals(ParseErrorType.PTR_NO_REF_TOKEN_START, pe.getType());
        Assertions.assertEquals(0, pe.getPosition());

    }

    @Test
    public void strayTilde() {

        ParseException pe = Assertions.assertThrows(ParseException.class, ()->JsonPointer.parse("/abc~0~2def"));
        Assertions.assertEquals(ParseErrorType.PTR_STRAY_TILDE, pe.getType());
        Assertions.assertEquals(6, pe.getPosition());

        pe = Assertions.assertThrows(ParseException.class, ()->JsonPointer.parse("/ac/~"));
        Assertions.assertEquals(ParseErrorType.PTR_STRAY_TILDE, pe.getType());
        Assertions.assertEquals(4, pe.getPosition());

    }

    @Test
    public void combos() {

        roundTrip("");
        roundTrip("/foo/0", "foo", "0");
        roundTrip("/", "");
        roundTrip("/a//b", "a", "", "b");
        roundTrip("/a~1b/c%d/e^f/g|h/i\\j/k\"l/ /m~0n", "a/b", "c%d", "e^f", "g|h", "i\\j", "k\"l", " ", "m~n");

    }

    private void roundTrip(String s, String ... members) {

        JsonPointer pointer = JsonPointer.parse(s);
        JsonPointerBuilder b = pointer.toBuilder();
        Assertions.assertEquals(members.length, b.size());
        int i = 0;
        for (PointerElement e : b) {
            String m = members[i++];
            Assertions.assertEquals(m, e.getAsMember());
        }
        Assertions.assertEquals(s, b.build().toString());

    }

    @Test
    public void parse() {

        JsonPointer ptr = JsonPointer.parse("/abc/012/1a3/-/-15/42/~0");
        Iterator<PointerElement> i = ptr.iterator();

        JsonPointerBuilder reBuilder = new JsonPointerBuilder();

        testElement(i, reBuilder, false, null, "abc");
        testElement(i, reBuilder, false, null, "012");
        testElement(i, reBuilder, false, null, "1a3");
        testElement(i, reBuilder, true, null, "-");
        testElement(i, reBuilder, false, null, "-15");
        testElement(i, reBuilder, false, 42, "42");
        testElement(i, reBuilder, false, null, "~");

        Assertions.assertFalse(i.hasNext());

        Assertions.assertEquals("/abc/012/1a3/-/-/-15/42/42/~0", reBuilder.toString());

    }

    private void testElement(Iterator<PointerElement> next, JsonPointerBuilder reBuilder, boolean isEOA, Integer num, String member) {

        PointerElement parsed = next.next();

        Consumer<PointerElement> test = e->{
            Assertions.assertEquals(isEOA, e.isEndOfArray());
            BigInteger bi = num == null ? null : BigInteger.valueOf(num.longValue());
            Assertions.assertEquals(bi, e.getAsIndex());
            Assertions.assertEquals(member, e.getAsMember());
        };

        test.accept(parsed);

        BigInteger i = parsed.getAsIndex();

        Runnable testLast = ()-> test.accept(reBuilder.elementAt(reBuilder.size()-1));

        if (i != null) {
            reBuilder.index(i);
            testLast.run();
        }
        if (parsed.isEndOfArray()) {
            reBuilder.pastLastArray();
            testLast.run();
        }
        reBuilder.member(parsed.getAsMember());
        testLast.run();

    }

}
