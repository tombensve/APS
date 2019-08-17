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
package se.natusoft.osgi.aps.api.core.platform.service;

import se.natusoft.docutations.NotNull;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;

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
