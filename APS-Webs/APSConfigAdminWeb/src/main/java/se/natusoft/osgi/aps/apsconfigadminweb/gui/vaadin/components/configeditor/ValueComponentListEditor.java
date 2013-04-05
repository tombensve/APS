/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.9.1
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
 *         2012-04-23: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.apsconfigadminweb.config.CAWConfig;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedEvent;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedListener;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.css.CSS;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a component that edits a list of values.
 */
public class ValueComponentListEditor extends VerticalLayout {
    //
    // Private Members
    //

    /** The value model whose value is edited by this component instance. */
    private APSConfigValueEditModel valueEditModel = null;

    /** Maps between a value and its index. */
    private Map<String, Integer> valueIndexMap = new HashMap<String, Integer>();

    /** The reverse of valueIndexMap. */
    private String[] valueByIndex = null;

    /** The currently selected index or null if none is selected. */
    private Integer selectedIndex = null;

    /** The data source for the component. */
    private DataSource dataSource = null;

    //----- GUI Components -----//

    /** This is where all entries are edited. */
    private ValueComponent editLine = null;

    /** Holds the values. */
    private ListSelect values = null;

    /** The remove button. */
    private Button removeButton = null;

    /** A label showing the number of entries in 'values' */
    private Label sizeLabel = null;

    //----- Listeners -----//

    /** The listener for the editLine text field. */
    private ValueChangedListener editLineListener = new ValueChangedListener() {
        @Override
        public void valueChanged(ValueChangedEvent event) {
            updateValue(event.getValue());
        }
    };

    /** Listener for the 'values' ListSelect. */
    private ValueChangeListener valuesListener = new ValueChangeListener() {
        @Override
        public void valueChange(ValueChangeEvent event) {
            selectValue((String)event.getProperty().getValue());
        }
    };

    //
    // Constructors
    //

