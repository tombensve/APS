/* 
 * 
 * PROJECT
 *     Name
 *         APS Administration Web
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         This is a web application providing and administration web frame.
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
 *         2011-09-18: Created!
 *         
 */
package se.natusoft.osgi.aps.apsadminweb.app.gui.vaadin;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService;
import se.natusoft.osgi.aps.apsadminweb.service.model.AdminWebReg;
import se.natusoft.osgi.aps.tools.web.ClientContext;
import se.natusoft.osgi.aps.tools.web.vaadin.APSTheme;
import se.natusoft.osgi.aps.tools.web.vaadin.components.HTMLFileLabel;

import java.util.List;

/**
 * This is the tab panel with a tab for each registered admin web.
 */
public class TabPanel extends TabSheet {
    //
    // Private Members
    //

    /** The client context. */
    private ClientContext clientContext = null;

    //
    // Constructors
    //

    public TabPanel(ClientContext clientContext) {
        this.clientContext = clientContext;

        this.setSizeFull();
        this.setStyleName("asp-tabsheet");

        VerticalLayout aboutTabLayout = new VerticalLayout();
        aboutTabLayout.setStyleName("aps-tabsheet-tab");
        aboutTabLayout.setMargin(true);
        aboutTabLayout.setSizeFull();
        Label aboutText = new HTMLFileLabel("/html/about-admin-web.html", APSTheme.THEME, getClass().getClassLoader());
        aboutTabLayout.addComponent(aboutText);
        addTab(aboutTabLayout, "About", null).setDescription("Information about APS Admin Web tool.");
        // Create tab for each registered admin web.
        refreshTabs();
    }

    //
    // Methods
    //

    /**
     * Recreates all tabs.
     */
    public void refreshTabs() {
        List<AdminWebReg> currentAdminWebs = this.clientContext.getService(APSAdminWebService.class).getRegisteredAdminWebs();

        // Remove old
        removeAllComponents();

        VerticalLayout aboutTabLayout = new VerticalLayout();
        aboutTabLayout.setStyleName("aps-tabsheet-tab");
        aboutTabLayout.setMargin(true);
        aboutTabLayout.setSizeFull();
        Label aboutText = new HTMLFileLabel("/html/about-admin-web.html", APSTheme.THEME, getClass().getClassLoader());
        aboutTabLayout.addComponent(aboutText);
        addTab(aboutTabLayout, "About", null).setDescription("Information about APS Admin Web tool.");


        // Add new
        for (AdminWebReg adminWebReg : currentAdminWebs) {
            VerticalLayout tabLayout = new VerticalLayout();
            tabLayout.setStyleName("aps-tabsheet-tab");
            tabLayout.setSizeFull();
            tabLayout.setMargin(false);
            tabLayout.setSpacing(false);
            Embedded adminWeb = new Embedded("", new ExternalResource(adminWebReg.getUrl() + "?adminRefresh"));
            adminWeb.setType(Embedded.TYPE_BROWSER);
            adminWeb.setSizeFull();
            tabLayout.addComponent(adminWeb);
            tabLayout.setData(adminWebReg);

            Tab tab = addTab(tabLayout, adminWebReg.getName(), null);
            tab.setDescription(adminWebReg.getDescription() + "<br/>[" + adminWebReg.getName() + ":" + adminWebReg.getVersion() + "]");
        }
    }

}
