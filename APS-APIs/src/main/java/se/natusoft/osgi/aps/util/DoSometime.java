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
 *         2018-05-31: Created!
 *         
 */
package se.natusoft.osgi.aps.util;

import se.natusoft.osgi.aps.exceptions.APSValidationException;

import java.util.LinkedList;
import java.util.List;

/**
 * The intention with this class is to be able to delay execution waiting for a condition to be met.
 * If the condition is already met at `action(...)` call then the action is preformed immediately, otherwise
 * it is saved until `executeWaiting()` is called.
 */
public class DoSometime {

    //
    // Private Members
    //

    private APSLogger logger;

    private Condition condition;

    private List<Runnable> todo = new LinkedList<>(  );

    //
    // Methods
    //

    /**
     * Supplies a condition for executing actions. This is required!
     *
     * @param condition The condition to be met.
     */
    public DoSometime condition(Condition condition) {
        this.condition = condition;
        return this;
    }

    /**
     * This supplies a logger that will be used on action exceptions. If no logger is supplied exceptions will
     * be quietly swallowed.
     *
     * @param logger The logger to provide.
     */
    public DoSometime logger(APSLogger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * An action to be performed sometime.
     *
     * @param action The action.
     */
    public void action(Runnable action) {
        if (this.condition == null) {
            throw new APSValidationException( "Cannot execute action, no condition has been provided!" );
        }
        if (this.condition.met()) {
            runAction( action );
        }
        else {
            this.todo.add(action);
        }
    }

    /**
     * Will execute all saved/waiting actions.
     */
    public void executWaiting() {
        for (Runnable action : this.todo) {
            runAction( action );
        }
    }

    /**
     * Internal action runner catching exceptions and logging if logger is available.
     *
     * @param action The action to run.
     */
    private void runAction(Runnable action) {
        try {
            action.run();
        }
        catch ( Exception e ) {
            if (this.logger != null) {
                this.logger.error(e.getMessage(), e);
            }
        }
    }
}
