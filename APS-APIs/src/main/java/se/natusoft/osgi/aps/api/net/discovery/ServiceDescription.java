/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2016-06-12: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.discovery;

import se.natusoft.osgi.aps.activator.annotation.DiscoveryKeys;

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
