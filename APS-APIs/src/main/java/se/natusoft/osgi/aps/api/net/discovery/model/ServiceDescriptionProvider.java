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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2011-10-16: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.discovery.model;

import java.time.LocalDateTime;

/**
 * A default implementation of ServiceProvider.
 */
public class ServiceDescriptionProvider implements ServiceDescription {
    //
    // Private Members
    //

    /** A short description of the service. */
    private String description = "";

    /** An id/name of the service. */
    private String serviceId = "";

    /** The version of the service. */
    private String version = "";

    /** The targetHost of the service. */
    private String serviceHost = "";

    /** The targetPort of the service. */
    private int servicePort = 0;

    /** The protocol of the service. */
    private String serviceProtocol = "";

    /** The network protocol used. */
    private String networkProtocol = "TCP";

    /** Any classification of the service. */
    private String classifier = "";

    /** The type of the content the service deals with. */
    private String contentType = "";

    /** An optional URL to the service. */
    private String serviceURL = "";

    /** Last updated. */
    private LocalDateTime lastUpdated = LocalDateTime.now();

    //
    // Constructors
    //

    /**
     * Creates a new ServiceDescription.
     */
    public ServiceDescriptionProvider() {}

    /**
     * This is copy constructor that copies from the API, not necessarily another instance of this class!
     *
     * @param serviceDescription The ServiceDescription to copy from.
     */
    public ServiceDescriptionProvider(ServiceDescription serviceDescription) {
        this.description = serviceDescription.getDescription();
        this.serviceId = serviceDescription.getServiceId();
        this.version = serviceDescription.getVersion();
        this.serviceHost = serviceDescription.getServiceHost();
        this.serviceProtocol = serviceDescription.getServiceProtocol();
        this.serviceURL = serviceDescription.getServiceURL();
        this.lastUpdated = serviceDescription.getLastUpdated();
    }

    //
    // Methods
    //

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return "{ " +
               "description:         '" + this.description + "'" +
               ", serviceId:           '" + this.serviceId + "'" +
               ", version:             '" + this.version + "'" +
               ", serviceHost:         '" + this.serviceHost + "'" +
               ", servicePort:         '" + this.servicePort + "'" +
               ", service protocol:    '" + this.serviceProtocol + "'" +
               ", network protocol:    '" + this.networkProtocol + "'" +
               ", classifier:          '" + this.classifier + "'" +
               ", contentType:         '" + this.contentType + "'" +
               ", serviceURL:          '" + this.serviceURL + "'" +
               " }";
    }

    /**
     * A short description of the service.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Sets a short description of the service.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * An id/name of the service.
     */
    @Override
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the id of the service.
     *
     * @param serviceId The service id to set.
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * The version of the service.
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the version of the service.
     *
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The targetHost of the service.
     */
    @Override
    public String getServiceHost() {
        return serviceHost;
    }

    /**
     * Sets the targetHost of the service.
     *
     * @param serviceHost The service targetHost to set.
     */
    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    /**
     * The targetPort of the service.
     */
    @Override
    public int getServicePort() {
        return servicePort;
    }

    /**
     * Sets the targetPort of the service.
     *
     * @param servicePort The service targetPort to set.
     */
    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    /**
     * The protocol used over the network. Valid values are "TCP", "UDP", and "MULTICAST"
     */
    @Override
    public String getNetworkProtocol() {
        return this.networkProtocol;
    }

    /**
     * Sets the protocol used over the network.
     *
     * @param networkProtocol The protocol to set.
     */
    public void setNetworkProtocol(String networkProtocol) {
        this.networkProtocol = networkProtocol;
    }

    /**
     * The protocol of the service.
     */
    @Override
    public String getServiceProtocol() {
        return this.serviceProtocol;
    }

    /**
     * Sets the protocol used by the service.
     *
     * @param protocol The protocol to set.
     */
    public void setServiceProtocol(String protocol) {
        this.serviceProtocol = protocol;
    }

    /**
     * An optional URL to the service.
     */
    @Override
    public String getServiceURL() {
        return serviceURL;
    }

    /**
     * This can be anything that classifies the entry as something more specific. Like a group or queue or whatever.
     */
    @Override
    public String getClassifier() {
        return this.classifier;
    }

    /**
     * Provides a classifier.
     *
     * @param classifier The classifier to provide.
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Describes the content type expected/delivered by the service. For example "XML", "JSON", "Binary:Java:Serialized"
     */
    @Override
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Specified the type of content used by the service.
     *
     * @param contentType The content type to set.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets an url to the service.
     *
     * @param serviceURL The service url to set.
     */

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    /**
     * Returns the date of last update.
     */
    @Override
    public LocalDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    /**
     * Sets a new last update time.
     *
     * @param lastUpdated The new time to set.
     */
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * @return The hash code of this ServiceDescriptionProvider.
     */
    @Override
    public int hashCode() {
        int result = this.serviceId != null ? this.serviceId.hashCode() : 0;
        result = 31 * result + (this.version != null ? this.version.hashCode() : 0);
        result = 31 * result + (this.serviceHost != null ? this.serviceHost.hashCode() : 0);
        result = 31 * result + this.servicePort;
        result = 31 * result + (this.serviceProtocol != null ? this.serviceProtocol.hashCode() : 0);
        result = 31 * result + (this.serviceURL != null ? this.serviceURL.hashCode() : 0);
        result = 31 * result + (this.networkProtocol != null ? this.networkProtocol.hashCode() : 0);
        result = 31 * result + (this.contentType != null ? this.contentType.hashCode() : 0);
        result = 31 * result + (this.classifier != null ? this.classifier.hashCode() : 0);
        return result;
    }

    /**
     * Compares with other object for equality.
     *
     * @param o The object to compare to.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceDescription)) return false;

        ServiceDescriptionProvider that = (ServiceDescriptionProvider) o;

        if (this.servicePort != that.servicePort) return false;
        if (this.serviceHost != null ? !this.serviceHost.equals(that.serviceHost) : that.serviceHost != null) return false;
        if (this.serviceId != null ? !this.serviceId.equals(that.serviceId) : that.serviceId != null) return false;
        if (this.version != null ? !this.version.equals(that.version) : that.version != null) return false;
        if (this.serviceProtocol != null ? !this.serviceProtocol.equals(that.serviceProtocol) : that.serviceProtocol != null) return false;
        if (this.serviceURL != null ? !this.serviceURL.equals(that.serviceURL) : that.serviceURL != null) return false;
        if (this.networkProtocol != null ? !this.networkProtocol.equals(that.networkProtocol) : that.networkProtocol != null) return false;
        if (this.contentType != null ? !this.contentType.equals(that.contentType) : that.contentType != null) return false;
        if (this.classifier != null ? !this.classifier.equals(that.classifier) : that.classifier != null) return false;
        return true;
    }
}
