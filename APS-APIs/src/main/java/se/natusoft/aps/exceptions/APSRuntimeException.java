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
package se.natusoft.aps.exceptions;

import java.util.LinkedList;
import java.util.List;

/**
 * Base exception for basically all other exceptions thrown by the APIs and services.
 */
@Deprecated // All APS exceptions are now runtime exceptions!
public class APSRuntimeException extends RuntimeException {

    //
    // Private Members
    //

    /** The exception message. */
    private StringBuilder messageBuilder = new StringBuilder();

    /** Support for multiple causes for this exception. */
    private List<Throwable> causes = new LinkedList<>();

    //
    // Constructors
    //

    /**
     * Creates a new _APSRuntimeException_ instance.
     */
    public APSRuntimeException() {
        super("[This exception is deprecated! All exceptions are now runtime!] ");
    }

    /**
     * Creates a new _APSRuntimeException_ instance.
     *
     * @param message The exception message.
     */
    public APSRuntimeException(String message) {
        super("[This exception is deprecated! All exceptions are now runtime!] " + message);
        this.messageBuilder.append(message);
    }

    /**
     * Creates a new _APSRuntimeException_ instance.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSRuntimeException(String message, Throwable cause) {
        super("[This exception is deprecated! All exceptions are now runtime!] " + message, cause);
        this.messageBuilder.append(message);
        this.causes.add(cause);
    }

    //
    // Methods
    //

    /**
     * Adds text the the exception message.
     *
     * @param text The text to add.
     */
    public void addToMessage(String text) {
        this.messageBuilder.append(text);
    }

    /**
     * Returns the exception message.
     */
    @Override
    public String getMessage() {
        return this.messageBuilder.toString();
    }

    /**
     * Adds a cause to this exception.
     *
     * @param cause The cause to add.
     */
    public void addCause(Throwable cause) {
        this.causes.add(cause);
    }

    /**
     * Returns a list of causes for this exception.
     */
    public List<Throwable> getCauses() {
        return this.causes;
    }

    /**
     * Returns true if there is at least one cause exception added to this exception.
     */
    public boolean hasCauses() {
        return !this.causes.isEmpty();
    }
}
