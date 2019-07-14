package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.osgi.aps.exceptions.APSValidationException;

/**
 * Targets will be treated as strings in message APIs. This is a utility and can be used by
 * APSBusRouter implementations.
 *
 * A target is something that messages can be sent to and that can be subscribed to for
 * receiving send messages. There are 2 parts of a target. It starts with an id that should
 * for most parts be unique for the implementation. Then there is a colon ':' followed by
 * the second part, an address.
 *
 * All buses has some address concept, and if it is more complex than a String, then it
 * has to be encoded as a String, since that is what is supported!
 *
 * The id basically identifies an implementation and the address is an address on the bus
 * used by the implementation. That said, it does not have to be that clear :-). There is
 * nothing stopping 2 different implementations to use the same id. If that is the case
 * then messages to those targets will be send on both buses! There could possibly be
 * side effects of that, but it is possible! There is also special target id 'all' that
 * will be accepted by all implementations, so if that is used it will listen/send to
 * all bus implementations recognize. Use with great care.
 *
 * It is not a bad idea to let implementations of APSBusRouter have a configuration
 * including the target id to use.
 *
 * APS currently includes 2 buses that will always be available (as long as APS-APIs,
 * aps-core-lib, and aps-vertx-provider are deployed). One (id 'local') only sends
 * messages locally in memory of running JVM. The other (id 'cluster') sends messages
 * over a Vert.x cluster.
 */
public class APSTargetSpec {

    private String id;
    private String compareId; // To avoid a String addition on every valid() call.

    public void setId(String id) {
        this.id = id;
        this.compareId = id + ":";
    }

    public String getId() {
        return this.id;
    }

    private String address;

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }

    /**
     * Use this as Groovy named params constructor: new APSTargetSpec( id: "local", address: "qwerty" ),
     * or if you only have id: new APSTargetSepc( id: "local" )
     */
    public APSTargetSpec() {}

    /**
     * Creates a new APSTargetSpec from its string equivalent. This expects an 'id:address' string.
     *
     * @param target The string target.
     */
    public APSTargetSpec( String target) {

        String[] parts = target.split( ":" );

        if (parts.length != 2) {
            throw new APSValidationException("target mus have format 'id:address'! '" + target + "' is invalid.");
        }

        this.id = parts[0];
        this.compareId = this.id + ":";
        this.address = parts[1];
    }

    /**
     *  A Convenience method to create a full spec from a partial. Do note that creating a new
     *  instance with a full target string will do the same.
     *
     *  The only difference with this is that it will enforce the id to be the same as the id of
     *  this instance. So even if the passed string contains another id the target spec will
     *  contain this id.
     *
     * @param target The string target to use to get a full target spec.
     *
     * @return The new target spec.
     */
    public APSTargetSpec with( String target) {

        APSTargetSpec ts = new APSTargetSpec( target );
        ts.id = this.id;

        return ts;
    }

    /**
     * Returns true if the specified target has a valid id. It will always be true for
     * 'all'!
     *
     * @param target The target to test.
     *
     * @return true or false.
     */
    public boolean valid(String target) {
        return target.startsWith( this.compareId ) || target.startsWith( "all:" );
    }

    /**
     * Convert this back to its string variant.
     */
    public String toString() {
        return this.id + ":" + this.address;
    }
}
