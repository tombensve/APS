/* 
 * 
 * PROJECT
 *     Name
 *         APS Administration Web
 *     
 *     Code Version
 *         0.10.0
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

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * This panels shows the logo and a refresh button.
 */
public class LogoPanel extends Panel {
    //
    // Constructors
    //

    public LogoPanel(Button.ClickListener clickListener) {
        super();
        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setSpacing(true);
        topLayout.setMargin(new MarginInfo(true, true, false, true));
        setContent(topLayout);
        setStyleName(Reindeer.PANEL_LIGHT);

        ThemeResource logoResource = new ThemeResource("images/app-platform-services.png");
        Embedded logo = new Embedded("", logoResource);
        logo.setWidth("300px");
        logo.setStyleName("aps-logo");
        topLayout.addComponent(logo);

        Label textLabel = new Label("<font size='+1'><b>Admin Web</b></font>");
        textLabel.setContentMode(ContentMode.HTML);
        topLayout.addComponent(textLabel);

        Label startBracket = new Label("[");
        topLayout.addComponent(startBracket);

        Button refreshButton = new Button("Refresh", clickListener);
        refreshButton.setStyleName(BaseTheme.BUTTON_LINK);
        topLayout.addComponent(refreshButton);

        Label endBracket = new Label("]");
        topLayout.addComponent(endBracket);
    }

    //
    // Methods
    //

}
