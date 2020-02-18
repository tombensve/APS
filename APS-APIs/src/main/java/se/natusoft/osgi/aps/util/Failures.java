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

import se.natusoft.osgi.aps.types.APSResult;

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
    private List<Exception> failures;

    //
    // Constructors
    //

    public Failures() {
        this.failures = new LinkedList<>(  );
    }

    private Failures(List<Exception> failures) {
        this.failures = failures;
    }

    //
    // Methods
    //

    /**
     * Returns true if there are any exceptions in this object.
     */
    public boolean hasFailures() {
        return !this.failures.isEmpty();
    }

    /**
     * Returns a filtered Failures instance.
     *
     * @param allowed An array of strings matching exception messages to mark as OK. If the message contains the
     *                string, then it is OK.
     */
    public Failures filter( String... allowed) {
        List<Exception> filteredFails = new LinkedList<>( this.failures );

        List<Exception> toRemove = new LinkedList<>(  );
        filteredFails.forEach( (fail) -> {
            for (String allow : allowed) {
                if ( fail.toString().contains( allow ) ) toRemove.add(fail);
            }
        } );

        toRemove.forEach( filteredFails::remove );

        return new Failures( filteredFails );
    }

    /**
     * Adds a new exception to this object.
     *
     * @param e The exception to add.
     */
    public void addException( Exception e ) {
        this.failures.add( e );
    }

    /**
     * Left shift operator for Groovy. Ex: failures << e
     *
     * @param e The exception to add.
     */
    public void leftShift( Exception e ) {
        addException( e );
    }

    /**
     * Returns a list of all contained exceptions.
     */
    public List<Exception> getFailures() {
        return this.failures;
    }

    /**
     * Prints the stack traces of all contained exceptions.
     *
     * @param ps The PrintStream to write the stack traces to.
     */
    @Override
    public void printStackTrace( PrintStream ps ) {
        this.failures.forEach( exception -> exception.printStackTrace( ps ) );
    }

    /**
     * Prints the stack traces of all contained exceptions.
     *
     * @param pw The PrintWriter to write the stack traces to.
     */
    @Override
    public void printStackTrace( PrintWriter pw ) {
        this.failures.forEach( exception -> exception.printStackTrace( pw ) );
    }

    /**
     * Checks of the result is a success or not. If not the exception is added to list of exceptions
     * and false is returned. If success true is returned.
     *
     * @param res The result to assert.
     *
     * @return true/false.
     */
    public boolean assertAPSResult( APSResult<?> res ) {
        if (!res.success()) {
            addException( res.failure() );
            return false;
        }

        return true;
    }

    public void showFailures() {
        System.err.println("Following errors occured:");
        for (Exception e : this.failures ) {
            System.err.println("_______________________");
            e.printStackTrace(System.err);
            System.err.println("_______________________");
        }
    }
}
