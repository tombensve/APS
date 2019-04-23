package se.natusoft.osgi.aps.web.backend.component.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Common base class for all models.
 */
@SuppressWarnings( { "unchecked", "WeakerAccess" } )
public class APSComponent<S> {

    //
    // Private Members
    //

    /** Holds the properties for the model. */
    private Map<String, Object> props = new LinkedHashMap<>(  );

    /** A unique address to the represented component. */
    private String componentAddress;

    //
    // Methods
    //

    protected void setComponentAddress(String componentAddress) {
        this.componentAddress = componentAddress;
    }

    protected String getComponentAddress() {
        return this.componentAddress;
    }

    protected Map<String, Object> newMap() {
        return new LinkedHashMap<>(  );
    }

    @SuppressWarnings( "UnusedReturnValue" )
    protected S setProperty( String name, Object value) {
        this.props.put(name, value);

        return (S)this;
    }

    protected Object getProperty(String name) {
        return this.props.get( name );
    }

    protected S setHeader(String name, Object value ) {

        Map<String, Object> headers = getHeaders();
        headers.put(name, value);

        return (S)this;
    }

    protected Map<String, Object> getHeaders() {
        if (!this.props.containsKey( "headers" )) {
            this.props.put("headers", newMap());
        }
        //noinspection unchecked
        return (Map<String, Object>)this.props.get("headers");
    }

    protected Map<String, Object> getRouting() {
        if (!getHeaders().containsKey( "routing" )) {
            getHeaders().put("routing", newMap());
        }

        //noinspection unchecked
        return (Map<String, Object>)getHeaders().get("routing");
    }

    public S setRouting( String name, String value) {

        getRouting().put(name, value);

        return (S)this;
    }

    public S setId(String id) {

        this.props.put("id", id);
        return (S)this;
    }

    public S setName(String name) {

        this.props.put("name", name);
        return (S)this;
    }

    public S setIncomingMessageRoutes(String... routes) {
        String comma="";
        String value = "";
        for (String route : routes) {
            value = value + comma + route;
            comma=",";
        }

        setRouting( "incomming", value );

        return (S)this;
    }

    public S setOutgoingMessageRoutes(String... routes) {
        String comma="";
        String value = "";
        for (String route : routes) {
            value = value + comma + route;
            comma=",";
        }

        setRouting( "outgoing", value );

        return (S)this;
    }

    public S setGroup( String group) {

        this.setProperty( "group", group );
        return (S)this;
    }

    public S setCollectGroups(String collectGroups) {

        this.setProperty( "collectGroups", collectGroups );

        return (S)this;
    }

    public S setEnabled(String enabled) {

        this.setProperty( "enabled", enabled );

        return (S)this;
    }

    public Map<String, Object> getJSON() {

        return this.props;
    }

}
