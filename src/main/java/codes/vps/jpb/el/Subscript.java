package codes.vps.jpb.el;

import codes.vps.jpb.PathElement;

public class Subscript extends PathElement {

    private final int idx;

    public Subscript(int idx) {
        this.idx = idx;
    }

    @Override
    protected void render(StringBuilder sb) {
        sb.append("[").append(idx).append("]");
    }

}
