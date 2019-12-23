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
package se.natusoft.aps.webtemplate.service.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This component displays markdown text.
 *
 * Note that in addition to setting value (with § forcing new paragraph in one line strings) this
 * component also listens to messages of aps.type "md-text", and content.targetId same as component id.
 * If such is received the content.markdown will be used as text for the component.
 *
 * Example of message:
 *     {
 *         aps: {
 *             type: "md-text"
 *         },
 *         content: {
 *             targetId: "id",
 *             markdown: "markdown text"
 *         }
 *     }
 */
@CompileStatic
@TypeChecked
class APSMarkdown extends APSComponent<APSMarkdown> {

    APSMarkdown() {
        this.componentProperties.type = "aps-markdown"
    }
}
