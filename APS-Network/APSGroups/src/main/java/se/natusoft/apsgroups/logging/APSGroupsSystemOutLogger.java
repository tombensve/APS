/* 
 * 
 * PROJECT
 *     Name
 *         APS APSNetworkGroups
 *     
 *     Code Version
 *         0.9.0
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.apsgroups.logging;

/**
 * Provides a very simple APSGroupsLogger implementation that logs to System.out.
 */
public class APSGroupsSystemOutLogger implements APSGroupsLogger {

    @Override
    public void debug(String message) {
        System.out.println("DEBUG: " + message);
    }

    @Override
    public void debug(String message, Throwable exception) {
        debug(message);
        exception.printStackTrace();
    }

    @Override
    public void info(String message) {
        System.out.println("INFO: " + message);
    }

    @Override
    public void info(String message, Throwable exception) {
        info(message);
        exception.printStackTrace();
    }

    @Override
    public void warn(String message) {
        System.err.println("WARN: " + message);
    }

    @Override
    public void warn(String message, Throwable exception) {
        warn(message);
        exception.printStackTrace();
    }

    @Override
    public void error(String message) {
        System.err.println("ERROR: " + message);
    }

    @Override
    public void error(String message, Throwable exception) {
        error(message);
        exception.printStackTrace();
    }
}
