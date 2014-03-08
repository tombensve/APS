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
 *         2012-04-15: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService.APSConfigEnvAdmin;

import java.util.LinkedList;
import java.util.List;

/**
 * This components selects a configuration environment to edit values for (when values are configuration environment specific).
 */
public class ConfigEnvSelector extends VerticalLayout {
    //
    // Private Members
    //

    /** Holds information about available configuration environments. */
    private APSConfigEnvAdmin configEnvAdmin = null;

    /** The currently selected config environment. */
    private APSConfigEnvironment selectedConfigEnv = null;
    
    /** The listeners of this component. */
    private List<ConfigEnvChangeListener> listeners = new LinkedList<ConfigEnvChangeListener>();
    
    //----- GUI Components -----//

    /** Selection of config env. */
    private ComboBox editForConfigEnvSelect = null;
    
    /** Listener for config env selection. */
    private ValueChangeListener configEnvSelectListener = null;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEnvSelector.
     */
    public ConfigEnvSelector() {
        this.editForConfigEnvSelect = new ComboBox("Edit for configuration environment:");
        this.editForConfigEnvSelect.setImmediate(true);
        this.editForConfigEnvSelect.setInvalidAllowed(false);
        this.editForConfigEnvSelect.setNullSelectionAllowed(false);
        addComponent(this.editForConfigEnvSelect);
        
        this.configEnvSelectListener = new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                selectEditedConfigEnv(event.getProperty().toString());
            }
        };
    }

    //
    // Methods
    //

    /**
     * Sets the data source for this component.
     *
     * @param dataSource The data source to set.
     */
    public void setDataSource(APSConfigEnvAdmin dataSource) {
        this.configEnvAdmin = dataSource;
    }

    /**
     * Refreshes data for the component.
     */
    public void refreshData() {
        if (this.selectedConfigEnv == null) {
            this.selectedConfigEnv = this.configEnvAdmin.getActiveConfigEnvironment();
        }

        this.editForConfigEnvSelect.removeListener(this.configEnvSelectListener);

        this.editForConfigEnvSelect.removeAllItems();

        for (APSConfigEnvironment configEnv : this.configEnvAdmin.getAvailableConfigEnvironments()) {
            this.editForConfigEnvSelect.addItem(configEnv.getName());

            if (configEnv.equals(this.selectedConfigEnv)) {
                this.editForConfigEnvSelect.setValue(configEnv.getName());
            }
        }

        this.editForConfigEnvSelect.addListener(this.configEnvSelectListener);
    }

    /**
     * @return The currently selected config environment.
     */
    public APSConfigEnvironment getSelectedConfigEnvironment() {
        return this.selectedConfigEnv;
    }

    //
    // Event Handling
    //

    /**
     * Handles config env select event.
     *
     * @param selectedConfigEnv The newly selected config environment.
     */
    private void selectEditedConfigEnv(String selectedConfigEnv) {
        for (APSConfigEnvironment configEnv : this.configEnvAdmin.getAvailableConfigEnvironments()) {
            if (selectedConfigEnv.equals(configEnv.getName())) {
                this.selectedConfigEnv = configEnv;
                break;
            }
        }

        fireConfigEnvChangedEvent(this.selectedConfigEnv);
    }
    
    /**
     * Adds a listener to this component for change of config environment.
     *
     * @param listener The listener to add.
     */
    public void addListener(ConfigEnvChangeListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Fires a change of selected config environment event.
     *
     * @param selectedConfigEnv The config environment that was selected.
     */
    private void fireConfigEnvChangedEvent(APSConfigEnvironment selectedConfigEnv) {
        ConfigEnvChangeEvent event = new ConfigEnvChangeEvent(this, selectedConfigEnv);
        for (ConfigEnvChangeListener listener : this.listeners) {
            listener.configEnvironmentChanged(event);
        }
    }

    /**
     * Event triggered by change of selected configuration environment.
     */
    public static class ConfigEnvChangeEvent extends Event {
        //
        // Private Members
        //

        /** The selected config environment that triggered the event. */
        private APSConfigEnvironment selectedConfigEnvironment = null;

        //
        // Constructors
        //

        /**
         * Constructs a new event with the specified source component.
         *
         * @param source the source component of the event
         * @param selectedConfigEnvironment The selected config environment that triggered the event.
         */
        public ConfigEnvChangeEvent(Component source, APSConfigEnvironment selectedConfigEnvironment) {
            super(source);
            this.selectedConfigEnvironment = selectedConfigEnvironment;
        }

        //
        // Methods
        //

        /**
         * @return The selected config environment that triggered the event.
         */
        public APSConfigEnvironment getSelectedConfigEnvironment() {
            return this.selectedConfigEnvironment;
        }
    }

    /**
     * Event listener for change of selected configuration environment.
     */
    public static interface ConfigEnvChangeListener {

        /**
         * Receives config environment change events.
         *
         * @param event The received event.
         */
        public void configEnvironmentChanged(ConfigEnvChangeEvent event);
    }
}
