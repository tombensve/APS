package se.natusoft.osgi.aps.tools;

import se.natusoft.osgi.aps.tools.tuples.Tuple2;

import java.util.LinkedList;
import java.util.List;

/**
 * This is intended to be used as a base class.
 *
 * Currently only thread exception support is available but more features can be added in the future.
 */
public class APSObject {

    //
    // Private Members
    //

    /** Another APSObject to delegate to. */
    private APSObject delegate = null;

    /** A list of listeners to received submitted exceptions. */
    private List<Tuple2<Class<?>, ExceptionListener>> exceptionListeners = new LinkedList<>();

    //
    // Methods
    //

    /**
     * Submits an exception. The idea here is to be able to submit exceptions from within threads that
     * then results in callbacks in a higher level class where they are handled, most probably with logging.
     *
     * This allows a thread to submit an exception with a stack trace which can then be handled at the same
     * place a thrown exception would be handled.
     *
     * Do note that the callback is done on the submitting thread!
     *
     * @param t The exception to submit.
     */
    @SuppressWarnings("unchecked")
    protected synchronized void submit(Throwable t) {
        if (this.delegate != null) {
            this.delegate.submit(t);
        }
        else {
            for (Tuple2<Class<?>, ExceptionListener> listenerEntry : this.exceptionListeners) {
                if (listenerEntry.t1.isAssignableFrom(t.getClass())) {
                    listenerEntry.t2.exceptionReceived(t);
                }
            }
        }
    }

    /**
     * Delegates to another APSObject instance.
     *
     * @param delegate The APSObject instance to delegate to.
     */
    public void setDelegate(APSObject delegate) {
        this.delegate = delegate;
    }

    /**
     * Adds a listener to receive submitted exceptions.
     *
     * @param exceptionClass Only listen to this exception class and subclasses.
     * @param exceptionListener The listener to call.
     */
    public void addExceptionListener(Class<?> exceptionClass, ExceptionListener<? extends Throwable> exceptionListener) {
        if (this.delegate != null) {
            this.delegate.addExceptionListener(exceptionClass, exceptionListener);
        }
        else {
            this.exceptionListeners.add(new Tuple2<Class<?>, ExceptionListener>(exceptionClass, exceptionListener));
        }
    }

    /**
     * This interface must be implemented to receive submitted exceptions.
     *
     * @param <E> The base exception type of the listener.
     */
    public static interface ExceptionListener<E extends Throwable> {

        /**
         * Receives an exception.
         *
         * @param e The received exception.
         */
        void exceptionReceived(E e);
    }
}
