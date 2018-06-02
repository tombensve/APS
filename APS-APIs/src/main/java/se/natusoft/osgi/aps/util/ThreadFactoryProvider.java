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

import java.util.concurrent.ThreadFactory;

/**
 * Provides a thread factory that also sets names on the threads.
 */
public class ThreadFactoryProvider implements ThreadFactory {
    //
    // Private Members
    //

    /** The basename of the created thread. */
    private String baseName;

    /** An instance number to make name unique. */
    private int inst = 0;

    //
    // Constructors
    //

    /**
     * Creates a new ThreadFactoryProvider.
     *
     * @param baseName The base thread name.
     */
    public ThreadFactoryProvider(String baseName) {
        this.baseName = baseName;
    }

    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(this.baseName + "-" + this.inst);
        if (this.inst == Integer.MAX_VALUE) {
            this.inst = 0;
        }
        else {
            this.inst++;
        }

        return thread;
    }
}
