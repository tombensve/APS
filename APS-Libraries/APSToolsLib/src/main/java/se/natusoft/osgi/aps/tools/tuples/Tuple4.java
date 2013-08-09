package se.natusoft.osgi.aps.tools.tuples;

/**
 * A tuple with four values.
 */
public class Tuple4<T1, T2, T3, T4> {

    public T1 t1;
    public T2 t2;
    public T3 t3;
    public T4 t4;

    public Tuple4() {}

    public Tuple4(T1 t1, T2 t2, T3 t3, T4 t4) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
    }
}
