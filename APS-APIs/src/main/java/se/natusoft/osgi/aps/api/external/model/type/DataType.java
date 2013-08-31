/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.3
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
 * This is an enum defining "basic" data types for service return types and parameters.
 */
public enum DataType {

    /** A void type. */
    VOID(true, "void"),

    /** A boolean type. */
    BOOLEAN(true, "Boolean"),

    /** a byte type. */
    BYTE(true, "Byte"),

    /** A unicode character. */
    CHAR(true, "Char"),

    /** A short integer (16 bits) */
    SHORT(true, "Short"),

    /** A standard integer (32 bits) */
    INT(true, "Integer"),

    /** A long integer (64 bitrs) */
    LONG(true, "Long"),

    /** A floating point number. */
    FLOAT(true, "Float"),

    /** A large floating point number. */
    DOUBLE(true, "Double"),

    /** A String. */
    STRING(true, "String"), // We pretend this is a primitive type!

    /** A list of child types. */
    LIST(false, "List"),

    /** A map of child types. */
    MAP(false, "Map"),

    /** An object containing child types. */
    OBJECT(false, "Object");

    /** True if primitive type or String. */
    private boolean primitive;
    
    /** The displayable type name. */
    private String typeName;

    /**
     * Creates a new DataType.
     *
     * @param primitive true if primitive type or String.
     * @param typeName The displayable name of the type.                  
     */
    DataType(boolean primitive, String typeName) {
        this.primitive = primitive;
        this.typeName = typeName;
    }

    /**
     * @return true if primitive type or String.
     */
    public boolean isPrimitive() {
        return this.primitive;
    }

    /**
     * @return true if this is a structured type having child types.
     */
    public boolean isStructured() {
        return !this.primitive;
    }

    /**
     * @return Returns a displayable name of the type.
     */
    public String getTypeName() {
        return this.typeName;
    }

}
