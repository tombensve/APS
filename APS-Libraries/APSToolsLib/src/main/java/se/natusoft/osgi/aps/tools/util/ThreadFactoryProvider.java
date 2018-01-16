package se.natusoft.osgi.aps.tools.util;

import java.util.concurrent.ThreadFactory;

/**
 * Provides a thread factory that also sets names on the threads.
 */
public class ThreadFactoryProvider implements ThreadFactory {
    //
    // Private Members
    //

    /** The basename of the created thread. */
    private String baseName;

    /** An instance number to make name unique. */
    private int inst = 0;

    //
    // Constructors
    //

    /**
     * Creates a new ThreadFactoryProvider.
     *
     * @param baseName The base thread name.
     */
    public ThreadFactoryProvider(String baseName) {
        this.baseName = baseName;
    }

    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(this.baseName + "-" + this.inst);
        if (this.inst == Integer.MAX_VALUE) {
            this.inst = 0;
        }
        else {
            this.inst++;
        }

        return thread;
    }
}
