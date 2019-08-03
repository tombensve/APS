package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Validates the id part of a target (id:address) and calls the passed closure if valid.
 *
 * This also supports "all:"!
 */
@CompileStatic
@TypeChecked
trait ValidTargetTrait {

    /** The target id to validate against. Should be set in constructor. */
    String vttTargetId

    /** Set this to true to support "all:" target id. */
    boolean vttSupportsAll = false

    /**
     * Sets the target id making sure it ends with ":".
     *
     * @param id The id to set with or without ending ":".
     */
    void setVttTargetId( String id) {
        this.vttTargetId = id
        if (!this.vttTargetId.endsWith( ":" )) {
            this.vttTargetId += ":"
        }
    }

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    @SuppressWarnings( "DuplicatedCode" )
    void validTarget( String target, Closure go ) {
        if ( target.startsWith( this.vttTargetId ) ) {
            target = target.substring( this.vttTargetId.length() )

            go.call( target )
        }
        else if (this.vttSupportsAll && target.startsWith( "all:" )) {
            target = target.substring( 4 )

            go.call( target )
        }
    }
}
