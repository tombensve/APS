package se.natusoft.osgi.aps.api.core.platform.service;

import se.natusoft.docutations.NotNull;
import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSResult;

/**
 * This provides a thread pool for executing jobs as a service.
 */
public interface APSExecutionService {

    /**
     * Submits a job for execution on a thread pool.
     *
     * @param job The job to submit.
     */
    void submit(@NotNull APSHandler job);

    /**
     * Submits a job for execution on a thread pool.
     *
     * @param job The job to submit.
     * @param jobDoneHandler The handler to call when the job have finished execution. It will supply a success or fail result.
     *                       fail will only happen if the job threw an exception.
     */
    void submit(@NotNull APSHandler job, @NotNull APSHandler<APSResult> jobDoneHandler);

    // For Groovy

    /**
     * Does the same as submit(job), but in groovy you can do:
     *
     *     this.execSvc << { ... }
     *
     * @param job The job to submit.
     */
    void leftShift(@NotNull APSHandler job);
}
