package codes.vps.jpb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

public class JsonPathTest {

    @Test
    public void basic() {

        JsonPathBuilder b = new JsonPathBuilder().property("store").property("book").arrayElement(0).property("title");
        Assertions.assertEquals(5, b.size());

        JsonPath p = b.build();
        Assertions.assertEquals("$.store.book[0].title", p.toString());
        b = p.toBuilder();

        Assertions.assertEquals(5, b.size());
        b.clip();
        b.clip();
        Assertions.assertEquals(3, b.size());
        Assertions.assertEquals("$.store.book", b.build().toString());

        JsonPathBuilder copy = b.copy();
        Assertions.assertThrows(IllegalArgumentException.class, ()->copy.clip(3));
        copy.clip(2);
        Assertions.assertEquals(1, copy.size());

    }

    @Test
    public void allElements() {

        JsonPathBuilder b = new JsonPathBuilder()
                .property("normal")
                .property("cr*a'z\\y")
                .property("")
                .arrayElement(3)
                .allArrayElements()
                .wildCard();

        String exp = "$.normal['cr*a\\'z\\\\y'][''][3][*].*";
        Assertions.assertEquals(exp, b.build().toString());
        Assertions.assertEquals(exp, b.toString());

        Assertions.assertEquals(exp, b.path.stream().map(PathElement::toString).collect(Collectors.joining("")));

    }

    @Test
    public void controlCharacters() {

        // RFC-9535 string literals must not contain unescaped control characters
        JsonPathBuilder b = new JsonPathBuilder().property("a\bb\fc\nd\re\tf\u0000g\u001fh");
        String exp = "$['a\\bb\\fc\\nd\\re\\tf\\u0000g\\u001fh']";
        Assertions.assertEquals(exp, b.toString());
        Assertions.assertEquals(exp, JsonPath.parse(exp).toString());

        StringBuilder all = new StringBuilder();
        for (char c = 0; c < 0x20; c++) { all.append(c); }
        String rendered = new JsonPathBuilder().property(all.toString()).toString();
        for (char c : rendered.toCharArray()) {
            Assertions.assertTrue(c >= 0x20, "unescaped control character in "+rendered);
        }
        Assertions.assertEquals(rendered, JsonPath.parse(rendered).toString());

    }

    @Test
    public void parseRoundTrip() {

        roundTrip("$");
        roundTrip("$.store.book[0].title");
        roundTrip("$.normal['cr*a\\'z\\\\y'][''][3][*].*");
        roundTrip("$[-1][2147483647][-2147483648]");
        roundTrip("$.été['a b']['1a']");

        // every element the builder renders comes back as the same element
        JsonPathBuilder b = new JsonPathBuilder()
                .property("x").property("with space").property("").arrayElement(7)
                .allArrayElements().wildCard().property("😀");
        JsonPath p = JsonPath.parse(b.toString());
        Assertions.assertEquals(b.size(), p.toBuilder().size());
        Assertions.assertEquals(b.toString(), p.toString());

    }

    @Test
    public void parseEquivalents() {

        equivalent("$.a_b", "$['a_b']");
        equivalent("$[\"x\"]", "$.x");
        equivalent("$[\"it's\"]", "$['it\\'s']");
        equivalent("$['say \"hi\"']", "$['say \"hi\"']");
        equivalent("$[\"q\\\"\"]", "$['q\"']");
        equivalent("$['\\u0041\\/\\\\']", "$['A/\\\\']");
        equivalent("$['\\uD83D\\uDE00']", "$['😀']");
        equivalent("$ .a\t[ 'b' ]\n[ 0 ]\r[ * ] .*", "$.a.b[0][*].*");

    }

