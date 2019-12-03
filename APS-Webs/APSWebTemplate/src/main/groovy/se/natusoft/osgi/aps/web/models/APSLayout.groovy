/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Manager
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This project contains 2 parts:
 *         
 *         1. A frontend React web app.
 *         2. A Vert.x based backend that serves the frontend web app using Vert.x http router.
 *         
 *         Vert.x eventbus is used to communicate between frontend and backend.
 *         
 *         This build thereby also builds the frontend by using maven-exec-plugin to run a bash
 *         script that builds the frontend. The catch to that is that it will probably only build
 *         on a unix machine.
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
 *     tommy ()
 *         Changes:
 *         2019-08-17: Created!
 *         
 */
package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Does layout of other components, either vertically or horizontally.
 */
@CompileStatic
@TypeChecked
class APSLayout extends APSContainerComponent<APSLayout> {
    static String ORIENTATION = "orientation"
    static String ORIENTATION_VERTICAL = "vertical"
    static String ORIENTATION_HORIZONTAL = "horizontal"
    static String BORDER_STYLE = "borderStyle"
    static String BORDER = "border"
    static String BORDER_TRUE = "true"
    static String BORDER_FALSE = "false"

    enum Orientation { vertical, horizontal }

    APSLayout() {

        this.componentProperties.type = "aps-layout"
        this.componentProperties.border = false
        this.componentProperties.borderStyle = "1px solid black"
    }

    APSLayout setOrientation( Orientation orientation ) {

        this.componentProperties.orientation = orientation.name(  )

        this
    }

    APSLayout setBorder(boolean border) {

        this.componentProperties.border = border

        this
    }

    APSLayout setBorderStyle(String borderStyle) {

        this.componentProperties.borderStyle = borderStyle

        this
    }
}
