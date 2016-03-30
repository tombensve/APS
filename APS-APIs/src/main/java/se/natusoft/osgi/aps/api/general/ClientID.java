package se.natusoft.osgi.aps.api.general;

import java.util.UUID;

/**
 * This is a general client identification for when such are needed.
 */
public final class ClientID {

    //
    // Private Members
    //

    /** An optional name of the client. */
    private String name = "<anonymous>";

    /** A unique id for the client. */
    private UUID id = UUID.randomUUID();

    //
    // Constructors
    //

    /**
     * Creates an anonymous client ID.
     */
    public ClientID() {}

    /**
     * Creates a named client ID.
     *
     * @param name The name of this client.
     */
    public ClientID(String name) {
        this.name = name;
    }

    //
    // Methods
    //

    /**
     * Returns the ID of this client.
     */
    public final UUID getId() {
        return this.id;
    }

    /**
     * Returns the name of this client.
     */
    public final String getName() {
        return this.name;
    }
}
