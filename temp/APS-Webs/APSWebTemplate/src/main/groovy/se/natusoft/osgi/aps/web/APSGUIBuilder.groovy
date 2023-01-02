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
package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
class APSGUIBuilder {

    private Map<String, Serializable> gui = [:]

    private Map<String, Serializable> current = gui

    private Map<String, Serializable> parent = null

    APSGUIBuilder comp(String name) {
        current["type"] = name
        this
    }

    APSGUIBuilder id(String id) {
        attr("id", id)
    }

    APSGUIBuilder attr(String name, Serializable value) {
        current[name] = value
        this
    }

    APSGUIBuilder children() {
        List<Map<String,Serializable>> children = []
        this
    }

    APSGUIBuilder endChildren() {
        endSub(  )
    }

    APSGUIBuilder subEntry(String name) {
        Map<String, Serializable> sub = [:]
        current[name] = (Serializable)sub
        parent = current
        current = sub
        this
    }

    APSGUIBuilder endSub() {
        current = parent
        parent = null
        this
    }

    APSGUIBuilder headers() {
        Map<String, Serializable> headers = [:]
        current["headers"] = (Serializable)headers
        parent = current
        current = headers
        this
    }

    APSGUIBuilder incomingRoutes(String incoming) {
        Map<String, Serializable> routing = current["routing"] as Map<String, Serializable>
        if (routing == null) {
            routing = [:]
            current["routing"] = (Serializable)routing
        }
        routing["incoming"] = incoming
        this
    }

    APSGUIBuilder outgoingRoutes(String outgoing) {
        Map<String, Serializable> routing = current["routing"] as Map<String, Serializable>
        if (routing == null) {
            routing = [:]
            current["routing"] = (Serializable)routing
        }
        routing["outgoing"] = outgoing
        this
    }

    Map<String, Serializable> toMap() {
        this.gui
    }
}
