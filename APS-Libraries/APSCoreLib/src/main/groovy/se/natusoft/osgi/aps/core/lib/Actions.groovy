/* 
 * 
 * PROJECT
 *     Name
 *         APS Core Lib
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This library is made in Groovy and thus depends on Groovy, and contains functionality that
 *         makes sense for Groovy, but not as much for Java.
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
 *         2018-05-23: Created!
 *         
 */
package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Simple utility to collect a set of actions and then later execute them in order.
 *
 * Supports Groovy << and += operators for adding tasks.
 */
@CompileStatic
@TypeChecked
class Actions implements Runnable {
    /** The list of actions. */
    private List<Closure> actions = [ ]

    /** This will be called if non null on error. An exception will be passed. */
    Closure errorHandler

    /**
     * Adds an action to execute at some later time.
     *
     * @param action The action to execute.
     */
    synchronized void addAction( Closure action ) {

        this.actions += action
    }

    /**
     * Allow left shift operator to add action.
     *
     * @param action The action to add.
     */
    void leftShift( Closure action ) {

        addAction( action )
    }

    /**
     * Allows plus operator.
     *
     * @param action The action to add.
     */
    void plus( Closure action ) {

        addAction( action )
    }

    /**
     * Execute all actions and clear the action list.
     */
    synchronized void run() {

        if ( !this.actions.isEmpty() ) {

            this.actions.each { Closure closure ->

                try {
                    closure.call()
                }
                catch ( Exception e ) {

                    if ( this.errorHandler != null ) {
                        this.errorHandler.call( e )
                    }
                }
            }

            this.actions.clear()
        }
    }

    /**
     * Discards current actions.
     */
    synchronized void discardActions() {

        this.actions.clear()
    }
}
