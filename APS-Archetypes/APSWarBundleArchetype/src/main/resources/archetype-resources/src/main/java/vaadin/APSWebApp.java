package ${package};

import se.natusoft.osgi.aps.tools.web.ClientContext;
import se.natusoft.osgi.aps.tools.web.vaadin.APSTheme;
import se.natusoft.osgi.aps.tools.web.vaadin.APSVaadinOSGiApplication;

/**
 *
 */
public class APSWebApp extends APSVaadinOSGiApplication {

    //
    // Vaadin GUI init
    //

    /**
     * Initializes services used by the application.
     *
     * @param clientContext The client context for accessing services.
     */
    @Override
    public void initServices(ClientContext clientContext) {
    }

    /**
     * Called when session is about to die to cleanup anything setup in initServices().
     *
     * @param clientContext The context for the current client.
     */
    @Override
    public void cleanupServices(ClientContext clientContext) {
    }

    /**
     * Creates the application GUI.
     */
    @Override
    public void initGUI() {

        this.setTheme(APSTheme.THEME);
    }

}
