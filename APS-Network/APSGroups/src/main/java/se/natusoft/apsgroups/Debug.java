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
package se.natusoft.apsgroups;

/**
 * Static utility for displaying debug output.
 */
public class Debug {
    public static final int DEBUG_NONE = 0;
    public static final int DEBUG_NORMAL = 1;
    public static final int DEBUG_HIGH = 2;

    public static int level = DEBUG_NORMAL;

    public static void print(String str) {
        if (level >= DEBUG_NORMAL) System.out.print(str);
    }

    public static void println(String str) {
        if (level >= DEBUG_NORMAL) System.out.println(str);
    }

    public static void print2(String str) {
        if (level >= DEBUG_HIGH) System.out.print(str);
    }

    public static void println2(String str) {
        if (level >= DEBUG_HIGH) System.out.println(str);
    }
}