    /**
     * Creates a new ValueListEditor.
     *
     * @param valueEditModel The config value edit model representing a specific config value.
     * @param valueComponent The component to edit a list of.
     */
    public ValueComponentListEditor(APSConfigValueEditModel valueEditModel, ValueComponent valueComponent) {
        this.valueEditModel = valueEditModel;

        // Setup component gui.
        this.editLine = valueComponent;
        this.editLine.enableNullValues();
        this.editLine.getComponent().setWidth("100%");
        this.editLine.getComponent().setEnabled(true);
        this.editLine.setComponentValue("", false);
        this.editLine.addListener(this.editLineListener);
        addComponent(this.editLine.getComponent());

        this.values = new ListSelect();
        this.values.setRows(4); // With the exception of 1 , 2 and 3 will give 4!
        this.values.setWidth("100%");
        this.values.setNullSelectionAllowed(false);
        this.values.setEnabled(false);
        this.values.setImmediate(true);
        this.values.addListener(this.valuesListener);
        addComponent(this.values);

        HorizontalLayout buttonsLayout = new HorizontalLayout(); {
            Button addButton = new Button(" + ");
            addButton.addListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    addValue();
                }
            });
            buttonsLayout.addComponent(addButton);

            this.removeButton = new Button(" - ");
            this.removeButton.addListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    removeValue();
                }
            });
            this.removeButton.setEnabled(false);
            buttonsLayout.addComponent(this.removeButton);

            this.sizeLabel = new Label("&nbsp;&nbsp;&nbsp;&nbsp;[ ]", Label.CONTENT_XHTML);
            this.sizeLabel.setStyleName(CSS.APS_MANYVALUE_COUNT_LABEL);
            buttonsLayout.addComponent(this.sizeLabel);
        }
        addComponent(buttonsLayout);
    }

    //
    // Methods
    //

    /**
     * Sets the data source for the component.
     *
     * @param dataSource The data source to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Reloads data from the data source.
     */
    public void refreshData() {
        int size = this.dataSource.getSize(this.valueEditModel);
        this.valueByIndex = new String[size];

        if (size > 0) {
            this.values.setEnabled(true);
        }
        else {
            this.values.setEnabled(false);
        }

        this.values.removeListener(this.valuesListener); {

            this.values.removeAllItems();
            this.valueIndexMap.clear();
            for (int i = 0; i < size; i++) {
                String value = this.dataSource.getValue(this.valueEditModel, i);
                this.valueIndexMap.put(value, i);
                this.valueByIndex[i] = value;
                this.values.addItem(value);
            }

            if (this.selectedIndex != null) {
                this.values.setValue(this.valueByIndex[this.selectedIndex]);
            }

            int linesToExpandTo = CAWConfig.inst.manyValueMaxLinesBeforeScrolling.toInt();
            if (size <= linesToExpandTo) {
                int rows = size;
                if (rows < 4) {
                    rows = 4;
                }
                this.values.setRows(rows);
            }

        } this.values.addListener(this.valuesListener);

        this.sizeLabel.setValue("&nbsp;&nbsp;&nbsp;&nbsp;[ " + size + " ]");
    }

    /**
     * Puts the focus on the edit line.
     */
    private void focusEditLine() {
        if (this.editLine instanceof AbstractField) {
            ((AbstractField)this.editLine).focus();
        }
    }

    /**
     * Sets a value for the 'editLine' TextField component without triggering an event.
     *
     * @param value The value to set.
     */
    private void setEditLineValue(String value) {
        this.editLine.removeListener(this.editLineListener);
        this.editLine.setComponentValue(value, false);
        this.editLine.addListener(this.editLineListener);
    }

    /**
     * Selects a value.
     *
     * @param value The selected value.
     */
    private void selectValue(String value) {
        setEditLineValue(value);
        this.selectedIndex = this.valueIndexMap.get(value);
        this.removeButton.setEnabled(true);
    }

    /**
     * Updates the current value.
     *
     * @param value The value to update.
     */
    private void updateValue(String value) {
        if (this.selectedIndex != null) {
            if (value != null) {
                this.dataSource.updateValue(this.valueEditModel, value, this.selectedIndex);
            }
        }
        else {
            if (value != null && value.trim().length() > 0 && !this.values.containsId(value)) {
                this.dataSource.addValue(this.valueEditModel, value);
            }
            setEditLineValue("");
            focusEditLine();
        }

        refreshData();

        // If no item is selected in the 'values' ListSelect then disable the remove button.
        if (this.values.getValue() == null) {
            this.removeButton.setEnabled(false);
        }
    }

    /**
     * Adds the current value.
     */
    private void addValue() {
        setEditLineValue("");
        this.selectedIndex = null;
        focusEditLine();
    }

    /**
     * Removes the current value.
     */
    private void removeValue() {
        int size = this.dataSource.getSize(this.valueEditModel);
        int index = this.selectedIndex;

        this.dataSource.removeValue(this.valueEditModel, index);

        if (index == 0) {
            if (size == 1) {
                this.selectedIndex = null;
                this.removeButton.setEnabled(false);
            }
        }
        else {
            this.selectedIndex = index - 1;
        }

        this.values.focus();

        refreshData();

        if (this.selectedIndex != null) {
            setEditLineValue(this.valueByIndex[this.selectedIndex]);
        }
        else {
            setEditLineValue("");
        }

    }

    //
    // Inner Classes
    //

    /**
     * The data source for the ValueListEditor.
     */
    public static interface DataSource {

        /**
         * Returns the number of values.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         */
        public int getSize(APSConfigValueEditModel valueEditModel);

        /**
         * Returns the value at the specified index.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         * @param index The index to get the value for.
         */
        public String getValue(APSConfigValueEditModel valueEditModel, int index);

        /**
         * Adds the specified value to the set of values.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         * @param value The value to add.
         */
        public void addValue(APSConfigValueEditModel valueEditModel, String value);

        /**
         * Removes a value from the set of values.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         * @param index The index of the value to remove.
         */
        public void removeValue(APSConfigValueEditModel valueEditModel, int index);

        /**
         * Updates a value.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         * @param value The value to update with.
         * @param index The index to update.
         */
        public void updateValue(APSConfigValueEditModel valueEditModel, String value, int index);
    }
}
