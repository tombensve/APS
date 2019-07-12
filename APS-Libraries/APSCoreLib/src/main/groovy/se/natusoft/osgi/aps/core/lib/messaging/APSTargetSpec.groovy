package se.natusoft.osgi.aps.core.lib.messaging

import se.natusoft.osgi.aps.exceptions.APSValidationException

/**
 * Targets will be treated as strings in message APIs. This is a utility and can be used by
 * both APSBusRouter implementations and code using the message APIs.
 */
class APSTargetSpec {

    String id

    String address

    /**
     * Use this as Groovy named params constructor: new APSTargetSpec( id: "local", address: "qwerty" ),
     * or if you only have id: new APSTargetSepc( id: "local" )
     */
    APSTargetSpec() {}

    /**
     * Creates a new APSTargetSpec from its string equivalent. This expects an 'id:address' string.
     *
     * @param target The string target.
     */
    APSTargetSpec( String target) {

        String[] parts = target.split( ":" )

        if (parts.length != 2) {
            throw new APSValidationException("target mus have format 'id:address'! '${target}' is invalid.")
        }

        this.id = parts[0]
        this.address = parts[1]
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
    APSTargetSpec with( String target) {

        APSTargetSpec ts = new APSTargetSpec( target )
        ts.id = this.id

        ts
    }

    /**
     * Returns true if the specified target has a valid id.
     *
     * @param target The target to test.
     *
     * @return true or false.
     */
    boolean valid(String target) {
        target.startsWith( "${id}:" )
    }

    /**
     * Convert this back to its string variant.
     */
    String toString() {
        return "${id}:${address}"
    }
}
