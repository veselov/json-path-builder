package codes.vps.jpb.el;

import codes.vps.jpb.PathElement;

import java.util.Objects;

public class Property extends PathElement {

    private final String p;
    private final boolean needEscape;

    public Property(String p) {

        Objects.requireNonNull(p);

        // it's really unclear what is considered a special character
        // we can assume that that'd be all characters that used for other
        // "operators", but ugh. We are going to say that it must not start
        // with a decimal digit, and must only contain alphanumeric characters.

        // a very special case
        if (p.isEmpty()) {
            this.p = "";
            needEscape = true;
            return;
        }

        boolean needEscape = false;

        StringBuilder use = new StringBuilder();
        boolean fc = true;

        for (char c : p.toCharArray()) {

            if (!needEscape) {
                if (fc) {
                    fc = false;
                    if (!Character.isLetter(c)) {
                        needEscape = true;
                    }
                } else {
                    if (!Character.isLetterOrDigit(c)) {
                        needEscape = true;
                    }
                }
            }

            if (c == '\'' || c == '\\') {
                use.append('\\');
            }

            // RFC-9535 string literals can not contain control characters, they must be escaped
            if (c < 0x20) {
                switch (c) {
                    case '\b': use.append("\\b"); break;
                    case '\f': use.append("\\f"); break;
                    case '\n': use.append("\\n"); break;
                    case '\r': use.append("\\r"); break;
                    case '\t': use.append("\\t"); break;
                    default:
                        String hex = Integer.toHexString(c);
                        use.append("\\u");
                        for (int i = hex.length(); i < 4; i++) { use.append('0'); }
                        use.append(hex);
                }
                continue;
            }

            use.append(c);

        }

        this.p = use.toString();
        this.needEscape = needEscape;

    }

    @Override
    protected void render(StringBuilder sb) {

        if (needEscape) {
            sb.append("['");
            sb.append(p);
            sb.append("']");
        } else {
            sb.append('.');
            sb.append(p);
        }

    }
}
