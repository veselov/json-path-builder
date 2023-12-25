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

}
