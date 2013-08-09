package se.natusoft.osgi.aps.tools.tuples;

/**
 * A tuple with one value.
 */
public class Tuple<T1> {

    public T1 t1;

    public Tuple() {}

    public Tuple(T1 t1) {
        this.t1 = t1;
    }
}
