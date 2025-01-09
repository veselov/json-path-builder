package codes.vps.jpb;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class PointerElement extends AnyElement {

    protected static final Pattern noNums = Pattern.compile("[^0-9]");

    protected final String referenceToken;
    // the spec insists on no limit to the number width.
    protected final BigInteger asIndex;
    protected final boolean endOfArray;

    public static PointerElement eoaInstance = new PointerElement(true);

    private PointerElement(boolean nonsense) {
        asIndex = null;
        endOfArray = true;
        referenceToken = "-";
    }

    public PointerElement(long n) {
        if (n < 0) { throw new IllegalArgumentException("input must be positive, got "+n); }
        referenceToken = String.valueOf(n);
        asIndex = BigInteger.valueOf(n);
        endOfArray = false;
    }

    public PointerElement(String referenceToken) {
        this.referenceToken = referenceToken;
        if ("-".equals(referenceToken)) {
            asIndex = null;
            endOfArray = true;
        } else {
            endOfArray = false;
            BigInteger b = null;
            do {
                if (referenceToken.isEmpty()) { break; }
                char c0 = referenceToken.charAt(0);
                if (c0 == '0') { break; }
                // no Character.isDigit(), the spec only allows ISO-LATIN-1 digits
                if (noNums.matcher(referenceToken).find()) { break; }
                b = new BigInteger(referenceToken);
            } while (false);
            asIndex = b;
        }
    }

    public PointerElement(BigInteger index) {
        referenceToken = index.toString();
        asIndex = index;
        endOfArray = false;
    }

    PointerElement(String val, boolean nonsense) {
        this.referenceToken = val;
        asIndex = null;
        endOfArray = false;
    }

    @Override
    protected void render(StringBuilder sb) {
        sb.append('/');
        sb.append(referenceToken
                .replace("~", "~0")
                .replace("/", "~1"));
    }

    public String getAsMember() {
        return referenceToken;
    }

    public BigInteger getAsIndex() {
        return asIndex;
    }

    public boolean isEndOfArray() {
        return endOfArray;
    }

}
