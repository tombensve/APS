/* 
 * 
 * PROJECT
 *     Name
 *         APS Vaadin Web Tools
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This provides some utility classes for web applications.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-09-16: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web.vaadin;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.*;
import se.natusoft.osgi.aps.tools.web.LoginHandler;

/**
 * This is a Vaadin based login dialog.
 */
public class VaadinLoginDialogHandler {
    //
    // Private Members
    //

    /** The login window dialog. */
    private Window loginWindow = null;

    /** The user name input field. */
    private TextField userName = null;

    /** The password input field. */
    private PasswordField password = null;

    /** Set to true when the dialog is shown. */
    private boolean dialogActive = false;

    /** A login handler for doing a login. */
    private LoginHandler loginHandler = null;

    //
    // Constructors
    //

    /**
     * Creates a new VaadinLoginDialogHandler. This constructor is deprecated. Use the one only taking a LoginHandler instead.
     *
     * @param appWindow The Vaadin application window to add the popup login dialog to.
     * @param loginHandler A handler for doing the login from the login dialog input.
     */
    @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
    @Deprecated
    public VaadinLoginDialogHandler(Window appWindow, LoginHandler loginHandler) {
        this(loginHandler);
    }

    /**
     * Creates a new VaadinLoginDialogHandler.
     *
     * @param loginHandler A handler for doing the login from the login dialog input.
     */
    public VaadinLoginDialogHandler(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;

        // Create Login window dialog
        this.loginWindow = new Window("Login");
        this.loginWindow.setClosable(false);
        this.loginWindow.setDraggable(false);
        this.loginWindow.setResizable(false);
        this.loginWindow.setModal(true);
        this.loginWindow.setWidth("300px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);
        dialogLayout.setMargin(true);
        this.userName = new TextField("User:");
        this.userName.setColumns(20);
        dialogLayout.addComponent(this.userName);
        this.password = new PasswordField("Password");
        this.password.setColumns(20);
        this.password.setImmediate(true);
        this.password.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                login();
            }
        });
        dialogLayout.addComponent(password);
        Button loginButton = new Button("Login");
        loginButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                login();
            }
        });
        dialogLayout.addComponent(loginButton);
        this.loginWindow.setContent(dialogLayout);
    }

    //
    // Methods
    //

    /**
     * Sets the title of the login dialog window.
     *
     * @param loginDialogTitle The title to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setLoginDialogTitle(String loginDialogTitle) {
        this.loginWindow.setCaption(loginDialogTitle);
    }

    /**
     * This will popup the login dialog if it is not already showing. Any previously entered user and password are ofcourse cleared!
     */
    @SuppressWarnings("UnusedDeclaration")
    public void doLoginDialog() {
        if (!this.dialogActive) {
            this.userName.setValue("");
            this.password.setValue("");
            UI.getCurrent().addWindow(this.loginWindow);
            this.dialogActive = true;
        }
    }

    /**
     * Logs in and removes the login dialog if successful.
     */
    private void login() {
        if (this.loginHandler.login(this.userName.getValue(), this.password.getValue())) {
            UI.getCurrent().removeWindow(this.loginWindow);
        }
    }
}
