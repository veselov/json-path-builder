package codes.vps.jpb.el;

import codes.vps.jpb.PathElement;

public class Root extends PathElement {
    @Override
    protected void render(StringBuilder sb) {
        sb.append("$");
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}
