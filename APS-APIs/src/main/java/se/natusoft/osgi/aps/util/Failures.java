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
 *         2017-01-05: Created!
 *
 */
package se.natusoft.osgi.aps.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * This represents a series of collected exceptions.
 */
public class Failures extends RuntimeException {

    //
    // Private Members
    //

    /**
     * The collected exceptions.
     */
    private List<Exception> exceptionList = new LinkedList<>();

    //
    // Methods
    //

    /**
     * Returns true if there are any exceptions in this object.
     */
    public boolean hasFailures() {
        if (!this.exceptionList.isEmpty()) {
            System.err.println("Following errors occured:");
            for (Exception e : this.exceptionList) {
                System.err.println("_______________________");
                e.printStackTrace(System.err);
                System.err.println("_______________________");
            }
        }
        return !this.exceptionList.isEmpty();
    }

    /**
     * Adds a new exception to this object.
     *
     * @param e The exception to add.
     */
    public void addException( Exception e ) {
        this.exceptionList.add( e );
    }

    /**
     * Returns a list of all contained exceptions.
     */
    public List<Exception> getExceptionList() {
        return this.exceptionList;
    }

    /**
     * Prints the stack traces of all contained exceptions.
     *
     * @param ps The PrintStream to write the stack traces to.
     */
    @Override
    public void printStackTrace( PrintStream ps ) {
        this.exceptionList.forEach( exception -> exception.printStackTrace( ps ) );
    }

    /**
     * Prints the stack traces of all contained exceptions.
     *
     * @param pw The PrintWriter to write the stack traces to.
     */
    @Override
    public void printStackTrace( PrintWriter pw ) {
        this.exceptionList.forEach( exception -> exception.printStackTrace( pw ) );
    }
}
