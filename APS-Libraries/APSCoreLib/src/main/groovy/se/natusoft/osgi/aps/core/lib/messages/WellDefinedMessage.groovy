package se.natusoft.osgi.aps.core.lib.messages

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.core.lib.MapJsonDocSchemaValidator
import se.natusoft.osgi.aps.exceptions.APSValidationException
import static SchemaConstants.*

/**
 * This is a base class for well defined messages being represented by
 * a subclass of this, but still can act as a JSON structure message.
 *
 * Each subclass should define a static schema and pass down to this class
 * on construction. This to have just one instance of the schema to reuse
 * for each message.
 */
@CompileStatic
@TypeChecked
class WellDefinedMessage<SubClass> implements Map<String, Object> {

    //
    // Private Members
    //

    /** The validator for our structure. */
    private MapJsonDocSchemaValidator schemaValidator

    //
    // Groovy JB Properties
    //

    /**
     * Provides base schema.
´     */
    Map<String, Object> getSchema() {
        [
                header_1: [
                        type_1: TEXT_NUM_DASH_DOT,   // Type of message.
                        version_1: "#>=0.0",         // Version of message.
                        sender_0: TEXT_NUM_DOT,      // Some info about sender.
                        replyAddress_0: BUS_ADDRESS  // For replies send to this address.
                ],
                content_1: [:]
        ] as Map<String, Object>
    }

    /**
     * A bit of Groovy goodness. This will provide Map API to this class that
     * automatically delegates to this instance. This allows us to change the
     * the actual instance being delegated to. So if created with a Map then
     * we use that Map rather than copy the contents of it, thus not wasting
     * memory, and garbage collection. In Java we would have to do all the
     * work of delegation our self!
     */
    @Delegate
    Map<String, Object> message

    /** If a validation have been done and failed this will contain the exception. */
    APSValidationException lastValidation

    boolean validate = false

    //
    // Constructors
    //

    /**
     * Creates a new WellDefinedMessage instance.
     */
    protected WellDefinedMessage( ) {
        this.schemaValidator = new MapJsonDocSchemaValidator( validStructure: schema )
    }

    //
    // Methods
    //

    // Note: The Map delegation above creates problems with java bean property auto
    // setters and getters.

    /**
     * Turns on validation of messages.
     */
    SubClass enableValidation() {

        this.validate = true

        this as SubClass
    }

    /**
     * Sets and validates the message.
     *
     * @param message The message to set.
     *
     * @throws APSValidationException on non valid message.
     */
    SubClass setMessage(Map<String, Object> message) {

        if (this.validate) this.schemaValidator.validate(message)

        this.message = message

        this as SubClass
    }

    /**
     * Returns the message.
     */
    Map<String, Object> getMessage() {

        return this.message
    }


    /**
     * Checks if the message is valid according to schema.
     *
     * @return true if valid. If false the lastValidation property will contain the exception.
     */
    boolean isValid() {

        boolean valid = true

        try {

            this.schemaValidator.validate( this.message )
        }
        catch ( APSValidationException validationException ) {

            valid = false
            this.lastValidation = validationException
        }

        valid
    }

}
