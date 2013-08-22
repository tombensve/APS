package se.natusoft.osgi.aps.tools.tuples;

/**
 * A tuple with three values.
 */
public class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {

    public T3 t3;

    public Tuple3() {}

    public Tuple3(T1 t1, T2 t2, T3 t3) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
    }
}