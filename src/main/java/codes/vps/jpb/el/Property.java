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
