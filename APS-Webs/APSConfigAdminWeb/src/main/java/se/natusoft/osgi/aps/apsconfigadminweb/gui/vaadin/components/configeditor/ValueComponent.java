/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.10.0
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
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor;

import com.vaadin.ui.Component;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedListener;

/**
 * This interface is implemented by all *Value components since they are very similar in behavior.
 */
public interface ValueComponent {

    /**
     * Adds a listener to this component.
     *
     * @param listener The listener to add.
     */
    void addListener(ValueChangedListener listener);

    /**
     * Removes the listener from the component.
     *
     * @param listener The listener to remove.
     */
    void removeListener(ValueChangedListener listener);

    /**
     * Sets the value for this component.
     *
     * @param value The value to set.
     * @param fireChangeEvent If true change events are fired.
     */
    public void setComponentValue(String value, boolean fireChangeEvent);

    /**
     * Returns the value of this component.
     */
    public String getComponentValue();

    /**
     * Returns the Vaadin component.
     */
    public Component getComponent();

    /**
     * This should enable null values where applicable!
     */
    public void enableNullValues();

}
