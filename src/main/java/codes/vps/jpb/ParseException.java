package codes.vps.jpb;

public class ParseException extends RuntimeException {

    protected final ParseErrorType type;
    protected final int position;

    public ParseException(ParseErrorType type, int position) {
        super(type.getMessage());
        this.position = position;
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public ParseErrorType getType() {
        return type;
    }

}
