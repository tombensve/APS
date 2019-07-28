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
    String validTargetTrait_targetId

    /**
     * Sets the target id making sure it ends with ":".
     *
     * @param id The id to set with or without ending ":".
     */
    void setValidTargetTrait_targetId(String id) {
        this.validTargetTrait_targetId = id
        if (!this.validTargetTrait_targetId.endsWith( ":" )) {
            this.validTargetTrait_targetId += ":"
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
        if ( target.startsWith( this.validTargetTrait_targetId ) ) {
            target = target.substring( this.validTargetTrait_targetId.length() )

            go.call( target )
        }
        else if (target.startsWith( "all:" )) {
            target = target.substring( 4 )

            go.call( target )
        }
    }
}