    @Test
    public void parseFailures() {

        parseFail("", ParseErrorType.PATH_NO_ROOT, 0);
        parseFail("a.b", ParseErrorType.PATH_NO_ROOT, 0);
        parseFail(" $", ParseErrorType.PATH_NO_ROOT, 0);
        parseFail("$a", ParseErrorType.PATH_UNEXPECTED_CHAR, 1);
        parseFail("$ ", ParseErrorType.PATH_UNEXPECTED_END, 2);
        parseFail("$.", ParseErrorType.PATH_UNEXPECTED_END, 2);
        parseFail("$.1a", ParseErrorType.PATH_UNEXPECTED_CHAR, 2);
        parseFail("$.a-b", ParseErrorType.PATH_UNEXPECTED_CHAR, 3);
        parseFail("$. a", ParseErrorType.PATH_UNEXPECTED_CHAR, 2);
        parseFail("$[a]", ParseErrorType.PATH_UNEXPECTED_CHAR, 2);
        parseFail("$[", ParseErrorType.PATH_UNEXPECTED_END, 2);
        parseFail("$[0", ParseErrorType.PATH_UNEXPECTED_END, 3);
        parseFail("$[0}", ParseErrorType.PATH_UNEXPECTED_CHAR, 3);

        // constructs the builder can not represent
        parseFail("$..a", ParseErrorType.PATH_UNSUPPORTED, 1);
        parseFail("$[1:2]", ParseErrorType.PATH_UNSUPPORTED, 3);
        parseFail("$[:2]", ParseErrorType.PATH_UNSUPPORTED, 2);
        parseFail("$[?@.a]", ParseErrorType.PATH_UNSUPPORTED, 2);
        parseFail("$[1,2]", ParseErrorType.PATH_UNSUPPORTED, 3);
        parseFail("$['a', 'b']", ParseErrorType.PATH_UNSUPPORTED, 5);
        parseFail("$[*,0]", ParseErrorType.PATH_UNSUPPORTED, 3);

        // indexes
        parseFail("$[01]", ParseErrorType.PATH_BAD_INDEX, 2);
        parseFail("$[-0]", ParseErrorType.PATH_BAD_INDEX, 2);
        parseFail("$[-a]", ParseErrorType.PATH_BAD_INDEX, 2);
        parseFail("$[2147483648]", ParseErrorType.PATH_INDEX_RANGE, 2);
        parseFail("$[-2147483649]", ParseErrorType.PATH_INDEX_RANGE, 2);
        parseFail("$[99999999999999999999999]", ParseErrorType.PATH_INDEX_RANGE, 2);

        // string literals
        parseFail("$['abc", ParseErrorType.PATH_UNEXPECTED_END, 6);
        parseFail("$['a\nb']", ParseErrorType.PATH_BAD_STRING_CHAR, 4);
        parseFail("$['a\\\"']", ParseErrorType.PATH_BAD_ESCAPE, 4);
        parseFail("$[\"a\\'\"]", ParseErrorType.PATH_BAD_ESCAPE, 4);
        parseFail("$['\\x']", ParseErrorType.PATH_BAD_ESCAPE, 3);
        parseFail("$['\\u00g0']", ParseErrorType.PATH_BAD_ESCAPE, 3);
        parseFail("$['\\u12']", ParseErrorType.PATH_BAD_ESCAPE, 3);
        parseFail("$['\\uD800']", ParseErrorType.PATH_BAD_ESCAPE, 3);
        parseFail("$['\\uD800\\u0041']", ParseErrorType.PATH_BAD_ESCAPE, 3);
        parseFail("$['\\uDC00']", ParseErrorType.PATH_BAD_ESCAPE, 3);
        parseFail("$['\uD800']", ParseErrorType.PATH_BAD_STRING_CHAR, 3);

    }

    private static void roundTrip(String s) {
        Assertions.assertEquals(s, JsonPath.parse(s).toString());
    }

    private static void equivalent(String s, String canonical) {
        Assertions.assertEquals(canonical, JsonPath.parse(s).toString(), s);
    }

    private static void parseFail(String s, ParseErrorType type, int position) {
        ParseException pe = Assertions.assertThrows(ParseException.class, ()->JsonPath.parse(s), s);
        Assertions.assertEquals(type, pe.getType(), s);
        Assertions.assertEquals(position, pe.getPosition(), s);
    }

}
