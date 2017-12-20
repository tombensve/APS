/*
 *
 * PROJECT
 *     Name
 *         APS Tools Library
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
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
 *         2017-01-05: Created!
 *
 */
package se.natusoft.osgi.aps.tools.util;

import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Date;

/**
 * Utility to track the intensiveness of exceptions and limit the number of such that can occur.
 */
public class IntensiveExceptionsGuard<E extends Exception> {

    private APSLogger logger;
    private long timeBetweenConsecutiveExceptions = 500;
    private int maxExceptions = 10;
    private long lastException = 0;
    int noExceptions = 0;
    boolean failed = false;

    //
    // Constructors
    //

    /**
     * Creates a new ExceptionGuard instance.
     *
     * @param logger The APSLogger to log to. Can be null for no logging.
     * @param timeBetweenConsecutiveExceptions The sensitivity, how often/intensely should exceptions occur to react on them. This is
     *                                         in milliseconds!
     * @param maxExceptions The max number of consecutive exceptions before triggering on the problem.
     */
    public IntensiveExceptionsGuard(APSLogger logger, long timeBetweenConsecutiveExceptions, int maxExceptions) {
        this.logger = logger;
        this.timeBetweenConsecutiveExceptions = timeBetweenConsecutiveExceptions;
        this.maxExceptions = maxExceptions;
    }

    /**
     * Creates a new ExceptionGuard with default configold.
     *
     * @param logger The APSLogger to log to. Can be null for no logging, but the no args constructor would be better then.
     */
    public IntensiveExceptionsGuard(APSLogger logger) {
        this.logger = logger;
    }

    /**
     * Creates a new ExceptionGuard with default configold.
     */
    public IntensiveExceptionsGuard() {}

    //
    // Methods
    //

    /**
     * This should be called for an exception within a loop.
     *
     * @param e The occurred exception
     * @return true if no problem, false on problem. If an APSLogger has been supplied an error will be logged to it.
     */
    public synchronized boolean checkException(E e) {
        long now = new Date().getTime();
        if (now - this.lastException < this.timeBetweenConsecutiveExceptions) {
            ++this.noExceptions;
            if (this.noExceptions >= this.maxExceptions) {
                if (this.logger != null) {
                    this.logger.error("Intensive exceptions detected!", e);
                }
                this.failed = true;
                return false;
            }
        }
        else {
            clearCount();
        }

        this.lastException = now;

        return true;
    }

    /**
     * Clears the count of consecutive exceptions. This should be called when something have gone OK and not thrown exception.
     *
     * Note: This also clears the failed flag.
     */
    public synchronized void clearCount() {
        this.noExceptions = 0;
        this.failed = false;
    }

    /**
     * If checkException() has returned false then a call to this will return true. This is useful where you
     * pass the guard to some other code and want to check it when that code returns.
     *
     * Do note: A clearCount() call will clear the failed state.
     */
    public synchronized boolean isFailed() {
        return this.failed;
    }
}
