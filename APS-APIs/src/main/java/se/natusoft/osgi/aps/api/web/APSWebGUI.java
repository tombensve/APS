package se.natusoft.osgi.aps.api.web;

import java.util.Map;

/**
 * This should be published as an OSGi service with an implementation that provides
 * a valid gui JSON structure for APSWebManager which will track this service.
 *
 * APSWebManager/GUIProvider will track OSGi services of this interface and call it for
 * the JSON it will publish on the bus to new clients.
 */
public interface APSWebGUI<Source> {

    /**
     * This should provide a Map JSON structure that is valid for APSWebManager.
     *
     * @param source A potential source for creating the JSON output. Can be null.
     *               Depends entirely on the implementation.
     */
    Map<String, Object> provideWebGUI(Source source);
}
