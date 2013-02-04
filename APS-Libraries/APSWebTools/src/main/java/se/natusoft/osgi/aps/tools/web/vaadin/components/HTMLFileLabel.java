/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.9.0
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
 *         2012-02-26: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web.vaadin.components;

import com.vaadin.ui.Label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This is a Label that takes a classpath relative path to an html file and loads it as label content.
 * Please note that it required XHTML!
 * <p/>
 * Any comment blocks in the html are skipped when loading.
 */
public class HTMLFileLabel extends Label {
    //
    // Constructors
    //

    /**
     * Creates a new HTMLFileLabel.
     * 
     * @param htmlFilePath The label content html file.
     * @param themeName The name of the theme used. Any {vaadin-theme} in loaded html will be
     *                  replaced with a valid vaadin theme path based on this theme name. Use
     *                  this when referencing images in the html (assuming htey are theme
     *                  specific). "" is a valid value if theme is not relevant.
     * @param classLoader The class loader to use for finding the html file path.
     */
    public HTMLFileLabel(String htmlFilePath, String themeName, ClassLoader classLoader) {
        super(loadHTML(htmlFilePath, "VAADIN/themes/" + themeName + "/images", classLoader));
        setContentMode(Label.CONTENT_XHTML);
    }

    /**
     * Loads html file from classpath and returns as String.
     * 
     * @param htmlFilePath The html file to load.
     * @param themePath The path to the theme images.
     * @param classLoader The class loader to use for finding the path.
     *                     
     * @return The loaded html file.
     */
    private static String loadHTML(String htmlFilePath, String themePath, ClassLoader classLoader) {
        String html = null;        
        try {
            InputStream htmlStream = classLoader.getResourceAsStream(htmlFilePath);
            if (htmlStream == null) {
                throw new IOException();
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(htmlStream));
            String line = null;
            boolean comment = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<!--")) {
                    comment = true;
                }
                if (!comment) {
                    line = line.trim().replace("{vaadin-theme}", themePath);
                    sb.append(line);
                    sb.append(" ");
                }
                if (line.trim().startsWith("-->")) {
                    comment = false;
                }
            }
            reader.close();
            html = sb.toString();
        }
        catch (IOException ioe) {
            html = "Bad path: '" + htmlFilePath + "'!";
        }
        
        return html;
    }
}
