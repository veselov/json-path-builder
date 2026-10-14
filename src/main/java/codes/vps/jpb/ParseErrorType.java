package codes.vps.jpb;

public enum ParseErrorType {

    PTR_NO_REF_TOKEN_START("No forward slash before pointer start"),
    PTR_STRAY_TILDE("Stray tilde sign"),
    PATH_NO_ROOT("Path does not start with the root identifier '$'"),
    PATH_UNEXPECTED_CHAR("Unexpected character"),
    PATH_UNEXPECTED_END("Unexpected end of path"),
    PATH_UNSUPPORTED("Unsupported construct (descendant segment, slice, filter, or more than one selector)"),
    PATH_BAD_STRING_CHAR("Character must be escaped in a string literal"),
    PATH_BAD_ESCAPE("Invalid escape sequence"),
    PATH_BAD_INDEX("Invalid index"),
    PATH_INDEX_RANGE("Index out of the supported range");

    final String message;
    ParseErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
