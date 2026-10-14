package codes.vps.jpb;

/**
 * Parses the subset of <a href="https://datatracker.ietf.org/doc/html/rfc9535">RFC-9535</a> JSONPath
 * queries that {@link JsonPathBuilder} can represent: the root identifier, child segments with a single
 * name, index or wildcard selector, in either bracket or shorthand notation. Descendant segments,
 * slice and filter selectors, and segments with more than one selector are rejected.
 */
class JsonPathParser {

    private final String str;
    private final int len;
    private final JsonPathBuilder b = new JsonPathBuilder();
    private int ptr;

    JsonPathParser(String str) {
        this.str = str;
        len = str.length();
    }

    JsonPath parse() {

        if (len == 0 || str.charAt(0) != '$') {
            throw new ParseException(ParseErrorType.PATH_NO_ROOT, 0);
        }

        ptr = 1;

        while (true) {

            // segments = *(S segment), so blank space must be followed by a segment
            int beforeBlank = ptr;
            skipBlank();
            if (ptr == len) {
                if (ptr != beforeBlank) {
                    throw new ParseException(ParseErrorType.PATH_UNEXPECTED_END, ptr);
                }
                break;
            }

            char c = str.charAt(ptr);
            if (c == '.') {
                dotSegment();
            } else if (c == '[') {
                bracketSegment();
            } else {
                throw new ParseException(ParseErrorType.PATH_UNEXPECTED_CHAR, ptr);
            }

        }

        return b.build();

    }

    private void dotSegment() {

        int dot = ptr++;

        if (ptr == len) {
            throw new ParseException(ParseErrorType.PATH_UNEXPECTED_END, ptr);
        }

        char c = str.charAt(ptr);

        if (c == '.') {
            // descendant segment
            throw new ParseException(ParseErrorType.PATH_UNSUPPORTED, dot);
        }

        if (c == '*') {
            ptr++;
            b.wildCard();
            return;
        }

        int start = ptr;
        int cp = str.codePointAt(ptr);
        if (!isNameFirst(cp)) {
            throw new ParseException(ParseErrorType.PATH_UNEXPECTED_CHAR, ptr);
        }
        ptr += Character.charCount(cp);

        while (ptr < len) {
            cp = str.codePointAt(ptr);
            if (!isNameFirst(cp) && !isDigit(cp)) { break; }
            ptr += Character.charCount(cp);
        }

        b.property(str.substring(start, ptr));

    }

    private void bracketSegment() {

        ptr++; // skip [
        skipBlank();

        if (ptr == len) {
            throw new ParseException(ParseErrorType.PATH_UNEXPECTED_END, ptr);
        }

        char c = str.charAt(ptr);

        if (c == '\'' || c == '"') {
            String name = stringLiteral(c);
            closeBracket();
            b.property(name);
        } else if (c == '*') {
            ptr++;
            closeBracket();
            b.allArrayElements();
        } else if (c == '-' || isDigit(c)) {
            int idx = index();
            if (ptr < len && str.charAt(ptr) == ':') {
                // slice selector
                throw new ParseException(ParseErrorType.PATH_UNSUPPORTED, ptr);
            }
            closeBracket();
            b.arrayElement(idx);
        } else if (c == ':' || c == '?') {
            // slice selector without a start, or filter selector
            throw new ParseException(ParseErrorType.PATH_UNSUPPORTED, ptr);
        } else {
            throw new ParseException(ParseErrorType.PATH_UNEXPECTED_CHAR, ptr);
        }

    }

    private void closeBracket() {

        skipBlank();

        if (ptr == len) {
            throw new ParseException(ParseErrorType.PATH_UNEXPECTED_END, ptr);
        }

        char c = str.charAt(ptr);

        if (c == ',') {
            // more than one selector in a segment
            throw new ParseException(ParseErrorType.PATH_UNSUPPORTED, ptr);
        }

        if (c != ']') {
            throw new ParseException(ParseErrorType.PATH_UNEXPECTED_CHAR, ptr);
        }

        ptr++;

    }

