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
 *         2011-07-22: Created!
 *
 */
package se.natusoft.osgi.aps.exceptions;

/**
 * A very lonely exception.
 *
 * I have finally come to an agreement with many others who I at first thought crazy that checked exceptions
 * are kind of evil. :-). It took me some time, but I finally got there. Just like JEE which I used to strongly
 * defend, but not so much anymore.
 *
 * Anyhow, this exception is here just for the heck of it! All other APS exceptions inherits from APSRuntimeException,
 * which makes them far more flexible, can be handled at the correct place, and no tons of catch-wrap-throw. I do
 * however document them in JavaDoc even if they are runtime exceptions.
 */
public class APSException extends Exception {

    /**
     * Creates a new _APSException_ instance.
     *
     * @param message The exception message.
     */
    public APSException(String message) {
        super(message);
    }

    /**
     * Creates a new _APSException_ instance.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSException(String message, Throwable cause) {
        super(message, cause);
    }
}
