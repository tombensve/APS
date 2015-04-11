package se.natusoft.osgi.aps.util;

/**
 * This is a utility base class.
 */
public class APSObject {
    //
    // Private Members
    //

    /** Flag to keep track of if delayedInit() has been called or not. */
    private boolean initDone = false;

    //
    // Methods
    //

    /**
     * A call to this will call delayedInit() once and only once.
     */
    protected void init() {
        if (!this.initDone) {
            delayedInit();
            this.initDone = true;
        }
    }

    /**
     * This does nothing, it is intended to be overridden.
     */
    protected void delayedInit() {}
}
