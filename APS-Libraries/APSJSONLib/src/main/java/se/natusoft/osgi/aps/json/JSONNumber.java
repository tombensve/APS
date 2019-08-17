/*
 *
 * PROJECT
 *     Name
 *         APS JSON Library
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a JSON parser and creator. Please note that this bundle has no dependencies to any
 *         other APS bundle! It can be used as is without APS in any Java application and OSGi container.
 *         The reason for this is that I do use it elsewhere and don't want to keep 2 different copies of
 *         the code. OSGi wise this is a library. All packages are exported and no activator nor services
 *         are provided.
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
 *         2011-01-30: Created!
 *
 */
package se.natusoft.osgi.aps.json;

import se.natusoft.osgi.aps.exceptions.APSIOException;

/**
 * This class is based on the structure defined on http://www.json.org/.
 *
 * This represents the "number" diagram on the above mentioned web page:
 *
 *                                           ______________________
 *                                          /                      \
 *                                          |                      |
 *     |_|______________ (0) _______________/__ (.) ___ (digit) ___\_________________________|_|
 *     | | \       /  \                    /         /           \  \                      / | |
 *         |       |  |                   /          \___________/  |                      |
 *         \_ (-) _/  \_ (digit 1-9) ____/_______                   |                      |
 *                                    /          \                  |                      |
 *                                    \_ (digit) /           _ (e) _|                      |
 *                                                          |_ (E) _|           ___________|
 *                                                          |        _ (+) _   /           |
 *                                                          \_______/_______\__\_ (digit) _/
 *                                                                  \_ (-) _/
 *
 * @author Tommy Svesson
 */
@SuppressWarnings("WeakerAccess")
public class JSONNumber extends JSONValue {
    //
    // Private Members
    //

    /** The number value. */
    private Number value;

    //
    // Constructors
    //

    /**
     * Creates a new JSONNumber instance for writing JSON output.
     *
     * @param value The numeric value.
     */
    public JSONNumber(Number value) {
        super();
        this.value = value;
    }

    /**
     * Creates a new JSONNumber instance for reading JSON input or writing JSON output.
     *
     * @param errorHandler The error handle to use.
     */
    public JSONNumber(JSONErrorHandler errorHandler) {
        super(errorHandler);
    }

    //
    // Methods
    //

    /**
     * Determines if the specified character denotes the start of a numeric value.
     *
     * @param c The character to test.
     */
    /*package*/ static boolean isNumberStart(char c) {
        return "-0123456789".indexOf(c) >= 0;
    }

    /**
     * Returns the number as a Number.
     */
    public Number toNumber() {
        return this.value;
    }

    /**
     * Returns the number as a double value.
     */
    @SuppressWarnings("unused")
    public double toDouble() {
        return this.value.doubleValue();
    }

    /**
     * Returns the number as a float value.
     */
    public float toFloat() {
        return this.value.floatValue();
    }

    /**
     * Returns the number as an int value.
     */
    public int toInt() {
        return this.value.intValue();
    }

    /**
     * Returns the number as a long value.
     */
    public long toLong() {
        return this.value.longValue();
    }

    /**
     * Returns the number as a short value.
     */
    public short toShort() {
        return this.value.shortValue();
    }

    /**
     * Returns the number as a byte value.
     */
    public byte toByte() {
        return this.value.byteValue();
    }

    /**
     * @return number as String.
     */
    public String toString() {
        return "" + this.value;
    }

    /**
     * Returns the number as a value of the type specified by the type parameter.
     *
     * @param type The type of the returned number.
     */
    public Object to(Class type) {
        Object result = null;

        if (type.isArray()) {
            type = type.getComponentType();
        }

        if (type == double.class || type == Double.class) {
            result = this.value;
        }
        else if (type == float.class || type == Float.class) {
            result = toFloat();
        }
        else if (type == int.class || type == Integer.class) {
            result = toInt();
        }
        else if (type == long.class || type == Long.class) {
            result = toLong();
        }
        else if (type == short.class || type == Short.class) {
            result = toShort();
        }
        else if (type == byte.class || type == Byte.class) {
            result = toByte();
        }
        else {
            fail("Type '" + type.getName() + "' is not an accepted number type! Only double,float, int, long and short is suppprted!");
        }

        return result;
    }

    /**
     * Loads the content of this JSONNumber model from the specified input stream.
     *
     * @param c A preread character from the input stream.
     * @param reader The JSONReader to read from.
     *
     * @throws APSIOException on IO problems.
     */
    @Override
    protected void readJSON(char c, JSONReader reader) throws APSIOException {
        c = reader.skipWhitespace(c);
        reader.assertChar(c, "-0123456789", "Character '" + c + "' is an invalid start of a number! Valid start characters are: -0123456789");
        StringBuilder sb = new StringBuilder();
        sb.append(c);

        boolean read = false;
        while (!read) {
            c = reader.getChar();
            if (reader.checkValidChar(c, "0123456789.eE+-")) {
                sb.append(c);
            }
            else {
                read = true;
                // We have just read a character that does not belong to us! So we return it again.
                reader.ungetChar(c);
            }
        }

        // Find best fit for numeric value.
        String sval = sb.toString();
        try {
            this.value = Byte.valueOf(sval);
        }
        catch (NumberFormatException nfe1) {
            try {
                this.value = Short.valueOf(sval);
            }
            catch (NumberFormatException nfe2) {
                try {
                    this.value = Integer.valueOf(sval);
                }
                catch (NumberFormatException nfe3) {
                    try {
                        this.value = Long.valueOf(sval);
                    }
                    catch (NumberFormatException nfe4) {
                        try {
                            this.value = Float.valueOf(sval);
                        }
                        catch (NumberFormatException nfe5) {
                            try {
                                this.value = Double.valueOf(sval);
                            }
                            catch (NumberFormatException nfe6) {
                                fail("Bad numeric value '" + sb.toString() + "'!", nfe6);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes the contents of this JSONNumber to the specified output stream in JSON format.
     *
     * @param writer The JSONWriter to write to.
     * @param compact Write json in compact format.
     *
     * @throws APSIOException on IO problems.
     */
    @Override
    protected void writeJSON(JSONWriter writer, boolean compact) throws APSIOException {
        writer.write(this.value.toString());
    }
}
