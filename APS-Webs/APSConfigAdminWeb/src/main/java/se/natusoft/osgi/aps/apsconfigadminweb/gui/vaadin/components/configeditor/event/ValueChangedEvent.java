/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Edits configurations registered with the APSConfigurationService.
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-04-30: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;

/**
 * This event is sent by *Value components when the value managed by the component has changed.
 */
public class ValueChangedEvent extends Event {
    //
    // Private Members
    //

    /** The config value reference representing the actual value. */
    private APSConfigReference valueRef = null;

    /** The value that triggered the event. */
    private String value = null;

    //
    // Constructors
    //

    /**
     * Creates a new ValueChangedEvent.
     *
     * @param originator The component that is the source of the event.
     * @param valueRef The config value reference representing the actual value.
     * @param value The value that triggered the event.
     */
    public ValueChangedEvent(Component originator, APSConfigReference valueRef, String value) {
        super(originator);
        this.valueRef = valueRef;
        this.value = value;
    }

    //
    // Methods
    //

    /**
     * Returns the value that triggered the event.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the config value edit model representing the actual value.
     */
    public APSConfigReference getValueReference() {
        return this.valueRef;
    }
}
