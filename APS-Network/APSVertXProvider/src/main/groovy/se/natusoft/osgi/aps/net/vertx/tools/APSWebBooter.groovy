package se.natusoft.osgi.aps.net.vertx.tools

import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Serves static content from resources/webContent.
 *
 * 1. Track a configured Vertx Router.
 * 2. Do `new WebContentServer(bundleContext: this.bundleContext, router: router, servePath: "/myweb", serveFilter:"*")`
 *
 * That's it! It will now serve resource files under /webContent in your bundle. So _http://myhost:1234/myweb/_
 * will for example fetch index.html.
 *
 * Once Vert.x is upp and is serving anything including web content it will continue to do so until Vert.x
 * is shut down. Vert.x will not shut down until aps-vertx-provider bundle is stopped. This wil affect all
 * deployed bundles using Vert.x. So it is basically impossible to stop individual routers while running others.
 *
 * On the other side, wanting to publish TCP or HTTP servers for only a short time is very unlikely so this is
 * not a problem.
 *
 * ### Note
 *
 * This is intended for booting SPA client side apps. After the client app is upp and running it will/can not
 * call this server again. It only serves static boot files. So to communicate with the backend another route
 * has to be setup for that, or the event bus has to be used. The APS web apps use the event bus. The
 * aps-vertx-provider can be configured to automatically setup a SOCKJS event bus bridge.
 */
class APSWebBooter {

    //
    // Properties
    //

    /** The context of the bundle using this. This is needed to serve resources from correct bundle. */
    @NotNull
    BundleContext context

    /** The router to serve static files for. */
    @NotNull
    Router router

    /** The path to serve at. If you want to serve anything under path don't forget to end with '.../*'. */
    @NotNull
    String servePath

    /** A filter for what is allowed under the serve path. */
    @NotNull
    String serveFilter

    /** The resource path. Default '/webContent' */
    @NotNull
    String resourcePath = "webContent"

    /** The logger to use. */
    @NotNull
    APSLogger logger

    //
    // Constructor
    //

    /**
     * Creates a new WebContentServer. It will start serving when it has gotten all 3 of BundleContext, Router,
     * and servePath.
     */
    APSWebBooter() {}

    //
    // Methods
    //

    @SuppressWarnings( "unused" )
    void setBundleContext( @NotNull BundleContext context ) {
        this.context = context
    }

    void setRouter( @NotNull Router router ) {
        this.router = router
    }

    void setServePath( String servePath ) {
        this.servePath = servePath
    }


    void validate() {
        if ( this.context == null ) throw new APSValidationException( "Missing 'BundleContext'! This is required." )
        if ( this.router == null ) throw new APSValidationException( "Missing 'Router'! This is required." )
        if ( this.servePath == null ) throw new APSValidationException( "Missing 'servePath'! This is required!" )
    }

    APSWebBooter serve() {
        validate()

        String finalServePath = this.servePath
        if ( !finalServePath.endsWith( "/" ) ) {
            finalServePath += "/"
        }

        finalServePath += this.serveFilter.startsWith( "/" ) ? this.serveFilter.substring( 1 ) : this.serveFilter

        this.router.route( finalServePath ).handler { RoutingContext context ->
            handleRequest( context.request().exceptionHandler
                    { Throwable exception ->

                        this.logger.error( exception.message, exception )
                        context.response()
                                .setStatusMessage( exception.message )
                                .setStatusCode( 500 )
                                .end()
                    }
            )
        }

        this
    }

    /**
     * Since we don't have control over the content of the parameters, make sure there are '/' available, and
     * not to many.
     */
    private String resourcePath(String reqFile) {
        String resourcePathStart = this.resourcePath.endsWith( "/" ) ? this.resourcePath : this.resourcePath + "/"

        String resourcePathEnd = reqFile.substring( this.servePath.length() )
        resourcePathEnd = resourcePathEnd.startsWith( "/" ) ? resourcePathEnd.substring( 1 ) : resourcePathEnd

        resourcePathStart + resourcePathEnd
    }

    /**
     * Serves requested files from resource /webContent classpath.
     *
     * @param request The received request.
     */
    @SuppressWarnings( "PackageAccessibility" )
    private void handleRequest( @NotNull HttpServerRequest request ) {
        String reqFile = request.path().trim()
        if ( reqFile.endsWith( "/" ) ) {
            reqFile = reqFile + "index.html"
        }

        String resourcePath = resourcePath( reqFile )

        this.logger.debug( "resourcePath: ${ resourcePath }" )

        URL resourceURL = this.context.getBundle().getResource( resourcePath )

        if (resourceURL == null) {

            String message = "HTTP resource: '${resourcePath}' does not exist!"
            this.logger.error(message)
            request.response(  ).statusCode = 404
            request.response(  ).statusMessage = message
            request.response(  ).end(  )

            return
        }

        InputStream embeddedStream = resourceURL.openStream()

        if ( embeddedStream == null ) {

            request.response().setStatusMessage( "${ request.path() } was not found!" )
                    .setStatusCode( 404 )
                    .end()
        }
        else {
            // Since the files we serve are embedded within a jar file it is rather difficult to
            // find their size. Yes, it would be possible to read the whole file into a String,
            // then check its size, and write the whole String as a response with the known size.
            // This  will however load files completely into memory before delivering their content.
            // That does not feel like a good idea. So the easy solution is to set the response mode
            // to 'chunked' and pass along chunks as they are read. This might not be optimal for
            // the receiving end, but this is only used to deliver a few base files like index.html,
            // css, and some javascripts for an SPA JS app. It will not cause a performance problem.
            // Once the client app is upp and running all communications with the backend is done over
            // the event bus. This server is boot only.
            request.response(  ).chunked = true

            InputStreamReader reader = new InputStreamReader( embeddedStream )
            int buffSize = 1000
            char[] buffer = new char[buffSize]
            boolean done = false

            while ( !done ) {

                int read = reader.read( buffer )

                if ( read < buffSize ) {

                    done = true
                    String part = read > 0 ? new String( buffer, 0, read ) : ""
                    request.response().end( part )
                }
                else {

                    request.response().write( new String( buffer ) )
                }
            }

            reader.close()
        }

    }

}
