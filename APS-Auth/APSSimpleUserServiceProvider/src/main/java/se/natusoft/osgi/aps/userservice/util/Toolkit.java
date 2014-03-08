/* 
 * 
 * PROJECT
 *     Name
 *         APS Simple User Service Provider
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         Provides an implementation of APSSimpleUserService backed by a database.
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
 *         2012-08-24: Created!
 *         
 */
package se.natusoft.osgi.aps.userservice.util;

import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class provides a toolkit of static methods.
 */
public class Toolkit {

    // Make non instantiable.
    private Toolkit() {};

    /**
     * Parses the passed string and creates and returns its information as Properties.
     *
     * @param str The string to parse. Should have been created by propertiesToString().
     *
     * @return A populated Dictionary.
     */
    public static Properties stringToProperties(String str) {
        Properties props = new Properties();
        StringTokenizer tokenizer =
                new StringTokenizer(str != null ? str : "", "§");
        while (tokenizer.hasMoreTokens()) {
            String prop = tokenizer.nextToken();
            String[] keyValue = prop.split("»");
            props.put(keyValue[0], keyValue[1]);
        }

        return props;
    }

    /**
     * Takes all the key, value pairs in the dictionary and converts it to a parseable string that can easily be stored.
     *
     * @param props The properties to convert to string.
     *
     * @return The dictionary data in string format.
     */
    public static String propertiesToString(Properties props) {
        Enumeration keyEnumeration = props.keys();
        StringBuilder sb = new StringBuilder();
        while(keyEnumeration.hasMoreElements()) {
            Object key = keyEnumeration.nextElement();

            sb.append(key.toString());
            sb.append('»');
            sb.append(props.get(key).toString());

            if (keyEnumeration.hasMoreElements()) {
                sb.append('§');
            }
        }

        return sb.toString();
    }
}
