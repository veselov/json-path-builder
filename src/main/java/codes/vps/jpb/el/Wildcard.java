package codes.vps.jpb.el;

import codes.vps.jpb.PathElement;

public class Wildcard extends PathElement {
    @Override
    protected void render(StringBuilder sb) {
        sb.append(".*");
    }
}
