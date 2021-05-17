package se.natusoft.aps.api.core.platform.service;

/**
 * For services that wants to know this, should implement this as a
 * service using @AutoService.
 */
public interface APSServiceLifecycle {

    /**
     * Called on startup.
     */
    void start();

    /**
     * Called on shutdown.
     */
    void stop();
}
