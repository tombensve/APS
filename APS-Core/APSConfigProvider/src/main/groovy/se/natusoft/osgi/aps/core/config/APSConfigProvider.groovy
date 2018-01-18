package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
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
     * This gets called when the configuration is available. This only works if APSServiceTracker wrapping as a
     * service functionality is used, and that 'nonBlocking' is set to true.
     *
     * Example:
     *
     *     @OSGiService ( additionalSearchCriteria = "(APS-Config-Id=myconf)" nonBlocking=true )
     *     private APSConfig config
     *
     * In this case any call made before the service actually been published will be cached by the underlaying
     * APSServiceTracker, and when the service becomes available all the cached calls will be performed on the
     * service. This will semi-work if nonBlocking=true but then the onConfigReady(...) call will block until
     * the service is available. That would also happen if you just try to get a configuration value. So there
     * would be no point in using onConfigReady(...) in that case.
     *
     * @param handler The handler to call when there is actual config data in the map.
     */
    @Override
    void onConfigReady( Runnable handler ) {
        handler.run()
    }

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
    void withStructPath( Consumer<String> pathHandler ) {

    }

    /**
     * Returns all struct paths as a List.
     */
    @Override
    List<String> getStructPaths() {
        return null
    }

    @Override
    Object lookup( String structPath ) {
        Object value = super.lookup( structPath )
        if ( value == null ) {
            value = this.defaultConfig.lookup( structPath )
            if ( value != null ) {
                super.provide( structPath, value )
                this.updatedNotifier( this )
            }
        }

        value
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
