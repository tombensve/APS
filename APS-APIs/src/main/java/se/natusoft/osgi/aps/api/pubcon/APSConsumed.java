package se.natusoft.osgi.aps.api.pubcon;

/**
 * This is a wrapper of consumed data.
 *
 * @param <Consumed> The actual type consumed.
 */
public interface APSConsumed<Consumed> {

    /**
     * @return The actual consumed instance.
     */
    Consumed get();

    /**
     * Tells the data owner that the consumer no longer uses the data.
     */
    void release();

    /**
     * A default base implementation.
     *
     * @param <Consumed> The consumed type.
     */
    abstract class Provider<Consumed> implements APSConsumed<Consumed> {

        private Consumed consumed;

        public Provider(Consumed consumed) {
            this.consumed = consumed;
        }

        public Consumed get() {
            return this.consumed;
        }
    }
}
