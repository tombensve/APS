/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
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
 *         2011-12-31: Created!
 *         
 */
package se.natusoft.osgi.aps.api.external.model.type;

/**
 * This represents a parameter to a service method call. It adds a position value to the DataTypeDescription.
 */
public class ParameterDataTypeDescription extends DataTypeDescription {
    //
    // Private Members
    //

    /** The position of the parameter. */
    private int position = 0;

    //
    // Constructors
    //

    /**
     * Creates a new ParameterDataDescription.
     */
    public ParameterDataTypeDescription() {}

    /**
     * Creates a new DataDescription.
     *
     * @param position The position of the parameter in the parameter list.
     * @param owner The description of the owner of this description.
     * @param dataType The type of the data of this description.
     * @param objectQName The fully qualified name of the object when this represents an object, null otherwise.
     */
    public ParameterDataTypeDescription(int position, DataTypeDescription owner, String name, DataType dataType, String objectQName) {
        super(owner, dataType, objectQName);
        this.position = position;
    }

    //
    // Methods
    //

    /**
     * Sets the position of the parameter in the parameter list.
     *
     * @param position The position to set.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the position of the parameter in the parameter list.
     */
    public int getPosition() {
        return this.position;
    }

}
