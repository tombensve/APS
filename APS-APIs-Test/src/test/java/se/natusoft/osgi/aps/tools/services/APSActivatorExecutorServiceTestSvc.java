/*
 *
 * PROJECT
 *     Name
 *         APS APIs Tests
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         APS (Application Platform Services) - A smörgåsbord of OSGi application/platform type services intended for
 *         web applications. Currently based on Vert.x for backend and React for frontend (its own web admin apps).
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
package se.natusoft.osgi.aps.tools.services;

import se.natusoft.osgi.aps.activator.annotation.ExecutorSvc;
import se.natusoft.osgi.aps.activator.annotation.Managed;
import se.natusoft.osgi.aps.activator.annotation.APSPlatformServiceProvider;
import se.natusoft.osgi.aps.activator.annotation.Schedule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
@APSPlatformServiceProvider
public class APSActivatorExecutorServiceTestSvc implements TestService {

    @Managed
    @ExecutorSvc(parallelism = 5, type = ExecutorSvc.ExecutorType.Cached, unConfigurable = true)
    private ExecutorService fixed;

    @Managed(name = "scheduled")
    @ExecutorSvc(parallelism = 15, type = ExecutorSvc.ExecutorType.Scheduled)
    private ScheduledExecutorService sched;

    private boolean scheduledServiceRun1 = false;
    private boolean scheduledServiceRun2 = false;

    @Schedule(on = "scheduled", delay = 1, repeat = 1, timeUnit = TimeUnit.SECONDS)
    private Runnable myRunnable1 = () -> {
        System.out.println("Being run!");
        scheduledServiceRun1 = true;
    };

    // This will use an internal ScheduledExecutionService.
    @Schedule(delay = 1, repeat = 1, timeUnit = TimeUnit.SECONDS)
    private Runnable myRunnable2 = () -> {
        System.out.println("Being run 2!");
        scheduledServiceRun2 = true;
    };

    @Override
    public String getServiceInstanceInfo() throws Exception {
        Thread.sleep(2000);

        String status = "OK";

        if (this.fixed == null || this.sched == null) {
            status = "BAD";
        }

        // Verify that we have a delegate for fixed.
        if (!this.fixed.getClass().getName().equals("java.util.concurrent.Executors$DelegatedExecutorService")) {
            status = this.fixed.getClass().getName();
        }

        // Verify that we have a scheduled executor service for sched.
        if (!this.sched.getClass().getName().equals("java.util.concurrent.ScheduledThreadPoolExecutor")) {
            status = this.sched.getClass().getName();
        }

        if (!this.scheduledServiceRun1 ) {
            status = "Scheduled runnable 1 was never run!";
        }
        if (!this.scheduledServiceRun2 ) {
            status += "Scheduled runnable 2 was never run!";
        }

        return status;
    }
}
