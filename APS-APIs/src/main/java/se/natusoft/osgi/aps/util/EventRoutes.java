package se.natusoft.osgi.aps.util;

public enum EventRoutes {

    // Locally within the client between components.
    CLIENT("client"),

    // Backend for the app using round robin strategy on send.
    BACKEND("backend"),

    // Delivers message to all listeners of the address in the whole cluster.
    ALL("all"),

    //
    ALL_CLIENTS("all:client"),

    ALL_BACKENDS("all:backend"),

    LOCAL("local"),

    NONE("none");

    private String route;

    EventRoutes(String route) {
        this.route = route;
    }

    public String getRoute() {
        return this.route;
    }

    public String toString() {
        return this.route;
    }
}

