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
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor;

import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.ListSelect;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedEvent;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedListener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This component manages a value that has a set of valid values it can have.
 */
public class ValidValuesValue extends ListSelect implements ValueComponent {
    //
    // Private Members
    //

    /** The listeners on this component. */
    private List<ValueChangedListener> listeners = new LinkedList<>();

    /** The config value reference representing the config value. */
    private APSConfigReference valueRef = null;

    private boolean doFireEvent = true;

    //
    // Constructors
    //

    /**
     * Creates a new ValidValuesValue.
     *
     * @param valueRef The config value reference representing the config value.
     */
    public ValidValuesValue(APSConfigReference valueRef) {
        super("", Arrays.asList(valueRef.getConfigValueEditModel().getValidValues()));

        this.valueRef = valueRef;

        setRows(1);
        setImmediate(true);
        setNullSelectionAllowed(false);

        addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (doFireEvent) fireEvent((String)event.getProperty().getValue());
            }
        });
    }

    //
    // Public Methods
    //

    /**
     * Adds a listener to this component.
     *
     * @param listener The listener to add.
     */
    @Override
    public void addListener(ValueChangedListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes the listener from the component.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeListener(ValueChangedListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Sets the value for this component.
     *
     * @param value The value to set.
     * @param fireEvent True to fire value update event.
     */
    @Override
    public void setComponentValue(String value, boolean fireEvent) {
        this.doFireEvent = fireEvent;

        if (value == null || value.trim().length() == 0) {
            setValue(null);
        }
        else {
            for (String validValue : this.valueRef.getConfigValueEditModel().getValidValues()) {
                if (value.equals(validValue)) {
                    setValue(value);
                    break;
                }
            }
        }

        this.doFireEvent = true;
    }

    /**
     * Returns the value of this component.
     */
    @Override
    public String getComponentValue() {
        return super.getValue().toString();
    }

    /**
     * Returns the Vaadin component.
     */
    @Override
    public Component getComponent() {
        return this;
    }

    /**
     * This should enable null values where applicable!
     */
    @Override
    public void enableNullValues() {
        setNullSelectionAllowed(true);
    }

    //
    // Private Methods
    //

    /**
     * Updates the value in the configuration.
     *
     * @param value The vale to update.
     */
    private void fireEvent(String value) {
        ValueChangedEvent event = new ValueChangedEvent(this, this.valueRef, value);
        for (ValueChangedListener listener : this.listeners) {
            listener.valueChanged(event);
        }
    }
}
