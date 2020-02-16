/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *
 */
package se.natusoft.osgi.aps.util;

import se.natusoft.docutations.NotNull;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;

import java.util.concurrent.*;

/**
 * This creates an ExecutorService with the amount of threads as there are processor cores in the machine * 4.
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
    // Static Methods
    //

    /**
     * @return The singleton instance.
     */
    private static APSExecutor get() {
        if ( apsExecInst == null ) {
            apsExecInst = new APSExecutor();
        }
        return apsExecInst;
    }

    /**
     * Submits a job for execution on a thread pool.
     *
     * @param job The job to submit.
     */
    public static void submit( @NotNull APSHandler<?> job ) {
        get()._submit( job );
    }

    /**
     * Alias for submit.
     *
     * @param job Job to run.
     */
    public static void parallel( @NotNull APSHandler<?> job) {
        submit( job );
    }

    /**
     * Submits a job for execution on a thread pool.
     * <p>
     * Do note that as a special feature an APSValue is created and passed to both handlers. This means that it is
     * possible to pass a value from one to the other.
     *
     * @param job            The job to submit.
     * @param jobDoneHandler The handler to call when the job have finished execution. It will supply a success or fail
     *                       result.
     *                       fail will only happen if the job threw an exception.
     */
    public static void submit( @NotNull APSHandler<?> job, @NotNull APSHandler<APSResult<?>> jobDoneHandler ) {
        get()._submit( job, jobDoneHandler );
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
        this.executor = // Creates a cached thread pool, but with better values than Executors provides!
                new ThreadPoolExecutor(
                        0,
                        Runtime.getRuntime().availableProcessors() * 2, // (*1)
                        30L,
                        TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(),
                        new APSThreadFactory( "aps-executor-" )
                );
    }
    // *1: Why "*2" ? Well, just for the heck of it! I can't really say. These are backend jobs and have no
    // gain in faked parallelism since no one will see it. But still in some way I cannot explain it does
    // feel right to provide some. Possibly human emotional stupidity.

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
    private void _submit( @NotNull APSHandler<?> job ) {
        this.executor.submit( () -> {
            try {
                job.handle( null );
            } catch ( Exception e ) {
                e.printStackTrace( System.err );
            }

        } );
    }

    /**
     * Submits a job for execution on a thread pool.
     * <p>
     * Do note that as a special feature an APSValue is created and passed to both handlers. This means that it is
     * possible to pass a value from one to the other.
     *
     * @param job            The job to submit.
     * @param jobDoneHandler The handler to call when the job have finished execution. It will supply a success or fail
     *                       result.
     *                       fail will only happen if the job threw an exception.
     */
    private void _submit( @NotNull APSHandler<?> job, @NotNull APSHandler<APSResult<?>> jobDoneHandler ) {
        this.executor.submit( () -> {
            try {
                job.handle( null );
                jobDoneHandler.handle( APSResult.success( null ) );
            } catch ( Exception e ) {
                jobDoneHandler.handle( APSResult.failure( e ) );
            }
        } );
    }

    /**
     * Use this only if you are really really stupid!
     */
    public static ExecutorService _internal_get_executor() {
        return get().executor;
    }
}
