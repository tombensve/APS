package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.http.HttpServerRequest
import io.vertx.groovy.core.http.HttpServerResponse
import se.natusoft.osgi.aps.api.reactive.Consumer
import se.natusoft.osgi.aps.net.vertx.api.VertxConsumer
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import se.natusoft.osgi.aps.web.adminweb.config.ContentServerConfig

/**
 * Delivers HTTP content to a browser.
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
class WebContentServer extends VertxConsumer implements Consumer<Vertx>, Constants {

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-admin-web-a2:web-content-server")
    private APSLogger logger

    /** Maps the requested file names to the path of the actual file. */
    private Map<String, File> serveFiles = [:]

    private Consumer.Consumed<Vertx> vertx

    //
    // Constructors
    //

    WebContentServer() {
        this.onVertxAvailable = { Consumer.Consumed<Vertx> vertx ->
            this.vertx = vertx

            // Default values, can be overridden by loaded options.
            def options = [
                    logActivity: true,
                    listenPort : 9080
            ] as Map<String, Object>

            loadOptions(options)

            def listenPort = options.remove('listenPort') as int

            vertx.get().createHttpServer(options).requestHandler { HttpServerRequest request ->
                request.exceptionHandler { Throwable exception ->

                    this.logger.error(exception.message, exception)
                    request.response()
                            .setStatusMessage(exception.message)
                            .setStatusCode(500)
                            .end()
                }

                handleRequest(request)

            }.listen(listenPort) { AsyncResult res ->
                if (res.succeeded()) {
                    logger.info("Web content HTTP server is up and running!")
                } else {
                    this.logger.error("Web content HTTP server failed to start! [${res.cause().message}]", res.cause())
                }
            }
        }

        this.onVertxRevoked = {
            this.vertx = null
            this.logger.error("Vertx instance was revoked. Will not operate until new instance is available!")
        }
    }

    //
    // Methods
    //

    /**
     * Handles HTTP requests.
     *
     * @param request The request to handle.
     */
    @SuppressWarnings("PackageAccessibility")
    private void handleRequest(HttpServerRequest request) {

        HttpServerResponse response = request.response()

        String reqFile = request.path()
        if (reqFile.trim().isEmpty()) {
            reqFile = "/index.html"
        }
        else if (reqFile.trim() == "/") {
            reqFile = "/index.html"
        }

        File serveFile = fileToServe(reqFile.substring(reqFile.lastIndexOf('/')))
        if (serveFile != null) {
            response.sendFile(serveFile.absolutePath)
        } else {
            response.setStatusMessage("${request.path()} was not found!")
                    .setStatusCode(404)
                    .end()
        }
    }

    /**
     * Since HttpServerResponse does not support sendFile(...) with an InputStream, only path Strings, we have to copy
     * embedded files to temp area and serve from there.
     *
     * The temp files copied to temp area are marked for automatic delete when JVM process dies.
     *
     * This updates the 'serverFiles' Map<String, File> member and thus only copies the files to temp file once (per JVM run).
     *
     * @param requestFile The request file to get on disk File object for.
     */
    private File fileToServe(String requestFile) {
        File serveFile = this.serveFiles[requestFile]
        if (serveFile == null) {
            serveFile = File.createTempFile("aps-admin-web", "${new Random(new Date().getTime()).nextLong()}")
            serveFile.deleteOnExit()

            InputStream embeddedStream = System.getResourceAsStream("/webContent" + requestFile)
            if (embeddedStream == null) {
                return null
            }
            InputStream from = new BufferedInputStream(embeddedStream)
            OutputStream to = new BufferedOutputStream(new FileOutputStream(serveFile))

            boolean done = false
            while (!done) {
                int b = from.read()
                if (b != -1) {
                    to.write(b)
                } else {
                    done = true
                }
            }
            from.close()
            to.close()

            this.serveFiles[requestFile] = serveFile
        }

        serveFile
    }

    /**
     * Loads config options into a Vertx options Map.
     *
     * @param options The map to load options into.
     */
    private static void loadOptions(Map<String, Object> options) {
        ContentServerConfig.managed.get().optionsValues.each { ContentServerConfig.ConfigValue entry ->
            Object value = ""

            // The type value will *always* contain one of these values and nothing else.
            switch (entry.type.string) {
                case "String":
                    value = entry.value.string
                    break

                case "Int":
                    value = Integer.valueOf(entry.value.string)
                    break

                case "Float":
                    value = Float.valueOf(entry.value.string)
                    break

                case "Boolean":
                    value = Boolean.valueOf(entry.value.string)
            }

            options[entry.name.string] = value
        }
    }

    @BundleStop
    void shutdown() {
        if (this.vertx != null) this.vertx.release()

        // It is not a bad idea to delete these even on redeployment or shutdown of bundle for other reasons.
        this.serveFiles.each { String key, File file ->
            if (!file.delete()) { this.logger.error("${file.absolutePath} could not be deleted!") }
        }
    }
}
