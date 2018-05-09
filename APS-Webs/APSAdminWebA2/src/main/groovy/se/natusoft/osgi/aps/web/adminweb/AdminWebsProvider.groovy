package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.net.vertx.api.VertxSubscriber
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService

// IDEA shows this as not used, but that is not true!! This class will not compile without this import.
/**
 * General services.
 *
 * __GroovyUnusedDeclaration__
 *
 * There are complains that this class is not used, this because it is managed by APSActivator and the IDE
 * cannot see the code that creates and manages this instance.
 *
 * __PackageAccessibility__
 *
 * This is an OSGi issue. OSGi imports and exports packages, and to be deployable a jar must contain a
 * valid MANIFEST.MF with OSGi keys for imports, exports, etc. Must 3rd party jars do contain a valid
 * OSGi MANIFEST.MF exporting all packages of the jar sp that they can just be dropped into an OSGi
 * container and have their classpath be made available to all other code running in the container.
 *
 * The Groovy Vertx wrapper code does not contain a valid OSGi MANIFEST.MF. I have solved this by having
 * the aps-vertx-provider bundle include the Groovy Vertx wrapper, and export all packages of that
 * dependency. So as long as the aps-vertx-provider is deployed the Groovy Vertx wrapper code will
 * also be available runtime. IDEA however does not understand this. It does not figure out the
 * exported dependency from aps-vertx-provider either. So it sees code that is not OSGi compatible
 * and used in the code without including the dependency jar in the bundle, and complains about
 * that. But since in reality this code will be available at runtime I just hide these incorrect
 * warnings.
 */
@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
class AdminWebsProvider extends VertxSubscriber implements Constants {
    //
    // Constants
    //

    private static final String RESOLVE_ADDRESS = "APSAdminWebA2-Resolver"

    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed( loggingFor = "aps-admin-web-a2:admin-webs-provider" )
    private APSLogger logger

    @OSGiService
    private APSAdminWebService adminWebService

    /** Consumer of public event-bus messages. */
    private MessageConsumer eventConsumer

    //
    // Init
    //

    @Initializer
    void init() {
        this.logger.connectToLogService( this.context )
        this.useLogger = this.logger // Provide logger to base class.

        this.onVertxAvailable = { Vertx vertx ->

            this.eventConsumer = vertx.eventBus().consumer( NODE_ADDRESS ).handler { Message message ->
                Map<String, Object> event = getBody( message )

                try {
                    EventDefs.EVENT_VALIDATOR.validate( event )

                    JsonObject resp

                    Event ev = new Event( event )
                    switch ( ev.eventType ) {
                        case "req-webs":
                            resp = provideWebs()
                            break
                        default:
                            resp = new Event( NODE_ADDRESS )
                                    .error( 1, "Only 'req-webs' is allowed for eventType!" )
                                    .toJson()
                            eventBusReply( message, resp )
                    }

                    eventBusReply( message, resp )
                }
                catch ( APSValidationException ve ) {
                    eventBusReply(
                            message,
                            new Event( NODE_ADDRESS )
                                    .error( 10, "Bad message!" )
                                    .toJson() as JsonObject
                    )
                    this.logger.error( "Bad message received! (${ message })", ve )
                }

            }

        }
    }

    private static JsonObject provideWebs() {

        // @formatter:off (IDEA does a really poor job here!)
        new Event( NODE_ADDRESS )
                .eventType( "provide-webs" )
                .data( [
                    webs: [
                            [
                                    name: "Network",
                                    url : "http://localhost/apsadminweb/net"
                            ],
                            [
                                    name: "Users",
                                    url : "http://localhost/apsadminweb/users"
                            ]
                    ]
                ] as Map<String, Object> ).toJson()
        // @formatter:on

    }
}
