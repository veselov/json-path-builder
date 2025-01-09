package codes.vps.jpb;

public abstract class AnyElement {

    protected abstract void render(StringBuilder sb);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        render(sb);
        return sb.toString();
    }

}
