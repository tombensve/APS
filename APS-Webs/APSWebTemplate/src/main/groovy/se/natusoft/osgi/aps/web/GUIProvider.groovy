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
 *         2019-04-17: Created!
 *
 */
package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import org.osgi.framework.ServiceReference
import se.natusoft.aps.activator.annotation.Initializer
import se.natusoft.aps.activator.annotation.Managed
import se.natusoft.aps.activator.annotation.APSPlatformService
import se.natusoft.aps.api.messaging.APSBus
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.APSUUID
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSJson
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.web.models.APSAlert
import se.natusoft.osgi.aps.web.models.APSButton
import se.natusoft.osgi.aps.web.models.APSCheckbox
import se.natusoft.osgi.aps.web.models.APSComponent
import se.natusoft.osgi.aps.web.models.APSDate
import se.natusoft.osgi.aps.web.models.APSLayout
import se.natusoft.osgi.aps.web.models.APSNumber
import se.natusoft.osgi.aps.web.models.APSRadioSet
import se.natusoft.osgi.aps.web.models.APSSelect
import se.natusoft.osgi.aps.web.models.APSTextArea
import se.natusoft.osgi.aps.web.models.APSTextField
import se.natusoft.osgi.aps.web.models.APSTree
import se.natusoft.osgi.aps.web.models.APSTreeNode

// Instantiated, injected, etc by APSActivator. IDE can't see that.
@SuppressWarnings( "unused" )
@CompileStatic
class GUIProvider {

    // In your web app copy of APSWebManager change this to your web app name.
    private final String APS_WEB_APP_NAME = "web-demo"

    @Managed
    private APSLogger logger

    @APSPlatformService
    private APSServiceTracker<APSBus> apsBusTracker
    private APSBus apsBus

    //private MessageConsumer newClientConsumer
    private ID clientConsumerId

    @Initializer
    void init() {

        this.apsBusTracker.onActiveServiceAvailable = { APSBus _apsBus, ServiceReference sref ->

            this.apsBus = _apsBus
            this.logger.info( "The underlaying Vert.x EventBus just became available!" )

            this.clientConsumerId = new APSUUID()
            apsBus.subscribe(
                    this.clientConsumerId,
                    "cluster:aps:${ APS_WEB_APP_NAME }:backend"
            ) { APSResult result ->

                if ( !result.success() ) {
                    this.logger.error( result.failure().message, result.failure() )
                }
            } { Map<String, Object> received ->

                //this.logger.debug( "§§§§§§§§§§ Received: ${received}" )

                String recv = received.toString()
                this.logger.info( "Received from '${ received[ "aps" ][ "origin" ] }': ${ recv }" )

                try {
                    if ( received[ "aps" ][ "type" ] == "avail" ) {
                        provideGui( apsBus, received[ "aps" ][ "origin" ] as String )
                    }
                }
                catch ( Exception e ) {
                    this.logger.error( e.getMessage(), e )
                }
            }
        }

        this.apsBusTracker.onActiveServiceLeaving = { ServiceReference sref, Class api ->
            this.apsBus = null
            this.logger.info( "EventBus going away!" )
        }

    }

    /**
     * This gets called when a new frontend announces itself with a message. The message contains
     * the address the frontend is listening to. This is the second argument to this method.
     *
     * This needs to provide a GUI spec in JSON and send to the frontend, which will render it.
     * All further communication with the frontend gui is done through event bus messages. The
     * JSON GUI spec determines which components deliver messages to backend. Components can
     * act as "collectors" of data from other components in GUI on the frontend. These will
     * pass that data on to backend when they send messages. This simulates form behaviour, but
     * in a more flexible way.
     *
     * @param eventBus Our event bus.
     * @param address The frontend address to send GUI JSON spec to.
     */
    private void provideGui( APSBus apsBus, String address ) {
        this.logger.debug( "In provideGui!" )

        Map<String, Object> reply =
                [
                        aps    : [
                                origin: "aps:web-demo:backend",
                                app   : "web-demo",
                                type  : "gui"
                        ],
                        content: buildGUIUsingModels()
                ] as Map<String, Object>

        apsBus.send( "cluster:${address}", reply ) { APSResult res ->

            if ( !res.success() ) {
                this.logger.error( res.failure().message, res.failure() )
            }
        }

        this.logger.debug( "Sent: ${ reply } to cluster:${ address }" )

    }

    // Below are 2 different methods that return a JSON structure as Map<String, Object>.
    //
    // One just reads a complete JSON document with the whole GUI and sends it to the
    // frontend.
    //
    // The other uses models under se.natusoft.osgi.aps.web.models that models the frontend
    // GUI components. This allows for building the GUI in a Groovy/Java way. With this variant
    // return toJSON() of the root object to produce a complete, correct JSON of the GUI.

    /**
     * Reads GUI from classpath resource JSON file.
     */
    protected static Map<String, Object> loadGUIFromJSONResource() {

        InputStream jsonStream =
                new BufferedInputStream( GUIProvider.class.classLoader.getResourceAsStream( "guijson/gui.json" ) )

        Map<String, Object> gui = null

        try {
            gui = APSJson.readObject( jsonStream )
        }
        finally {
            jsonStream.close()
        }

        gui
    }

