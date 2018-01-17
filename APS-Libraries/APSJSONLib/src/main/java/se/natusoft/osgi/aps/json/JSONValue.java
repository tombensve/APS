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

import java.io.*;

/**
 * This class is based on the structure defined on http://www.json.org/.
 * <p>
 * This is a base class for all other JSON* classes. It represents the "value" diagram on the above mentioned web page:
 * <p>
 * Subclasses
 * ----------
 * |________________ (STRING) ________________|      JSONString
 * |  |_____________ (NUMBER) _____________|  |      JSONNumber
 * |_____________ (OBJECT) _____________|         JSONObject
 * |_____________ (ARRAY)  _____________|         JSONArray
 * |_____________ (true)   _____________|     \__ JSONBoolean
 * |_____________ (false)  _____________|     /
 * \_____________ (null)   _____________/         JSONNull
 *
 * @author Tommy Svensson
 * @see JSONObject
 */
@SuppressWarnings("WeakerAccess")
public abstract class JSONValue {
    //
    // Private Members
    //

    /**
     * The error handler.
     */
    private JSONErrorHandler errorHandler = null;

    /**
     * The indent level for writing.
     */
    private String indent = "";

    //
    // Constructors
    //

    /**
     * Creates a new JSONValue.
     */
    protected JSONValue() {
    }

