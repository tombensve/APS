/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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

/**
 * Describes a service.
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

    /** The targetPort of the service.*/
    private int servicePort = 0;

    /** An optional URL to the service. */
    private String serviceURL = "";

    //
    // Constructors
    //

    /**
     * Creates a new ServiceDescirption.
     */
    public ServiceDescriptionProvider() {}

    //
    // Methods
    //

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return "    description:         '" + this.description + "'\n" +
               "    serviceId:           '" + this.serviceId + "'\n" +
               "    version:             '" + this.version + "'\n" +
               "    serviceHost:         '" + this.serviceHost + "'\n" +
               "    servicePort:         '" + this.servicePort + "'\n" +
               "    serviceURL:          '" + this.serviceURL + "'\n";
    }

    /**
     * A short description of the service.
     */
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
     * An optional URL to the service.
     */
    @Override
    public String getServiceURL() {
        return serviceURL;
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
     * @return The hash code of this ServiceDescriptionProvider.
     */
    @Override
    public int hashCode() {
        int result = this.serviceId != null ? this.serviceId.hashCode() : 0;
        result = 31 * result + (this.version != null ? this.version.hashCode() : 0);
        result = 31 * result + (this.serviceHost != null ? this.serviceHost.hashCode() : 0);
        result = 31 * result + this.servicePort;
        result = 31 * result + (this.serviceURL != null ? this.serviceURL.hashCode() : 0);
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
        if (this.serviceURL != null ? !this.serviceURL.equals(that.serviceURL) : that.serviceURL != null) return false;

        return true;
    }

}
