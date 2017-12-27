package se.natusoft.osgi.aps.api.util;

import se.natusoft.docutations.Optional;

/**
 * For services that support it, the passed Runnable will be called when this service is ready to work.
 *
 * This to provide a non blocking API.
 *
 * This can be extended by service APIs to be consistent with this functionality.
 */
public interface OnReady {

    /**
     * Provides a callback that gets called when service is ready to work.
     *
     * @param onReady The callback to call when service is ready to work.
     */
    @Optional
    void onReady(Runnable onReady);

    /**
     * Provides a callback that gets called when service is no longer in a ready state.
     *
     * @param onNotReady The callback to call when service is no longer in ready state.
     */
    @Optional
    void onNotReady(Runnable onNotReady);
}