    private int index() {

        int start = ptr;
        boolean negative = false;

        if (str.charAt(ptr) == '-') {
            negative = true;
            ptr++;
        }

        if (ptr == len) {
            throw new ParseException(ParseErrorType.PATH_UNEXPECTED_END, ptr);
        }

        char c = str.charAt(ptr);
        if (!isDigit(c)) {
            throw new ParseException(ParseErrorType.PATH_BAD_INDEX, start);
        }

        // int = "0" / (["-"] DIGIT1 *DIGIT)
        if (c == '0') {
            ptr++;
            if (negative || (ptr < len && isDigit(str.charAt(ptr)))) {
                throw new ParseException(ParseErrorType.PATH_BAD_INDEX, start);
            }
            return 0;
        }

        long value = 0;
        boolean tooBig = false;
        while (ptr < len && isDigit(str.charAt(ptr))) {
            if (!tooBig) {
                value = value * 10 + (str.charAt(ptr) - '0');
                // Integer.MIN_VALUE is the one value whose magnitude exceeds Integer.MAX_VALUE
                if (value > (long) Integer.MAX_VALUE + 1) {
                    tooBig = true;
                }
            }
            ptr++;
        }

        if (negative) {
            value = -value;
        }

        if (tooBig || value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new ParseException(ParseErrorType.PATH_INDEX_RANGE, start);
        }

        return (int) value;

    }

    private String stringLiteral(char quote) {

        ptr++; // skip opening quote
        StringBuilder sb = new StringBuilder();

        while (true) {

            if (ptr == len) {
                throw new ParseException(ParseErrorType.PATH_UNEXPECTED_END, ptr);
            }

            char c = str.charAt(ptr);

            if (c == quote) {
                ptr++;
                return sb.toString();
            }

            if (c == '\\') {
                escape(quote, sb);
                continue;
            }

            int cp = str.codePointAt(ptr);
            if (cp < 0x20 || isSurrogate(cp)) {
                throw new ParseException(ParseErrorType.PATH_BAD_STRING_CHAR, ptr);
            }
            sb.appendCodePoint(cp);
            ptr += Character.charCount(cp);

        }

    }

    private void escape(char quote, StringBuilder sb) {

        int esc = ptr++;

        if (ptr == len) {
            throw new ParseException(ParseErrorType.PATH_UNEXPECTED_END, ptr);
        }

        char c = str.charAt(ptr++);

        if (c == quote) {
            sb.append(c);
            return;
        }

        switch (c) {
            case 'b': sb.append('\b'); return;
            case 'f': sb.append('\f'); return;
            case 'n': sb.append('\n'); return;
            case 'r': sb.append('\r'); return;
            case 't': sb.append('\t'); return;
            case '/': sb.append('/'); return;
            case '\\': sb.append('\\'); return;
            case 'u': break;
            default:
                throw new ParseException(ParseErrorType.PATH_BAD_ESCAPE, esc);
        }

        char high = hexChar(esc);

        if (Character.isLowSurrogate(high)) {
            throw new ParseException(ParseErrorType.PATH_BAD_ESCAPE, esc);
        }

        if (!Character.isHighSurrogate(high)) {
            sb.append(high);
            return;
        }

        // hexchar = non-surrogate / (high-surrogate "\" %x75 low-surrogate)
        if (ptr + 1 >= len || str.charAt(ptr) != '\\' || str.charAt(ptr + 1) != 'u') {
            throw new ParseException(ParseErrorType.PATH_BAD_ESCAPE, esc);
        }

        ptr += 2;
        char low = hexChar(esc);
        if (!Character.isLowSurrogate(low)) {
            throw new ParseException(ParseErrorType.PATH_BAD_ESCAPE, esc);
        }

        sb.append(high).append(low);

    }

    private char hexChar(int esc) {

        if (ptr + 4 > len) {
            throw new ParseException(ParseErrorType.PATH_BAD_ESCAPE, esc);
        }

        int value = 0;
        for (int i = 0; i < 4; i++) {
            int d = Character.digit(str.charAt(ptr + i), 16);
            // Character.digit() also accepts non-ASCII digits, which HEXDIG does not
            if (d < 0 || str.charAt(ptr + i) > 'f') {
                throw new ParseException(ParseErrorType.PATH_BAD_ESCAPE, esc);
            }
            value = value * 16 + d;
        }

        ptr += 4;
        return (char) value;

    }

    private void skipBlank() {
        while (ptr < len) {
            char c = str.charAt(ptr);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') { break; }
            ptr++;
        }
    }

    private static boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isSurrogate(int cp) {
        return cp >= 0xD800 && cp <= 0xDFFF;
    }

    // name-first = ALPHA / "_" / %x80-D7FF / %xE000-10FFFF
    private static boolean isNameFirst(int cp) {
        return (cp >= 'A' && cp <= 'Z') || (cp >= 'a' && cp <= 'z') || cp == '_' ||
                (cp >= 0x80 && !isSurrogate(cp));
    }

}
