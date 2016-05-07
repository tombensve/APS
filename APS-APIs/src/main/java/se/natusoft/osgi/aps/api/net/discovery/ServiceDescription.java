package se.natusoft.osgi.aps.api.net.discovery;

import java.util.Properties;

/**
 * A service description is only a set of Properties, this is just a helper/convenience.
 *
 * Any property can be set, not only those with setters! The setters match the keys in
 * DiscoveryKeys.
 */
public class ServiceDescription extends Properties {

    //
    // Constructors
    //

    public ServiceDescription() {}

    public ServiceDescription(Properties props) {
        putAll(props);
    }

    //
    // Methods
    //

    public void setName(String name) {
        setProperty(DiscoveryKeys.NAME, name);
    }

    public String getName() {
        return getProperty(DiscoveryKeys.NAME);
    }

    public void setVersion(String version) {
        setProperty(DiscoveryKeys.VERSION, version);
    }

    public String getVersion() {
        return getProperty(DiscoveryKeys.VERSION);
    }

    public void setApsUri(String apsUri) {
        setProperty(DiscoveryKeys.APS_URI, apsUri);
    }

    public String getApsUri() {
        return getProperty(DiscoveryKeys.APS_URI);
    }

    public void setUrl(String url) {
        setProperty(DiscoveryKeys.URL, url);
    }

    public String getUrl() {
        return getProperty(DiscoveryKeys.URL);
    }

    public void setPort(String port) {
        setProperty(DiscoveryKeys.PORT, port);
    }

    public String getPort() {
        return getProperty(DiscoveryKeys.PORT);
    }

    public void setHost(String host) {
        setProperty(DiscoveryKeys.HOST, host);
    }

    public String getHost() {
        return getProperty(DiscoveryKeys.HOST);
    }

    public void setContentType(String contentType) {
        setProperty(DiscoveryKeys.CONTENT_TYPE, contentType);
    }

    public String getContentType() {
        return getProperty(DiscoveryKeys.CONTENT_TYPE);
    }

    public void setDescription(String description) {
        setProperty(DiscoveryKeys.DESCRIPTION, description);
    }

    public String getDescription() {
        return getProperty(DiscoveryKeys.DESCRIPTION);
    }

    public void setLastUpdated(String lastUpdated) {
        setProperty(DiscoveryKeys.LAST_UPDATED, lastUpdated);
    }

    public String getLastUpated() {
        return getProperty(DiscoveryKeys.LAST_UPDATED);
    }

    public void setProtocol(String protocol) {
        setProperty(DiscoveryKeys.PROTOCOL, protocol);
    }

    public String getProtocol() {
        return getProperty(DiscoveryKeys.PROTOCOL);
    }
}
