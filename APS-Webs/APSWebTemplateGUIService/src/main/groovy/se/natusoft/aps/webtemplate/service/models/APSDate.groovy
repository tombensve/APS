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

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * Date selector component.
 */
@CompileStatic
@TypeChecked
class APSDate extends APSComponent<APSDate> {

    APSDate() {
        this.componentProperties.type = "aps-date"
    }

    APSDate setStartValue(String yymmdd) {
        this.componentProperties.startValue = yymmdd
        this
    }

    APSDate setStartValueAsDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        this.componentProperties.startValue = sdf.format( date )
        this
    }

    APSDate setStartValueAsTemporalAccessor(TemporalAccessor ta) {
        this.componentProperties.startValue = DateTimeFormatter.ISO_LOCAL_DATE.format( ta )
        this
    }

    APSDate setDisabled(boolean disabled) {
        this.componentProperties.disabled = "" + disabled
        this
    }
}
