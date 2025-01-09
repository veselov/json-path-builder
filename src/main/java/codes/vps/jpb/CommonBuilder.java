package codes.vps.jpb;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class CommonBuilder<E extends AnyElement, I extends CommonBuilder<E,I>> implements Iterable<E> {

    protected final LinkedList<E> path = new LinkedList<>();
    protected int size;

    static String toString(List<? extends AnyElement> path) {

        StringBuilder sb = new StringBuilder();
        path.forEach(p->p.render(sb));
        return sb.toString();

    }

    @Override
    public String toString() {
        return toString(path);
    }

    protected I addElement(E e) {

        path.add(e);
        size++;
        //noinspection unchecked
        return (I)this;

    }

    public I clip() {
        return clip(1);
    }

    public I clip(int l) {

        if (size - l < 1) {
            throw new IllegalArgumentException("Can not clip more than "+(size-1)+" elements");
        }

        path.subList(size - l, size).clear();
        size -= l;

        //noinspection unchecked
        return (I)this;

    }

    public int size() {
        return path.size();
    }

    E elementAt(int index) {
        return path.get(index);
    }

    @Override
    public Iterator<E> iterator() {
        return path.iterator();
    }
}
