package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.reactive.APSHandler
import se.natusoft.osgi.aps.core.lib.StructMap

import java.util.function.Consumer

/**
 * This class represents one individual configuration.
 */
@CompileStatic
@TypeChecked
class APSConfigProvider extends StructMap implements APSConfig {

    //
    // Properties
    //

    /** The Id of this config. */
    String apsConfigId

    /** The default config. */
    StructMap defaultConfig

    /** Call on data updates. */
    Closure updatedNotifier

    //
    // Methods
    //

    /**
     * Looks up the value of a specified struct Path.
     *
     * @param structPath The structPath to lookup.
     *
     * @return The value or null.
     */
    /**
     * Calls the provided handler for each value path in the map.
     *
     * @param pathHandler The handler to call with value paths.
     */
    @Override
    void withStructPath( APSHandler<String> pathHandler ) {

    }

    @Override
    void lookup( String structPath, APSHandler<Object> valueHandler ) {
        Object value = super.lookup( structPath )
        if ( value == null ) {
            value = this.defaultConfig.lookup( structPath )
            if ( value != null ) {
                super.provide( structPath, value )
                this.updatedNotifier( this )
            }
        }

        valueHandler.handle( value )
    }

    /**
     * provides a value.
     *
     * @param structPath The value path.
     * @param value The value.
     */
    @Override
    void provide( String structPath, Object value ) {
        super.provide( structPath, value )

        this.updatedNotifier( this, structPath, value )
    }
}
