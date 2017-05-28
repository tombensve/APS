package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.LocalEventBus
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

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
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class AdminWebsProvider implements Constants {
    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed(loggingFor = "aps-admin-web-a2:admin-webs-provider")
    private APSLogger logger

    @Managed
    private LocalEventBus localBus

    @OSGiService
    private APSAdminWebService adminWebService

    //
    // Init
    //

    @Initializer
    void init() {
        this.logger.connectToLogService(this.context)
        this.localBus.subscribe(LOCAL_BUS_ADDRESS) { Map<String, Object> event ->
            try {
                EventDefinition.validate(event)

                switch (event[_body_][_action_] as String) {
                    case ACTION_GET_WEBS:
                        handleGetWebs(event)
                        break
                }

            }
            catch (Exception e) {
                this.logger.error("Problem with received event: ${e.message}")
                // Smells like a Groovy bug! "Constants." is required for a pub, but not for the get call above!
                //noinspection UnnecessaryQualifiedReference
                event[Constants._error_] =
                        EventDefinition.createError(code: 1, message: "Problem with received event: ${e.message}" as Object)
            }

        }
    }

    private void handleGetWebs(@NotNull Map<String, Object> event) {

    }
}
