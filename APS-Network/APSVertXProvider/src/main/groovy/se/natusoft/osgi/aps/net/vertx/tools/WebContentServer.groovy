package se.natusoft.osgi.aps.net.vertx.tools

import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Serves static content from resources/webContent.
 *
 * 1. Track a configured Vertx Router.
 * 2. Do `new WebContentServer(bundleContext: this.bundleContext, router: router, servePath: "/myweb")`
 *
 * That's it! It will now serve resource files under /webContent in your bundle. So _http://myhost:1234/myweb/_
 * will for example fetch index.html.
 *
 * Once Vert.x is upp and is serving anything including web content it will continue to do so until Vert.x
 * is shut down. Vert.x will not shut down until aps-vertx-provider bundle is stopped. This wil affect all
 * deployed bundles using Vert.x. So it is basically impossible to stop individual routers while running others.
 *
 * However wanting to publish TCP or HTTP servers for only a short time is very unlikely.
 */
class WebContentServer {

    //
    // Private Members
    //

    //
    // Properties
    //

    /** The context of the bundle using this. This is needed to serve resources from correct bundle. */
    @NotNull
    BundleContext context

    /** The router to serve static files for. */
    @NotNull
    Router router

    /** The path to serve at. */
    @NotNull
    String servePath

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
    WebContentServer() {}

    //
    // Methods
    //

    void setBundleContext( @NotNull BundleContext context ) {
        this.context = context

        if ( this.router != null && this.servePath != null ) startServing()
    }

    void setRouter( @NotNull Router router ) {
        this.router = router

        if ( this.context != null && this.servePath != null ) startServing()
    }

    void setServePath( String servePath ) {
        this.servePath = servePath

        if ( this.router != null && this.context != null ) startServing()
    }

    private void startServing() {
        this.router.route( this.servePath ).handler { RoutingContext context ->
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

        URL resourceURL =
                this.context.getBundle().getResource( "/webContent" + reqFile.substring( this.servePath.length() ) )

        InputStream embeddedStream = resourceURL.openStream()

        if ( embeddedStream == null ) {

            request.response().setStatusMessage( "${ request.path() } was not found!" )
                    .setStatusCode( 404 )
                    .end()
        }
        else {
            InputStreamReader reader = new InputStreamReader( embeddedStream )
            char[] buffer = new char[1000]
            boolean done = false

            while ( !done ) {

                int read = reader.read( buffer )

                if ( read < 1000 ) {

                    done = true
                    String part = read > 0 ? new String( buffer, 0, read) : ""
                    request.response().end( part )
                }
                else {

                    request.response(  ).write( new String(buffer) )
                }
            }

            reader.close(  )
        }

    }

}
