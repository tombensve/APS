package se.natusoft.osgi.aps.net.vertx.api

/**
 * This is just a marker interface for indicating that the consumer is also interested in the Vertx web Router.
 *
 * All receiving services have to do is to implement this, extend VertxConsumer and install a closure hook on onRouterAvailable.
 */
interface WebRouterConsumer {}
