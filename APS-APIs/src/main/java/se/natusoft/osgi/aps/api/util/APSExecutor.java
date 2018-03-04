package se.natusoft.osgi.aps.api.util;

import se.natusoft.docutations.NotNull;
import se.natusoft.osgi.aps.model.APSHandler;
import se.natusoft.osgi.aps.model.APSResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This creates an ExecutorService with the same amount of threads as there are processor cores in the machine.
 * <p>
 * Submitted jobs are forwarded to the ExecutorService.
 */
public class APSExecutor {

    //
    // Private Members
    //

    /** This have a thread pool matching number of cores */
    private ExecutorService executor;

    /** Holds the singleton instance. */
    private static APSExecutor apsExecInst;

    //
    // Constructors
    //

    /**
     * @return The singleton instance.
     */
    private static APSExecutor get() {
        if (apsExecInst == null) {
            apsExecInst = new APSExecutor();
        }
        return apsExecInst;
    }

    /**
     * Submits a job for execution on a thread pool.
     *
     * @param job The job to submit.
     */
    public static void submit(@NotNull APSHandler<?> job) {
        get()._submit(job);
    }

    /**
     * Submits a job for execution on a thread pool.
     * <p>
     * Do note that as a special feature an APSValue is created and passed to both handlers. This means that it is
     * possible to pass a value from one to the other.
     *
     * @param job            The job to submit.
     * @param jobDoneHandler The handler to call when the job have finished execution. It will supply a success or fail result.
     *                       fail will only happen if the job threw an exception.
     */
    public static void submit(@NotNull APSHandler<?> job, @NotNull APSHandler<APSResult> jobDoneHandler) {
        get()._submit(job, jobDoneHandler);
    }

    public static void shutdown() {
        get()._shutdown();
    }

    //
    // Constructor
    //

    /**
     * Creates a new APSPlatformServiceProvider instance.
     */
    private APSExecutor() {
        this.executor =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new APSThreadFactory("aps-executor-"));
    }

    //
    // Methods
    //

    private void _shutdown() {
        this.executor.shutdownNow();
        apsExecInst = null;
    }

    /**
     * Submits a job for execution on a thread pool.
     *
     * @param job The job to submit.
     */
    private void _submit(@NotNull APSHandler<?> job) {
        this.executor.submit(() -> {
            try {
                job.handle(null);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

        });
    }

    /**
     * Submits a job for execution on a thread pool.
     * <p>
     * Do note that as a special feature an APSValue is created and passed to both handlers. This means that it is
     * possible to pass a value from one to the other.
     *
     * @param job            The job to submit.
     * @param jobDoneHandler The handler to call when the job have finished execution. It will supply a success or fail result.
     *                       fail will only happen if the job threw an exception.
     */
    private void _submit(@NotNull APSHandler<?> job, @NotNull APSHandler<APSResult> jobDoneHandler) {
        this.executor.submit(() -> {
            try {
                job.handle(null);
                jobDoneHandler.handle(APSResult.success(null));
            } catch (Exception e) {
                jobDoneHandler.handle(APSResult.failure(e));
            }
        });
    }
}
