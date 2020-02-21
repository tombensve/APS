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
 * This creates an ExecutorService that have max processor cores * 2 threads. It will
 * keep each thread alive for 30 seconds of inactivity and then shut it down. New will
 * be created again if needed later.
 * <p>
 * This provides a static API that will create one common instance on first use.
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
    public static void submit( @NotNull Runnable job ) {
        get()._submit( job );
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

    private void _submit( @NotNull Runnable job) {
        this.executor.submit( job );
    }

    /**
     * For temporary internal use only!!
     */
    public static ExecutorService _internal_get_executor() {
        return get().executor;
    }
}
