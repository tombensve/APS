package se.natusoft.osgi.aps.tools;

/**
 * This should be injected with @Managed. When this is available APSActivator will not by default register the service with
 * the OSGi platform. The service itself have to set state READY for that to happen.
 */
public interface APSActivatorInteraction {

    /**
     * Holds the signal that can be send.
     */
    enum State {
        /** Indicates that the service is in a startup state. */
        IN_STARTUP,

        /** Indicates the the service is ready to be used. APSActivator will register service with OSGi platform when it sees this. */
        READY,

        /** Indicates that startup failed, and that APSActivator should shutdown */
        STARTUP_FAILED
    }

    /** The default value to use for state. */
    State DEFAULT_VALUE = State.IN_STARTUP;

    /**
     * Sends signals to the APSActivator.
     *
     * @param state The state to set.
     */
    void setState(State state);

    /**
     * Returns the current state.
     */
    State getState();
}
