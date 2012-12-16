/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         1.0.0
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
 *         2012-01-04: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Sends data to one or more data receivers. This is easiest used as a base class when possible.
 * <p/>
 * This is intended for primitive data sharing between decoupled classes. And yes, this is basically
 * events! I just didn't want to call it that for reasons I'm not entirely sure of :-)
 */
public class DataTransmitter<DataDescription, Data> {
    //
    // Private Members
    //

    /** The receivers of data from this instance. */
    private List<DataReceiver> dataReceivers = new LinkedList<DataReceiver>();
    
    //
    // Constructors
    //

    /**
     * Creates a new DataTransmitter instance.
     */
    public DataTransmitter() {}
    
    //
    // Methods
    //

    /**
     * Adds a data receiver.
     * 
     * @param dataReceiver The data receiver to add.
     */
    public void addDataReceiver(DataReceiver dataReceiver) {
        this.dataReceivers.add(dataReceiver);
    }

    /**
     * Removes a data receiver.
     * 
     * @param dataReceiver The dataReceiver to remove.
     */
    public void removeDataReceiver(DataReceiver dataReceiver) {
        this.dataReceivers.remove(dataReceiver);
    }

    /**
     * Sends data to receivers.
     *
     * @param dataDescription A description of the data.
     * @param data The data to send.
     */
    public void sendData(DataDescription dataDescription, Data data) {
        for (DataReceiver dataReceiver : this.dataReceivers) {
            dataReceiver.dataReceived(dataDescription, data);
        }
    }
}
