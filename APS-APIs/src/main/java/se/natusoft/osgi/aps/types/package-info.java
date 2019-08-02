/**
 * This package is for general models not connected to a specific function.
 *
 * ## Vertx
 * This package has reactive style APIs. These are heavily inspired by Vertx!
 *
 * At first I considered using the Vertx APIs directly, but that would hardcode Vertx into APS.
 * Yes, currently APS is heavily based on Vertx, but it goes heavily against my inner reflexes
 * to hardcode dependencies, and this is also OSGi code which makes it trivially easy to
 * embed and conceal things with public APIs for functionality. The APS-APIs bundle have
 * very few external dependencies.
 *
 */
package se.natusoft.osgi.aps.types;
