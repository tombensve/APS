/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
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
 *         2012-02-02: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * As the name indicates, a trivial data bus. It can be used to bind together different parts of a service implementation.
 */
public class TrivialDataBus<DataDescription, Data> {
    //
    // Private Members
    //

    /** The members of the bus. */
    private List<TrivialBusMember> members = new LinkedList<TrivialBusMember>();

    //
    // Constructors
    //

    /**
     * Creates a new TrivialDataBus instance.
     */
    public TrivialDataBus() {}

    //
    // Methods
    //

    /**
     * Adds a member to the bus.
     * 
     * @param member The member to add.
     */
    public void addMember(TrivialBusMember<DataDescription, Data> member) {
        this.members.add(member);
        member.memberOf(this);
    }

    /**
     * Removes a member from the bus.
     * 
     * @param member The member to remove.
     */
    public void removeMember(TrivialBusMember<DataDescription, Data> member) {
        this.members.remove(member);
    }

    /**
     * Sends data on the bus.
     *
     * @param dataDescription The description of the data to send.
     * @param data The data to send.
     */
    public void sendData(DataDescription dataDescription, Data data) {
        for (TrivialBusMember<DataDescription, Data> member : this.members) {
            if (member instanceof TrivialBusReceivingMember) {
                ((TrivialBusReceivingMember<DataDescription, Data>)member).dataReceived(dataDescription, data);
            }
        }
    }
    
    //
    // Inner Classes
    //

    /**
     * Defines the trivial bus member API.
     *
     * @param <DataDescription> An object that describes the sent and received data.
     * @param <Data> The actual data.
     */
    public static interface TrivialBusMember<DataDescription, Data> {

        /**
         * When a member is added to a bus this is called to receive the bus being added to.
         *
         * @param bus The bus the member now is part of.
         */
        public void memberOf(TrivialDataBus<DataDescription, Data> bus);

    }

    /**
     * Defines a trivial bus member that also receives data.
     *
     * @param <DataDescription> An object that describes the sent and received data.
     * @param <Data> The actual data.
     */
    public static interface TrivialBusReceivingMember<DataDescription, Data> extends TrivialBusMember<DataDescription, Data> {
        /**
         * Data is received on the bus.
         *
         * @param dataDescription The description of the data received.
         * @param data The data received.
         */
        public void dataReceived(DataDescription dataDescription, Data data);
    }

    /**
     * For passing on a TrivialDataBus to request some data. This is a pure utility and does not have to be used!
     */
    public static abstract class TrivialDataRequest<Type> {
        //
        // Private Members
        //

        /** The type of data requested. */
        private Class<Type> requestedDataType = null;

        /** Query data for more specifically specifying what data is wanted. */
        private Object[] queryData = null;

        //
        // Constructors
        //

        /**
         * Creates a new DataRequest instance.
         *
         * @param requestedDataType The type of data wanted.
         * @param queryData Optional query data for specifying what data to return.
         */
        public TrivialDataRequest(Class<Type> requestedDataType, Object... queryData) {
            this.requestedDataType = requestedDataType;
            this.queryData = queryData;
        }

        //
        // Methods
        //

        /**
         * @return The data type of the requested data.
         */
        public Class<Type> getRequestedDataType() {
            return this.requestedDataType;
        }

        /**
         * @return The query data for the data request.
         */
        public Object[] getQueryData() {
            return this.queryData;
        }
    }

    /**
     * For passing on a TrivialDataBus to request some data. This is a pure utility and does not have to be used!
     */
    public static class TrivialSingleDataRequest<Type> extends TrivialDataRequest<Type> {
        //
        // Private Members
        //

        /** The requested data. */
        private Type data = null;

        //
        // Constructors
        //

        /**
         * Creates a new DataRequest instance.
         *
         * @param requestedDataType The type of data wanted.
         * @param queryData Optional query data for specifying what data to return.
         */
        public TrivialSingleDataRequest(Class<Type> requestedDataType, Object... queryData) {
            super(requestedDataType, queryData);
        }

        //
        // Methods
        //

        /**
         * Sets the data asked for.
         *
         * @param data The data to set.
         */
        public void setData(Type data) {
            this.data = data;
        }

        /**
         * @return The delivered data.
         */
        public Type getData() {
            return this.data;
        }
    }
    
    /**
     * For passing on a TrivialDataBus to request some data. This is a pure utility and does not have to be used!
     */
    public static class TrivialManyDataRequest<Type> extends TrivialDataRequest<Type> {
        //
        // Private Members
        //

        /** The requested data. */
        private List<Type> data = null;

        //
        // Constructors
        //

        /**
         * Creates a new DataRequest instance.
         *
         * @param requestedDataType The type of data wanted.
         * @param queryData Optional query data for specifying what data to return.
         */
        public TrivialManyDataRequest(Class<Type> requestedDataType, Object... queryData) {
            super(requestedDataType, queryData);
        }

        //
        // Methods
        //

        /**
         * Sets the data asked for.
         *
         * @param data The data to set.
         */
        public void setData(List<Type> data) {
            this.data = data;
        }

        /**
         * @return The delivered data.
         */
        public List<Type> getData() {
            return this.data;
        }
    }
    
    /**
     * For passing on a TrivialDataBus to request some data. This is a pure utility and does not have to be used!
     */
    public static class TrivialMapDataRequest<Key, Type> extends TrivialDataRequest<Type> {
        //
        // Private Members
        //

        /** The requested data. */
        private Map<Key,Type> data = null;

        //
        // Constructors
        //

        /**
         * Creates a new DataRequest instance.
         *
         * @param requestedDataType The type of data wanted.
         * @param queryData Optional query data for specifying what data to return.
         */
        public TrivialMapDataRequest(Class<Type> requestedDataType, Object... queryData) {
            super(requestedDataType, queryData);
        }

        //
        // Methods
        //

        /**
         * Sets the data asked for.
         *
         * @param data The data to set.
         */
        public void setData(Map<Key, Type> data) {
            this.data = data;
        }

        /**
         * @return The delivered data.
         */
        public Map<Key, Type> getData() {
            return this.data;
        }
    }
}
