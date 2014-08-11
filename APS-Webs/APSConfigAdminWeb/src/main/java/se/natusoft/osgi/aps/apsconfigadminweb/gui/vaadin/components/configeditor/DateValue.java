/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.11.0
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
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.PopupDateField;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedEvent;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * This component manages a date value.
 */
public class DateValue extends PopupDateField implements ValueComponent {
    //
    // Private Members
    //

    /** The listeners on this component. */
    private List<ValueChangedListener> listeners = new LinkedList<>();

    /** A reference to the config value. */
    private APSConfigReference valueRef = null;

    /** The date format to use for this date value. */
    private SimpleDateFormat dateFormat = null;

    private boolean doFireEvent = true;

    //
    // Constructors
    //

    /**
     * Creates a new DateValue.
     *
     * @param valueRef The config value reference representing the config value.
     */
    public DateValue(APSConfigReference valueRef) {
        this.dateFormat = new SimpleDateFormat(valueRef.getConfigValueEditModel().getDatePattern());

        setDateFormat(valueRef.getConfigValueEditModel().getDatePattern());
        setResolution(Resolution.DAY);
        super.setValue(new Date());
        setImmediate(true);

        addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    if (doFireEvent) fireEvent();
                }
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
     * @param fireEvent True to fire update event.
     */
    @Override
    public void setComponentValue(String value, boolean fireEvent) {
        this.doFireEvent = fireEvent;
        Date dateValue = null;

        if (value != null && value.trim().length() > 0) {
            try {
                dateValue = dateFormat.parse(value);
            }
            catch (ParseException pe) {
                dateValue = new Date();
            }
        }

        setValue(dateValue);

        this.doFireEvent = true;
    }

    /**
     * Returns the value of this component.
     */
    @Override
    public String getComponentValue() {
        Date dateValue = getValue();
        return this.dateFormat.format(dateValue);
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
    public void enableNullValues() {}

    //
    // Private Methods
    //

    /**
     * Updates the value in the configuration.
     */
    private void fireEvent() {
        String value = this.dateFormat.format(getValue());

        ValueChangedEvent event = new ValueChangedEvent(this, this.valueRef, value);
        for (ValueChangedListener listener : this.listeners) {
            listener.valueChanged(event);
        }
    }

}
