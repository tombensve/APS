package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.sockjs.SockJSHandler
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.reactive.Consumer
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

/**
 * Provides a Vertx EventBus bridge.
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
@SuppressWarnings(["GroovyUnusedDeclaration", "PackageAccessibility"])
@CompileStatic
@TypeChecked
@OSGiServiceProvider(properties = [@OSGiProperty(name = "consumed", value = "vertx")])
class SockJSEventBusBridge implements Consumer<Vertx>, Constants {
    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed(loggingFor = "aps-admin-web-a2:sockjs-eventbus-bridge")
    private APSLogger logger

    /** A Vertx instance. Received in onObjectAvailable(...). */
    private Consumer.Consumed<Vertx> vertx

    //
    // Methods
    //

    /**
     * Called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService(this.context)
    }

    /**
     * Specific options for the consumer.
     */
    @Override
    Properties consumerOptions() { return null }

    /**
     * Called with requested object type when available.
     *
     * @param object The received object.
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void onConsumedAvailable(Consumer.Consumed<Vertx> vertx) {
        this.vertx = vertx

        Router router = Router.router(this.vertx.get())

        // Currently no more detailed permissions than on target address. Might add limits on message contents
        // later.
        def twowaysPermitted1 = [address: BUS_ADDRESS]

        SockJSHandler sockJSHandler = SockJSHandler.create(this.vertx.get()).bridge([
                inboundPermitteds : [twowaysPermitted1] as Object,
                outboundPermitteds: [twowaysPermitted1] as Object
        ])

        // TODO: Need to pass HTTP requests to router.accept(...)!
        router.route("/eventbus/*").handler(sockJSHandler)

        this.logger.info "Vert.x SockJSHandler for event bus bridging started successfully!"

    }

    /**
     * Called when there is a failure to deliver requested object.
     */
    @Override
    void onConsumedUnavailable() {
        this.logger.error "Failed to setup SockJSHandler due to no Vertx instance available!"
    }

    /**
     * Called if/when a previously made available object is no longer valid.
     */
    @Override
    void onConsumedRevoked() {
        this.vertx = null
        this.logger.warn "Vertx instance have been revoked! Until new becomes available there will be no server access!"
    }

    @BundleStop
    void shutdown() {
        if (this.vertx != null) this.vertx.release()
    }
}
