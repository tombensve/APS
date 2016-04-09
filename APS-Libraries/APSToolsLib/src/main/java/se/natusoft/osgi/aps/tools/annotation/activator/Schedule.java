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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-08-19: Created!
 *
 */
package se.natusoft.osgi.aps.tools.annotation.activator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Fields annotated with this should be of type Runnable, and will be scheduled
 * on an ServiceExecutor whose name matches the name specified in "on=...".
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Schedule {

    /**
     * The defined executor service to schedule this on. This should be the name of it. If left blank an internal
     * ScheduledExecutorService will be used.
     */
    String on() default "";

    /** The amount of time to wait for the (first) execution. */
    long delay();

    /** If specified how long to wait between runs. */
    long repeat();

    /** The time unit used for the above values. Defaults to seconds. */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /** Possibility to affect the size of the thread pool when such is created internally for this (on="..." not provided!). */
    int poolSize() default 2;
}
