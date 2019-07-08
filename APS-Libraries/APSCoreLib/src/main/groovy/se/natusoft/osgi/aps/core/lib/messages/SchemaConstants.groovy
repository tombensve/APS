package se.natusoft.osgi.aps.core.lib.messages

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
@TypeChecked
class SchemaConstants {

    // Predefined regexps for value validation. Syntax is for MapJsonSchemaValidator.
    public static final String TEXT_NUM_DOT = "?[a-z,A-Z, ,0-9,\\.]*\$"
    public static final String TEXT_NUM_DASH = "?[a-z,A-Z, ,0-9,-]*\$"
    public static final String TEXT_NUM_DASH_DOT = "?[a-z,A-Z, ,0-9,-.\\.]*\$"
    public static final String TEXT_NUM_DASH_DOT_COLON = "?[a-z,A-Z, ,0-9,-.\\.,:]*\$"
    public static final String TEXT_DOT = "?[a-z,A-Z, ,\\.]*\$"
    public static final String TEXT = "?.*"
    public static final String NUM_DOT = "?[0-9,\\.]*\$"
    public static final String NUMBERS_DECIMAL = "?[0-9,\\.]*\$"

    // Aliases
    public static final String IP_ADDRESS = NUM_DOT
    public static final String BUS_ADDRESS = TEXT_NUM_DASH_DOT_COLON
    public static final String _UUID = TEXT_NUM_DASH // Collides with java.util.UUID without the _ prefix!

}
