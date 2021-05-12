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
package se.natusoft.aps.util;

import se.natusoft.docutations.NotNull;

import java.util.concurrent.*;

/**
 * This creates two ExecutorServices, one that have max processor cores * 2 threads called
 * parallel and one with only one thread called sequential.
 * <p>
 * This provides a static API that will create one common instance on first use.
 */
public class APSExecutor {

    //
    // Private Members
    //

    /** This have a thread pool taking the number of cores in consideration. */
    private ExecutorService parallelExecutor;

    /** Single thread executor for handlers. */
    private ExecutorService sequentialExecutor;

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
     * Submits a job for execution on the thread pool based on number of processor cores.
     *
     * @param job The job to submit.
     */
    public static void concurrent( @NotNull Runnable job ) {
        get()._concurrent( job );
    }

    /**
     * Submits a handler to the sequential thread.
     *
     * @param job The job to push to the sequential thread pool.
     */
    public static void sequential( @NotNull Runnable job) {
        get()._sequential( job );
    }

    public static void shutdown() {
        get()._shutdown();
    }

    //
    // Constructor
    //

    /**
     * Creates a new APSExecutor.
     */
    private APSExecutor() {
        this.parallelExecutor = // Creates a cached thread pool, but with better values than Executors provides!
                new ThreadPoolExecutor(
                        0,
                        Runtime.getRuntime().availableProcessors(),
                        30L,
                        TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(),
                        new APSThreadFactory( "aps-executor-" )
                );

        this.sequentialExecutor = Executors.newSingleThreadExecutor( new ThreadFactory() {
            @Override
            public Thread newThread( Runnable r ) {
                //noinspection InstantiatingAThreadWithDefaultRunMethod
                return new Thread("aps-handler-thread" );
            }
        } );
    }

    //
    // Methods
    //

    private void _shutdown() {
        this.parallelExecutor.shutdownNow();
        this.sequentialExecutor.shutdown();
        apsExecInst = null;
    }

    private void _concurrent( @NotNull Runnable job) {
        this.parallelExecutor.submit( job );
    }

    private void _sequential( @NotNull Runnable job) {
        this.sequentialExecutor.submit( job );
    }

    /**
     * For temporary internal use only!!
     */
    public static ExecutorService _internal_get_executor() {
        return get().parallelExecutor;
    }
}
