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
 * Common base class for all models.
 *
 * __PLEASE NOTE__: There are absolutely no synchronization between these models and the rendered GUI in any way!
 * When a model structure is passed to _APSWebManager_ class then a JSON structure is crated and a message is sent
 * to the client. The backend is stateless! If, as a result of a client event the app wants to update the GUI, it
 * currently have to create a new model and send via _APSWebManager_. This will rerender the complete GUI.
 */
@SuppressWarnings( [ "unchecked", "WeakerAccess", "unused" ] )
@CompileStatic
@TypeChecked
class APSComponent<Component> {

    //
    // Private Members
    //

    /** Holds the properties for the model. */
    private Map<String, Object> props = [:]

    private Object value

    private boolean valueUpdated = false

    //
    // Methods
    //

    /**
     * @return The component properties.
     */
    Map<String, Object> getComponentProperties() {
        this.props
    }

    /**
     * Sets a component property.
     * @param name
     * @param value
     * @return
     */
    @SuppressWarnings( "UnusedReturnValue" )
    protected Component setComponentProperty( String name, Object value ) {

        this.props[ ( name ) ] = value

        return ( Component ) this
    }

    protected Object getComponentProperty( String name ) {

        this.props[ ( name ) ]
    }

    protected Component setHeader( String name, Object value ) {

        if ( this.props[ 'headers' ] == null ) {
            this.props[ 'headers' ] = (Object)[:]
        }

        def headers = this.props.headers

        headers[ name ] = value

        ( Component ) this
    }

    /**
     * This completely replaces 'headers' with passed structure!!
     *
     * @param rawHeaders The new headers to set.
     */
    protected Component setHeadersRaw( Map<String, Object> rawHeaders ) {
        this.props[ 'headers' ] = rawHeaders
        ( Component ) this
    }

    /**
     * Makes sure there are headers and returns them. The returned value can be added to!
     */
    protected Map<String, Object> getHeaders() {

        if ( this.props[ 'headers' ] == null ) {
            this.props[ 'headers' ] = (Object)[:]
        }

        this.props[ 'headers' ] as Map<String, Object>
    }

    protected Map<String, Object> getRouting() {

        if ( this.headers.routing == null ) {
            this.headers.routing = (Object)[:]
        }

        this.headers.routing as Map<String, Object>
    }

    void setValue( Object value ) {
        this.value = value
        this.valueUpdated = true
    }

    void clearUpdatedState() {
        this.valueUpdated = false
    }

    /**
     * This will affect the same values as setIncomingMessageRoutes(...) and setOutgoingMessageRoutes().
     * Use either this or those, but not both!
     *
     * If this is used then name is either "incoming" or "outgoing", and the value contains comma separated
     * routes with no spaces.
     *
     * @param name "incoming" or "outgoing".
     * @param value A route or a comma separated list of routes.
     * @return The subclass. Provides builder pattern.
     */
    Component setRouting( String name, String value ) {

        this.routing.put( name, value )

        ( Component ) this
    }

    /**
     * Sets the components unique id.
     *
     * @param id The id to set.
     * @return The subclass. Provides builder pattern.
     */
    Component setId( String id ) {

        this.props.id = id

        return ( Component ) this
    }

    /**
     * Sets the name of the component. This might go away.
     *
     * @param name The name to set.
     * @return The subclass. Provides builder pattern.
     */
    Component setName( String name ) {

        this.props.name = name
        return ( Component ) this
    }

    /**
     * Sets incoming message routes. vararg/array for multiple routes. Will convert to comma
     * separated string internally.
     *
     * @param routes The incoming routes to set.
     * @return The subclass. Provides builder pattern.
     */
    Component setIncomingMessageRoutes( List<String> routes ) {
        String comma = ""
        String value = ""
        for ( String route : routes ) {
            value = value + comma + route
            comma = ","
        }

        this.routing.incoming = value

        return ( Component ) this
    }

    Component setIncomingMessageRoutesAsArray( String... routes ) {
        setIncomingMessageRoutes( Arrays.asList( routes ) )
    }

    /**
     * Sets outgoing message routes. vararg/array for multiple routes. Will convert to comma
     * separated string internally.
     *
     * @param routes The outgoing routes to set.
     * @return The subclass. Provides builder pattern.
     */
    Component setOutgoingMessageRoutes( List<String> routes ) {
        String comma = ""
        String value = ""
        for ( String route : routes ) {
            value = value + comma + route
            comma = ","
        }

        this.routing.outgoing = value

        return ( Component ) this
    }

    Component setOutgoingMessageRoutesAsArray( String... routes ) {
        setOutgoingMessageRoutes( Arrays.asList( routes ) )
    }

    /**
     * Set name of group component belongs to if any.
     *
     * @param group The name of the group the component belongs to.
     * @return The subclass. Provides builder pattern.
     */
    Component setGroup( String group ) {

        this.props.group = group

        return ( Component ) this
    }

    /**
     * Sets a comma separated list of groups to collect data from components belonging to these groups.
     *
     * Collected data are passed on in event message produced by component. This simulates web forms. Let
     * a set of components belong to same group name, and have one component act as collector of group to
     * pass data on when triggered. A collector will basically act as a submit. *ANY* component can be a
     * collector, but it makes most sense for buttons.
     *
     * @param collectGroups A comma separated list of groups to collect data for.
     * @return The subclass. Provides builder pattern.
     */
    Component setCollectGroups( String collectGroups ) {

        this.props.collectGroups = collectGroups

        return ( Component ) this
    }

    /**
     * Set if this component should be enabled or not after rendering.
     *
     * @param enabled The enabled state of the component,
     * @return The subclass. Provides builder pattern.
     */
    Component setEnabled( String enabled ) {

        this.props.enabled = enabled

        return ( Component ) this
    }

    Map<String, Object> toJSON() {

        return this.props
    }

}
