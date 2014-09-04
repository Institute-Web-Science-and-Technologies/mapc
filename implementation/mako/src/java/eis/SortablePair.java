package eis;

/**
 * This class allows sortable pairs where an {@link Integer} {@code key} is
 * associated with an {@link Object} which allows sorting by said {@code key}.
 * 
 * @author Michael Ruster
 */
public class SortablePair<O> implements Comparable<SortablePair<O>> {
    public final Integer key;
    public final O o;

    public SortablePair(Integer distance, O o) {
        this.key = distance;
        this.o = o;
    }

    public Integer getKey() {
        return key;
    }

    public O getO() {
        return o;
    }

    /**
     * This class has a natural ordering consistent with {@code equals}. It
     * primarily compares the {@code key}. If both keys are identical, it will
     * be assumed that {@code o} is of higher value than {@code this}.
     */
    @Override
    public int compareTo(SortablePair<O> o) {
        int order = this.key.compareTo(o.key);
        if (order == 0) {
            if (!this.equals(o)) {
                order = -1;
            }
        }
        return order;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((o == null) ? 0 : o.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SortablePair)) {
            return false;
        }
        SortablePair<O> other = (SortablePair<O>) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (o == null) {
            if (other.o != null) {
                return false;
            }
        } else if (!o.equals(other.o)) {
            return false;
        }
        return true;
    }
}