    /**
     * Builds GUI code wise using Groovy/Java models.
     */
    protected static Map<String, Object> buildGUIUsingModels() {

        APSComponent root = new APSLayout(
                id: "id",
                name: "page",
                orientation: APSLayout.Orientation.vertical,
                borderStyle: "1px solid black",
                border: false,
        ) << new APSAlert(
                id: "aps-default-alert",
                name: "alert-comp",
                alertType: "danger",
                incomingMessageRoutes: ["client"],
                outgoingMessageRoutes: ["client"]
        )

        APSLayout hTestGroup1 = new APSLayout(
                id: "h-test-group-1",
                name: "top",
                orientation: APSLayout.Orientation.horizontal
        )
        root << hTestGroup1

        hTestGroup1 << new APSTextField(
                id: "name",
                name: "name-field",
                group: "gpoc",
                label: "Qwerty",
                width: 20,
                value: "",
                outgoingMessageRoutes: ["client"],
                incomingMessageRoutes: ["client"]

        ) << new APSTextArea(
                id: "description",
                name: "descriptionField",
                group: "gpoc",
                cols: 30,
                rows: 1,
                value: "",
                outgoingMessageRoutes: ["client"],
                incomingMessageRoutes: ["client"]

        ) << new APSButton(
                id: "submit",
                name: "submitButton",
                group: "gpoc",
                label: "Save",
                styleClass: "btn btn-success",
                disabled: true,
                collectGroups: "gpoc",
                enabled: "groupNotEmpty:gpoc",
                outgoingMessageRoutes: ["backend"],
                incomingMessageRoutes: ["client"]

        ) << new APSNumber(
                id: "num",
                name: "numeric",
                min: -10.0 as double,
                max: 10.0 as double,
                value: 2.5 as double,
                outgoingMessageRoutes: ["client"],
                incomingMessageRoutes: ["client"]

        ) << new APSDate(
                id: "date",
                name: "dateSelector",
                startValue: "2018-08-10",
                outgoingMessageRoutes: ["client", "backend"],
                incomingMessageRoutes: ["client"]

        ) << new APSCheckbox(
                id: "chckbox",
                name: "cb",
                value: "checked",
                label: "test",
                outgoingMessageRoutes: ["client"],
                incomingMessageRoutes: ["client"]
        )

        APSLayout hTestGroup2 = new APSLayout(
                id: "h-test-group-2",
                name: "top",
                orientation: APSLayout.Orientation.horizontal,
        )
        root << hTestGroup2

        hTestGroup2 << new APSRadioSet(
                id: "radioset",
                name: "radios",
                value: "two",
                outgoingMessageRoutes: ["client"],
                incomingMessageRoutes: ["client"]
        )
                .addRadio( "one", "One" )
                .addRadio( "two", "Two" )
                .addRadio( "three", "Three" )

        hTestGroup2 << new APSSelect(
                id: "select",
                name: "select",
                value: "two",
                outgoingMessageRoutes: ["client"],
                incomingMessageRoutes: ["client"]
        )
                .addOption( "one", "One" )
                .addOption( "two", "Two" )
                .addOption( "three", "Three" )

        hTestGroup2 << new APSTree(
                id: "tree",
                name: "tree",
                outgoingMessageRoutes: ["client"],
                incomingMessageRoutes: ["client"],
                nodes: new APSTreeNode(
                        label: "one",
                        id: "one",
                        type: APSTreeNode.Type.branch,
                        open: false )
                        .addChild( new APSTreeNode(
                                label: "one.one",
                                id: "one.one",
                                type: APSTreeNode.Type.branch,
                                open: false )
                                .addChild( new APSTreeNode(
                                        label: "one.one.one",
                                        id: "one.one.one",
                                        type: APSTreeNode.Type.leaf,
                                        open: false )
                                )
                        )
                        .addChild( new APSTreeNode(
                                label: "two",
                                id: "two",
                                type: APSTreeNode.Type.branch,
                                open: false )
                                .addChild( new APSTreeNode(
                                        label: "two.two",
                                        id: "two.two",
                                        type: APSTreeNode.Type.leaf,
                                        open: false )
                                )
                        )
        )

        APSLayout vertLayout = new APSLayout(
                id: "vert-layout",
                name: "vert-layout",
                orientation: APSLayout.Orientation.vertical,
        )
        hTestGroup2 << vertLayout

        vertLayout << new APSNumber(
                id: "num",
                name: "numeric",
                min: -20.0 as double,
                max: 20.0 as double,
                value: 12.0 as double,
                outgoingMessageRoutes: ["client"],
                incomingMessageRoutes: ["client"]
        ) << new APSDate(
                id: "date2",
                name: "dateSelector2",
                startValue: "2019-06-08",
                outgoingMessageRoutes: ["client", "backend"],
                incomingMessageRoutes: ["client"]
        )


        root.toJSON()
    }

}
