package codes.vps.jpb;

public enum ParseErrorType {

    PTR_NO_REF_TOKEN_START("No forward slash before pointer start"),
    PTR_STRAY_TILDE("Stray tilde sign");

    final String message;
    ParseErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
