package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Holds data about an individual value in a MapJson structure according to a MapJsonDocValidator defined schema.
 *
 * MapJsonSchemaMeta collects a set of these by parsing a MapJson schema.
 */
@CompileStatic
@TypeChecked
class MapJsonEntryMeta {

    //
    // Inner Types
    //

    enum Type {
        STRING, BOOLEAN, NUMBER // If these are underlined in angry red then you are using IDEA! Just ignore it.
    }

    //
    // Properties
    //

    /** The name of the entry. */
    String name

    /** Is this entry required ? */
    boolean required

    /** The type of the entry. */
    Type type

    /** The entry constraints. */
    String constraints

    //
    // Methods
    //

    /**
     * @return A string representation of this.
     */
    String toString() {
        return "{ name: ${name}, required: ${required}, type: ${type}, constraints: ${constraints} }"
    }
}
