package se.natusoft.osgi.aps.core.lib

import se.natusoft.aps.exceptions.APSConfigException
import se.natusoft.aps.exceptions.APSIOException
import se.natusoft.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.util.APSJson
import se.natusoft.osgi.aps.util.APSResourceLoader

/**
 * This class contains static functionality to fetch a JSON configuration file.
 *
 * It will first look at resource path apsconfig/(config_id)-config.json and if not found it
 * will look at apsconfig/default-(config_id)-config.json.
 *
 * The config file will be validated by apsconfig/(config_id)-schema.json if such exists.
 *
 * The point here is to provide with each bundle:
 *
 *     apsconfig/
 *         (config_id)-schema.json
 *         default-(config_id)-config.json
 *
 * and to provide a configuration that differs from bundle default, package:
 *
 *     apsconfig/
 *         (config_id)-config.json
 *
 * in a jar file and deploy it in _dependenciesDir_. It is possible to put multiple configs in
 * same jar. When (config_id)-config.json is found by below code it will be used instead of the
 * default. This will also be validated if a schema exists.
 *
 * This is as trivially easy it can get and still be able to provide configuration outside of
 * bundles.
 */
class APSConfigLoader {

    static Map<String, Object> get( String configId ) throws APSConfigException {

        String resource = "/apsconfig/${configId}-config.json"
        InputStream confs = APSResourceLoader.asInputStream( resource )
        if ( confs == null ) {
            resource = "/apsconfig/default-${configId}-config.json"
            confs = APSResourceLoader.asInputStream( resource )

            if ( confs == null ) {
                throw new APSConfigException( "No configuration with id '${configId}' was found!" )
            }
        }

        Map<String, Object> config

        try {
            config = APSJson.readObject( confs )
        }
        catch ( APSIOException ioe ) {
            throw new APSConfigException("Failed to read config(${configId})!", ioe)
        }

        InputStream schemas = System.getResourceAsStream( "/apsconfig/${configId}-schema.json" )
        if (schemas != null) {
            try {
                Map<String, Object> schema = APSJson.readObject( schemas )
                try {
                    new MapJsonSchemaValidator( validStructure: schema ).validate( config )
                }
                catch (APSValidationException ve) {
                    throw new APSConfigException("Config file for '${configId}' failed validation!", ve)
                }
            }
            catch ( APSIOException ioe ) {
                throw new APSConfigException("Found config schema (${configId}), but failed to read it!", ioe)
            }
        }

        config
    }
}
