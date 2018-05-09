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
package se.natusoft.osgi.aps.activator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutorService;

/**
 * This annotation indicates that the annotated field is an ExecutorService and the type must
 * actually be of type ExecutorService or ScheduledExecutorService. Fields annotated with this
 * must also be annotated with @Managed.
 *
 * This only works when APSActivator is used as bundle activator!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExecutorSvc {

    enum ExecutorType {
        FixedSize,
        WorkStealing,
        Single,
        Cached,
        Scheduled,
        SingleScheduled
    }

    /** This is loosely the number of concurrent threads. */
    int parallelism() default 10;

    /** The type of ExecutorService wanted. */
    ExecutorType type() default ExecutorType.FixedSize;

    /** If true the created ExecutorService will be wrapped with a delegate that disallows configuration. */
    boolean unConfigurable() default false;

    /** Provides base name for threads. */
    String name() default "-";
}
