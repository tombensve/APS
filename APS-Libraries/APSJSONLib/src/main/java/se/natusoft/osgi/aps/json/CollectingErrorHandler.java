/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Library
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a JSON parser and creator. Please note that this bundle has no dependencies to any
 *         other APS bundle! It can be used as is without APS in any Java application and OSGi container.
 *         The reason for this is that I do use it elsewhere and don't want to keep 2 different copies of
 *         the code. OSGi wise this is a library. All packages are exported and no activator nor services
 *         are provided.
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
 *         2019-08-17: Created!
 *         
 */
package se.natusoft.osgi.aps.json;

import se.natusoft.osgi.aps.json.JSONErrorHandler;
import se.natusoft.osgi.aps.exceptions.APSIOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility implementation of JSONErrorHandler.
 */
public class CollectingErrorHandler implements JSONErrorHandler {
    //
    // Private Members
    //

    private List<String> messages = new LinkedList<>();

    private boolean printWarnings = false;

    //
    // Constructors
    //

    public CollectingErrorHandler() {}

    /**
     * @param printWarnings If true warnings will be printed to stderr.
     */
    public CollectingErrorHandler(boolean printWarnings) {
        this.printWarnings = printWarnings;
    }

    //
    // Methods
    //

    /**
     * Warns about something.
     *
     * @param message The warning message.
     */
    @Override
    public void warning(String message) {
        this.messages.add(message);
        if (this.printWarnings) {
            System.err.println(message);
        }
    }

    /**
     * Indicate failure.
     *
     * @param message The failure message.
     * @param cause   The cause of the failure. Can be null!
     * @throws RuntimeException This method must throw a RuntimeException.
     */
    @Override
    public void fail(String message, Throwable cause) throws RuntimeException {
        StringBuilder sb = new StringBuilder();
        sb.append("Message:\n");
        sb.append(message);
        sb.append("\nException:\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        pw.flush();
        sb.append(sw.toString());
        this.messages.add(sb.toString());

        throw new APSIOException(message, cause);
    }

    /**
     * @return true if there are any messages.
     */
    public boolean hasMessages() {
        return !this.messages.isEmpty();
    }

    /**
     * @return All messages as one string.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message);
            sb.append("\n");
        }
        return sb.toString();
    }
}
