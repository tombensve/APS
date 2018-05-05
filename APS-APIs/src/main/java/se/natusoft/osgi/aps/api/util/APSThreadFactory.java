package se.natusoft.osgi.aps.api.util;

import java.util.concurrent.ThreadFactory;

public class APSThreadFactory implements ThreadFactory {

    //
    // Private Members
    //

    /** The base of the name given to threads. A unique number for each created thread is added to this. */
    private String baseName;

    /** The current thread number to use. */
    private int threadNumber = 0;

    //
    // Constructors
    //

    /**
     * Creates a new ThreadFactory.
     *
     * @param baseName The base name to use for naming the created threads.
     */
    public APSThreadFactory(String baseName) {
        this.baseName = baseName;
    }

    //
    // Methods
    //

    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param runnable a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(this.baseName + threadNumber++);

        return thread;
    }
}