    /**
     * Creates a new JSONValue
     */
    protected JSONValue(JSONErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    //
    // Abstract Methods
    //

    /**
     * This will read the vale from an input stream.
     *
     * @param c      The first character already read from the input stream.
     * @param reader The reader to read from.
     * @throws APSIOException on IO failure.
     */
    protected abstract void readJSON(char c, JSONReader reader) throws APSIOException;

    /**
     * This will write the data held by this JSON value in JSON format on the specified stream.
     *
     * @param writer  A JSONWriter instance to write with.
     * @param compact If true write the JSON as compact as possible. false means readable, indented.
     * @throws APSIOException On IO failure.
     */
    protected abstract void writeJSON(JSONWriter writer, boolean compact) throws APSIOException;

    //
    // Support Methods
    //

    /**
     * @return The user supplied error handler.
     */
    protected JSONErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * Reads and resolves what JSON type is the next in the input and returns it.
     *
     * @param c            The first already read character.
     * @param reader       The reader to read from.
     * @param errorHandler The user supplied error handler.
     * @return The read JSONValue.
     * @throws APSIOException on IOFailure.
     */
    /*package*/
    static JSONValue resolveAndParseJSONValue(char c, JSONReader reader, JSONErrorHandler errorHandler) throws APSIOException {
        c = reader.skipWhitespace(c);

        JSONValue value = null;
        if (JSONObject.isObjectStart(c)) {
            value = createObject(errorHandler);
            value.readJSON(c, reader);
        } else if (JSONString.isStringStart(c)) {
            value = createString(errorHandler);
            value.readJSON(c, reader);
        } else if (JSONNumber.isNumberStart(c)) {
            value = createNumber(errorHandler);
            value.readJSON(c, reader);
        } else if (JSONBoolean.isBooleanStart(c)) {
            value = createBoolean(errorHandler);
            value.readJSON(c, reader);
        } else if (JSONNull.isNullStart(c)) {
            value = createNull(errorHandler);
            value.readJSON(c, reader);
        } else if (JSONArray.isArrayStart(c)) {
            value = createArray(errorHandler);
            value.readJSON(c, reader);
        } else {
            errorHandler.fail("Non expected character '" + c + "' found in input stream. This does not match the start of any acceptable value!", null);
        }

        return value;
    }

    /**
     * Sets the indentation level for output.
     *
     * @param indent The indent to set.
     */
    /*package*/ void setIndent(String indent) {
        this.indent = indent;
    }

    /**
     * Returns the indent for output.
     */
    /*package*/ String getIndent() {
        return this.indent;
    }

    /**
     * Provide a warning.
     *
     * @param message The warning message.
     */
    @SuppressWarnings("unused")
    protected void warn(String message) {
        if (this.errorHandler == null) {
            throw new IllegalStateException("Bad state! You have probably tried to call readJSON() on an instance created for writing (== no error support!)");
        }
        this.errorHandler.warning(message);
    }

    /**
     * Fails the job.
     *
     * @param message The failure message.
     * @param cause   An eventual cause of the failure. Can be null.
     */
    protected void fail(String message, Throwable cause) {
        if (this.errorHandler == null) {
            throw new IllegalStateException("Bad state! You have probably tried to call readJSON() on an instance created for writing (== no error support!)");
        }
        this.errorHandler.fail(message, cause);
    }

    /**
     * Fails the job.
     *
     * @param message The failure message.
     */
    protected void fail(String message) {
        fail(message, null);
        throw new RuntimeException("Bad developer! The implementation of JSONErrorHandler should always throw an exception on fail(...)!");
    }

    /**
     * This will read the value from an input stream.
     *
     * @param is The input stream to read from.
     * @throws APSIOException on IO failure.
     */
    public void readJSON(InputStream is) throws APSIOException {
        try {
            JSONReader reader = new JSONReader(new PushbackReader(new InputStreamReader(is, "UTF-8")), this.errorHandler);
            readJSON(reader.getChar(), reader);
        }
        catch (IOException ioe) {
            throw new APSIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * This writes JSON to the specified OutputStream.
     *
     * @param os The outoutStream to write to.
     * @throws APSIOException on IO failure.
     */
    public void writeJSON(OutputStream os) throws APSIOException {
        writeJSON(os, true);
    }

    /**
     * This writes JSON to the specified OutputStream.
     *
     * @param os      The outoutStream to write to.
     * @param compact If true write JSON as compact as possible. If false write it readable with indents.
     * @throws APSIOException on IO failure.
     */
    public void writeJSON(OutputStream os, boolean compact) throws APSIOException {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            try {
                writeJSON(new JSONWriter(osw), compact);
            } finally {
                // Any but the flush on the first instance of OutputStreamWriter when this method is called multiple times
                // in a row will fail if run from a JUnit test withing IntelliJ IDEA 11.0, by selecting the test and doing "Run" on it.
                // If JUnit test is run through maven it will output every time.
                osw.flush();
                osw.close();
            }
        }
        catch (IOException ioe) {
            throw new APSIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Configures the created instance.
     *
     * @param jsonObj The created JSON object.
     * @param <T>     The JSONValue subclass type created.
     * @return The passed object.
     */
    private static <T> T createFilter(T jsonObj) {
        // Currently nothing to do here.
        return jsonObj;
    }

    /**
     * Method for creating a JSONString instance.
     *
     * @param errorHandler The user error handler.
     */
    /*package*/
    static JSONString createString(JSONErrorHandler errorHandler) {
        return createFilter(new JSONString(errorHandler));
    }

    /**
     * Method for creating a JSONNumber instance.
     *
     * @param errorHandler The user error handler.
     */
    /*package*/
    @SuppressWarnings("WeakerAccess")
    static JSONNumber createNumber(JSONErrorHandler errorHandler) {
        return createFilter(new JSONNumber(errorHandler));
    }

    /**
     * Method for creating a JSONNull instance.
     *
     * @param errorHandler The user error handler.
     */
    /*package*/
    @SuppressWarnings("WeakerAccess")
    static JSONNull createNull(JSONErrorHandler errorHandler) {
        return createFilter(new JSONNull(errorHandler));
    }

    /**
     * Method for creating a JSONBoolean instance.
     *
     * @param errorHandler The user error handler.
     */
    /*package*/
    @SuppressWarnings("WeakerAccess")
    static JSONBoolean createBoolean(JSONErrorHandler errorHandler) {
        return createFilter(new JSONBoolean(errorHandler));
    }

    /**
     * Method for creating a JSONArray instance.
     *
     * @param errorHandler The user error handler.
     */
    /*package*/
    @SuppressWarnings("WeakerAccess")
    static JSONArray createArray(JSONErrorHandler errorHandler) {
        return createFilter(new JSONArray(errorHandler));
    }

    /**
     * Method for creating a JSONObject instance.
     *
     * @param errorHandler The user error handler.
     */
    /*package*/
    @SuppressWarnings("WeakerAccess")
    static JSONObject createObject(JSONErrorHandler errorHandler) {
        return createFilter(new JSONObject(errorHandler));
    }

    //
    // Inner Classes
    //

    /**
     * For subclasses to use in readJSON(JSONReader reader).
     * <p>
     * __Please note__ Since this reads chars and chars cannot contain -1 or any value that can be used to indicate EOF it
     * throws a JSONEOFException on EOF! JSONEOFException extends IOException.
     */
    /*package*/ static class JSONReader {
        //
        // Private Members
        //

        /**
         * The reader to read from.
         */
        private PushbackReader reader;

        /**
         * The error handles for the JSON parsing
         */
        private JSONErrorHandler errorHandler;

        //
        // Constructors
        //

        /**
         * Creates a new JSONReader instance.
         *
         * @param reader       The PushbackReader to read from.
         * @param errorHandler The handler for errors.
         */
        protected JSONReader(PushbackReader reader, JSONErrorHandler errorHandler) {
            this.reader = reader;
            this.errorHandler = errorHandler;
        }

        //
        // Methods
        //

        /**
         * Returns the next character on the specified input stream, setting EOF state checkable with isEOF().
         *
         * @throws APSIOException on IO problems.
         */
        protected char getChar() throws APSIOException {
            return getChar(false);
        }

        /**
         * Returns the next character on the specified input stream, setting EOF state checkable with isEOF().
         *
         * @param handleEscapes If true then \* escape character are handled.
         * @throws APSIOException on IO problems.
         */
        @SuppressWarnings("WeakerAccess")
        protected char getChar(boolean handleEscapes) throws APSIOException {
            try {
                char[] cbuff = new char[1];
                int noRead = this.reader.read(cbuff, 0, 1);
                if (noRead <= 0) {
                    this.errorHandler.fail("Unexpected end-of-file!", null);
                    throw new JSONEOFException();
                }

                char c = cbuff[0];
                if (handleEscapes) {
                    c = handleEscape(c);
                }

                return c;
            }
            catch (IOException ioe) {
                throw new APSIOException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Unreads the specified character so that the next call to getNextChar() will return it again.
         *
         * @param c The character to unget.
         * @throws APSIOException on IO problems.
         */
        @SuppressWarnings("WeakerAccess")
        protected void ungetChar(char c) throws APSIOException {
            try {
                char[] cbuff = new char[1];
                cbuff[0] = c;
                this.reader.unread(cbuff);
            }
            catch (IOException ioe) {
                throw new APSIOException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Skips whitespace returning the first non whitespace character. This also sets the EOF flag.
         *
         * @param c The first char already read from the input stream.
         * @throws APSIOException on IO problems.
         */
        @SuppressWarnings("WeakerAccess")
        protected char skipWhitespace(char c) throws APSIOException {
            while (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                c = getChar();
            }

            return c;
        }

        /**
         * Skips whitespace returning the first non whitespace character. This also sets the EOF flag.
         *
         * @throws APSIOException on IO problems.
         */
        @SuppressWarnings("unused")
        protected char skipWhitespace() throws APSIOException {
            return skipWhitespace(getChar());
        }

        /**
         * This checks for and handles escape sequences.
         *
         * @param c The char to check for escape char.
         * @return A possible converted char or the passed char.
         * @throws APSIOException On IO failure.
         */
        private char handleEscape(char c) throws APSIOException {
            if (c == '\\') {
                c = getChar();

                if (c == 'b') {
                    c = '\b';
                } else if (c == 'f') {
                    c = '\f';
                } else if (c == 'n') {
                    c = '\n';
                } else if (c == 'r') {
                    c = '\r';
                } else if (c == 't') {
                    c = '\t';
                } else if (c == 'u') {
                    byte[] hexDigits = new byte[4];
                    char[] hexChars = new char[4];
                    hexChars[0] = getChar();
                    hexChars[1] = getChar();
                    hexChars[2] = getChar();
                    hexChars[3] = getChar();
                    for (int i = 0; i < 4; i++) {
                        char h = hexChars[i];
                        if (h == '0') {
                            hexDigits[i] = 0;
                        } else if (h == '1') {
                            hexDigits[i] = 1;
                        } else if (h == '2') {
                            hexDigits[i] = 2;
                        } else if (h == '3') {
                            hexDigits[i] = 3;
                        } else if (h == '4') {
                            hexDigits[i] = 4;
                        } else if (h == '5') {
                            hexDigits[i] = 5;
                        } else if (h == '6') {
                            hexDigits[i] = 6;
                        } else if (h == '7') {
                            hexDigits[i] = 7;
                        } else if (h == '8') {
                            hexDigits[i] = 8;
                        } else if (h == '9') {
                            hexDigits[i] = 9;
                        } else if (h == 'a' || h == 'A') {
                            hexDigits[i] = 10;
                        } else if (h == 'b' || h == 'B') {
                            hexDigits[i] = 11;
                        } else if (h == 'c' || h == 'C') {
                            hexDigits[i] = 12;
                        } else if (h == 'd' || h == 'D') {
                            hexDigits[i] = 13;
                        } else if (h == 'e' || h == 'E') {
                            hexDigits[i] = 14;
                        } else if (h == 'f' || h == 'F') {
                            hexDigits[i] = 15;
                        }
                    }
                    String sb = String.valueOf(hexDigits[0]) +
                            hexDigits[1] +
                            hexDigits[2] +
                            hexDigits[3];
                    int ucode = Integer.parseInt(sb.toUpperCase(), 16);
                    c = (char) ucode; // Not entirely sure this will yield the correct result!
                }
            }

            return c;
        }

        /**
         * Reads until any of a specified set of characters occur.
         *
         * @param until         The characters to stop reading at. The stopping character will be returned unless EOF.
         * @param c             The first preread character.
         * @param sb            If not null read characters are added to this. The stopping character will not be included.
         * @param handleEscapes True if we are reading a string that should handle escape characters.
         * @return The next character to read.
         * @throws APSIOException on IO problems.
         */
        @SuppressWarnings("WeakerAccess")
        protected char readUntil(String until, char c, StringBuilder sb, boolean handleEscapes) throws APSIOException {
            while (until.indexOf(c) < 0) {
                sb.append(c);
                c = getChar(handleEscapes);
            }

            return c;
        }

        /**
         * Reads until any of a specified set of characters occur.
         *
         * @param until        The characters to stop reading at. The stopping character will be returned unless EOF.
         * @param sb           If not null read characters are added to this. The stopping character will not be included.
         * @param stringEscape True if we are rading a string that should be escaped.
         * @throws APSIOException on IO problems.
         */
        @SuppressWarnings("WeakerAccess")
        protected char readUntil(String until, StringBuilder sb, boolean stringEscape) throws APSIOException {
            return readUntil(until, getChar(), sb, stringEscape);
        }

        /**
         * Reads until any of a specified set of characters occur.
         *
         * @param until The characters to stop reading at. The stopping character will be returned unless EOF.
         * @param sb    If not null read characters are added to this. The stopping character will not be included.
         * @throws APSIOException on IO problems.
         */
        @SuppressWarnings("unused")
        protected char readUntil(String until, StringBuilder sb) throws APSIOException {
            return readUntil(until, sb, false);
        }

        /**
         * Returns true if c is one of the characters in validChars.
         *
         * @param c          The character to check.
         * @param validChars The valid characters.
         */
        @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
        protected boolean checkValidChar(char c, String validChars) {
            return validChars.indexOf(c) >= 0;
        }

        /**
         * Asserts that char a equals expected char c.
         *
         * @param a       The char to assert.
         * @param e       The expected value.
         * @param message Failure message.
         */
        @SuppressWarnings("WeakerAccess")
        protected void assertChar(char a, char e, String message) {
            if (a != e) {
                this.errorHandler.fail("Assert: " + message, null);
            }
        }

        /**
         * Asserts that char a equals expected char c.
         *
         * @param a        The char to assert.
         * @param expected String of valid characters.
         * @param message  Failure message.
         */
        @SuppressWarnings("WeakerAccess")
        protected void assertChar(char a, String expected, String message) {
            if (expected.indexOf(a) < 0) {
                this.errorHandler.fail("Assert: " + message, null);
            }
        }
    }

    /**
     * For subclasses to use in writeJSON(JSONWriter writer).
     */
    protected static class JSONWriter {
        //
        // Private Members
        //

        /**
         * The writer to write to.
         */
        private Writer writer;

        //
        // Constructors
        //

        /**
         * Creates a new JSONWriter instance.
         *
         * @param writer The writer to write to.
         */
        protected JSONWriter(Writer writer) {
            this.writer = writer;
        }

        //
        // Methods
        //

        /**
         * Writes JSON output.
         *
         * @param json The JSON output to write.
         * @throws APSIOException on IO failure.
         */
        protected void write(String json) throws APSIOException {
            try {
                this.writer.write(json);
            }
            catch (IOException ioe) {
                throw new APSIOException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Writes JSON output plus a newline.
         *
         * @param json The JSON output to write.
         * @throws APSIOException on IO problems.
         */
        @SuppressWarnings("WeakerAccess")
        protected void writeln(@SuppressWarnings("SameParameterValue") String json) throws APSIOException {
            try {
                this.writer.write(json);
                this.writer.write("\n");
            }
            catch (IOException ioe) {
                throw new APSIOException(ioe.getMessage(), ioe);
            }
        }
    }
}
