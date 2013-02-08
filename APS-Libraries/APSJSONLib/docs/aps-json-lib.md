# APSJSONLib

This is a library (exports all its packages and provides no service) for reading and writing JSON. It can also write a JavaBean object as JSON and take a JSON value or inputstream containing JSON and produce a JavaBean.

This basically provides a class representing each JSON type: JSONObject, JSONString, JSONNumber, JSONBoolean, JSONArray, JSONNull, and a JSONValue class that is the common base class for all the other. Each class knows how to read and write the JSON type it represents. Then there is a JavaToJSON and a JSONToJava class with static methods for converting back and forth. This mapping is very primitive. There has to be one to one between the JSON and the Java objects.

This does not try to be an alternative to Jackson! This is used internally by other services.

# APIs

public _class_ __JSON__   [se.natusoft.osgi.aps.json] {

>  This is the official API for reading and writing JSON values. 

__public static JSONValue read(InputStream jsonIn, JSONErrorHandler errorHandler) throws IOException__

>  Reads any JSON object from the specified InputStream.  

_Returns_

> A JSONValue subclass. Which depends on what was found on the stream.

_Parameters_

> _jsonIn_ - The InputStream to read from. 

> _errorHandler_ - An implementation of this interface should be supplied by the user to handle any errors during JSON parsing. 

_Throws_

> _IOException_ - on any IO failures. 

__public static void write(OutputStream jsonOut, JSONValue value) throws IOException__

>  Writes a JSONValue to an OutputStream. This will write compact output by default.  

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

_Throws_

> _IOException_ - on failure. 

__public static void write(OutputStream jsonOut, JSONValue value, boolean compact) throws IOException__

>  Writes a JSONValue to an OutputStream.  

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

> _compact_ - If true the written JSON is made very compact and hard to read but produce less data. 

_Throws_

> _IOException_

}

----

    

public _class_ __JSONArray__ extends  JSONValue  [se.natusoft.osgi.aps.json] {

>  This class is based on the structure defined on http://www.json.org/. 

> This represents the "array" diagram on the above mentioned web page: 

                  _______________________
                 /                       \
                 |                       |
    |_____ ([) __/_______ (value) _______\__ (]) _____|
    |              /                   \              |
                   |                   |
                   \_______ (,) _______/

>  @author Tommy Svensson 



__public JSONArray()__

>  Creates a new JSONArray for wrinting JSON output. 

__public JSONArray(JSONErrorHandler errorHandler)__

>  Creates a new JSONArray for reading JSON input and writing JSON output.  

_Parameters_

> _errorHandler_





__public void addValue(JSONValue value)__

>  Adds a value to the array.  

_Parameters_

> _value_ - The value to add. 

__public List<JSONValue> getAsList()__

>  Returns the array values as a List. 

__public <T extends JSONValue> List<T> getAsList(Class<T> type)__

>  Returns the array values as a list of a specific type.  

_Returns_

> A list of specified type if type is the same as in the list.

_Parameters_

> _type_ - The class of the type to return values as a list of. 

> _<T>_ - One of the JSONValue subclasses. 

__protected void readJSON(char c, JSONReader reader) throws IOException__

>  Loads this JSONArray model with data from the specified input stream.  

_Parameters_

> _c_ - A preread character from the input stream. 

> _reader_ - The JSONReader to read from. 

_Throws_

> _IOException_

__protected void writeJSON(JSONWriter writer, boolean compact) throws IOException__

>  Writes the JSONArray content in JSON format on the specified output stream.  

_Parameters_

> _writer_ - The writer to write to. 

> _compact_ - Write json in compact format. 

_Throws_

> _IOException_

}

----

    

public _class_ __JSONBoolean__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

>  This class is based on the structure defined on http://www.json.org/.   @author Tommy Svensson 



__public JSONBoolean(boolean value)__

>  Creates a new JSONBoolean instance for writing JSON output.  

_Parameters_

> _value_ - The value for this boolean. 

__public JSONBoolean(JSONErrorHandler errorHandler)__

>  Creates a new JSONBoolean instance for reading JSON input or writing JSON output.  

_Parameters_

> _errorHandler_



__public void setBooleanValue(boolean value)__

>  Sets the value of this boolean.  

_Parameters_

> _value_ - The value to set. 

__public boolean getAsBoolean()__

>  Returns the value of this boolean. 

__public String toString()__

>  Returns the value of this boolean as a String. 

__protected void readJSON(char c, JSONReader reader) throws IOException__

>  Loads the contents of this JSONBoolean model from the specified input stream.  

_Parameters_

> _c_ - A preread character from the input stream. 

> _reader_ - The JSONReader to read from. 

_Throws_

> _IOException_

__protected void writeJSON(JSONWriter writer, boolean compact) throws IOException__

>  Writes the contents of this JSONBoolean to the specified output stream in JSON format.  

_Parameters_

> _writer_ - The JSONWriter to write to. 

> _compact_ - Write json in compact format. 

_Throws_

> _IOException_

}

----

    

public _interface_ __JSONErrorHandler__   [se.natusoft.osgi.aps.json] {

>  This is called on warnings or failures.   @author Tommy Svensson 

__void warning(String message)__

>  Warns about something.  

_Parameters_

> _message_ - The warning message. 

__void fail(String message, Throwable cause) throws RuntimeException__

>  Indicate failure.  

_Parameters_

> _message_ - The failure message. 

> _cause_ - The cause of the failure. Can be null! 

_Throws_

> _RuntimeException_ - This method must throw a RuntimeException. 

}

----

    

public _class_ __JSONNull__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

>  This class is based on the structure defined on http://www.json.org/.   @author Tommy Svensson 

__public JSONNull()__

>  Creates a new JSONNull instance for writing JSON output. 

__public JSONNull(JSONErrorHandler errorHandler)__

>  Creates a new JSONNull instance for reading JSON input or writing JSON output.  

_Parameters_

> _errorHandler_



__public String toString()__

>  

_Returns_

> as String.

__protected void readJSON(char c, JSONReader reader) throws IOException__

>  Reads the content of the JSONNull model from the specified input stream.  

_Parameters_

> _c_ - A preread character from the input stream. 

> _reader_ - The JSONReader to read from. 

_Throws_

> _IOException_

__protected void writeJSON(JSONWriter writer, boolean compact) throws IOException__

>  Writes the content of this JSONNull model to the specified output stream in JSON format.  

_Parameters_

> _writer_ - The JSONWriter to write to. 

> _compact_ - Write json in compact format. 

_Throws_

> _IOException_

}

----

    

public _class_ __JSONNumber__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

>  This class is based on the structure defined on http://www.json.org/. 

> This represents the "number" diagram on the above mentioned web page: 

                                          ______________________
                                         /                      \
                                         |                      |
    |_|______________ (0) _______________/__ (.) ___ (digit) ___\_________________________|_|
    | | \       /  \                    /         /           \  \                      / | |
        |       |  |                   /          \___________/  |                      |
        \_ (-) _/  \_ (digit 1-9) ____/_______                   |                      |
                                   /          \                  |                      |
                                   \_ (digit) /           _ (e) _|                      |
                                                         |_ (E) _|           ___________|
                                                         |        _ (+) _   /           |
                                                         \_______/_______\__\_ (digit) _/
                                                                 \_ (-) _/

>  @author Tommy Svesson 



__public JSONNumber(Number value)__

>  Creates a new JSONNumber instance for writing JSON output.  

_Parameters_

> _value_ - The numeric value. 

__public JSONNumber(JSONErrorHandler errorHandler)__

>  Creates a new JSONNumber instance for reading JSON input or writing JSON output.  

_Parameters_

> _errorHandler_ - The error handle to use. 



__public Number toNumber()__

>  Returns the number as a Number. 

__public double toDouble()__

>  Returns the number as a double value. 

__public float toFloat()__

>  Returns the number as a float value. 

__public int toInt()__

>  Returns the number as an int value. 

__public long toLong()__

>  Returns the number as a long value. 

__public short toShort()__

>  Returns the number as a short value. 

__public byte toByte()__

>  Returns the number as a byte value. 

__public String toString()__

>  

_Returns_

> number as String.

__public Object to(Class type)__

>  Returns the number as a value of the type specified by the type parameter.  

_Parameters_

> _type_ - The type of the returned number. 

__protected void readJSON(char c, JSONReader reader) throws IOException__

>  Loads the content of this JSONNumber model from the specified input stream.  

_Parameters_

> _c_ - A preread character from the input stream. 

> _reader_ - The JSONReader to read from. 

_Throws_

> _IOException_

__protected void writeJSON(JSONWriter writer, boolean compact) throws IOException__

>  Writes the contents of this JSONNumber to the specified output stream in JSON format.  

_Parameters_

> _writer_ - The JSONWriter to write to. 

> _compact_ - Write json in compact format. 

_Throws_

> _IOException_

}

----

    

public _class_ __JSONObject__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

>  This class is based on the structure defined on http://www.json.org/. 

> It represents the "object" diagram on the above mentioned web page: 

                 ________________________________________
                /                                        \
    |___ ({) __/_____ (string) ____ (:) ____ (value) _____\___ (}) ____|
    |           /                                        \             |
                \__________________ (,) _________________/
    

> This is also the starting point. 

> To write JSON, create a new JSONObject (new JSONObject()) and call addProperty(name, value) for children. Then do jsonObj.writeJSON(outputStream). 

> To read JSON, create a new JSONObject (new JSONObject(jsonErrorHandler)) and then do jsonObj.readJSON(inputStream). Then use getProperty(name) to extract children.    @author Tommy Svensson 



__public JSONObject(JSONErrorHandler errorHandler)__

>  Creates a JSONObject instance for writing JSON output.  Creates a new JSONObject instance for reading JSON input or writing JSON output.  

_Parameters_

> _errorHandler_





__public Set<JSONString> getPropertyNames()__

>  Returns the names of the available properties. 

__public JSONValue getProperty(JSONString name)__

>  Returns the named property.  

_Parameters_

> _name_ - The name of the property to get. 

__public JSONValue getProperty(String name)__

>  Returns the named property.  

_Parameters_

> _name_ - The name of the property to get. 

__public void addProperty(JSONString name, JSONValue value)__

>  Adds a property to this JSONObject instance.  

_Parameters_

> _name_ - The name of the property. 

> _value_ - The property value. 

__public void addProperty(String name, JSONValue value)__

>  Adds a property to this JSONObject instance.  

_Parameters_

> _name_ - The name of the property. 

> _value_ - The property value. 

__protected void readJSON(char c, JSONReader reader) throws IOException__

>  Loads this JSONObject model from the specified input stream.  

_Parameters_

> _c_ - A pre-read character from the input stream. 

> _reader_ - The JSONReader to read from. 

_Throws_

> _IOException_

__protected void writeJSON(JSONWriter writer, boolean compact) throws IOException__

>  Writes the contents of this JSONObject model to the specified output stream in JSON format.  

_Parameters_

> _writer_ - The JSONWriter to write to. 

> _compact_ - Write json in compact format. 

_Throws_

> _IOException_

}

----

    

public _class_ __JSONString__ extends  JSONValue    [se.natusoft.osgi.aps.json] {

>  This class is based on the structure defined on http://www.json.org/. 

> This represents the "string" diagram on the above mentioned web page: 

               __________________________________________________________________________
              /    ___________________________________________________________________   \
              |   /                                                                   \  |
    |___ (") _|___|___ (Any UNICODE character except " or \ or control character) ____|__|_ (") ___|
    |           \                                                                  /               |
                 |                                                                 |
                 \__ (\) ___ (") (quotation mark) _________________________________|
                         |__ (\) (reverse solidus) ________________________________|
                         |__ (/) (solidus) ________________________________________|
                         |__ (b) (backspace) ______________________________________|
                         |__ (f) (formfeed) _______________________________________|
                         |__ (n) (newline) ________________________________________|
                         |__ (r) (carriage return) ________________________________|
                         |__ (t) (orizontal tab) __________________________________|
                         \__ (u) (4 hexadecimal digits) ___________________________/

>   @author Tommy Svensson 



__public JSONString(String value)__

>  Creates a new JSONString for writing JSON output.  

_Parameters_

> _value_ - The value of this JSONString. 

__public JSONString(JSONErrorHandler errorHandler)__

>  Creates a new JSONString for reading JSON input and writing JSON output.  

_Parameters_

> _errorHandler_





__protected void readJSON(char c, JSONReader reader) throws IOException__

>  Loads this JSONString model from the specified input stream.  

_Parameters_

> _c_ - A preread character from the input stream. 

> _reader_ - The JSONReader to read from. 

_Throws_

> _IOException_

__protected void writeJSON(JSONWriter writer, boolean compact) throws IOException__

>  Writes the contents of this JSONString to the specified output stream in JSON format.  

_Parameters_

> _writer_ - The JSONWriter to write to. 

> _compact_ - If true write compact. 

_Throws_

> _IOException_

__public String toString()__

>  Converts this to a String. 

__public int hashCode()__

>  Returns the hash code of this instance. 

__public boolean equals(Object obj)__

>  Compares this object with another for equality.  

_Parameters_

> _obj_ - The object to compare to. 

}

----

    

public _abstract_ _class_ __JSONValue__   [se.natusoft.osgi.aps.json] {

>  This class is based on the structure defined on http://www.json.org/. 

> This is a base class for all other JSON* classes. It represents the "value" diagram on the above mentioned web page: 

                                                      Subclasses
                                                      ----------
    |________________ (STRING) ________________|      JSONString
    |  |_____________ (NUMBER) _____________|  |      JSONNumber
       |_____________ (OBJECT) _____________|         JSONObject
       |_____________ (ARRAY)  _____________|         JSONArray
       |_____________ (true)   _____________|     \__ JSONBoolean
       |_____________ (false)  _____________|     /
       \_____________ (null)   _____________/         JSONNull
    

>    @author Tommy Svensson 





__protected JSONValue(JSONErrorHandler errorHandler)__

>  Creates a new JSONValue.  Creates a new JSONValue 

__protected abstract void readJSON(char c, JSONReader reader) throws IOException__

>  This will read the vale from an input stream.  

_Returns_

> the last character read.

_Parameters_

> _c_ - The first character already read from the input stream. 

> _reader_ - The reader to read from. 

_Throws_

> _IOException_ - on IO failure. 

__protected abstract void writeJSON(JSONWriter writer, boolean compact) throws IOException__

>  This will write the data held by this JSON value in JSON format on the specified stream.  

_Parameters_

> _writer_ - A JSONWriter instance to write with. 

> _compact_ - If true write the JSON as compact as possible. false means readable, indented. 

_Throws_

> _IOException_ - On IO failure. 

__protected JSONErrorHandler getErrorHandler()__

>  

_Returns_

> The user supplied error handler.







__protected void warn(String message)__

>  Provide a warning.  

_Parameters_

> _message_ - The warning message. 

__protected void fail(String message, Throwable cause)__

>  Fails the job.  

_Parameters_

> _message_ - The failure message. 

> _cause_ - An eventual cause of the failure. Can be null. 

__protected void fail(String message)__

>  Fails the job.  

_Parameters_

> _message_ - The failure message. 

__public void readJSON(InputStream is) throws IOException__

>  This will read the value from an input stream.  

_Parameters_

> _is_ - The input stream to read from. 

_Throws_

> _IOException_ - on IO failure. 

__public void writeJSON(OutputStream os) throws IOException__

>  This writes JSON to the specified OutputStream.  

_Parameters_

> _os_ - The outoutStream to write to. 

_Throws_

> _IOException_ - on IO failure. 

__public void writeJSON(OutputStream os, boolean compact) throws IOException__

>  This writes JSON to the specified OutputStream.  

_Parameters_

> _os_ - The outoutStream to write to. 

> _compact_ - If true write JSON as compact as possible. If false write it readable with indents. 

_Throws_

> _IOException_ - on IO failure. 





















__protected JSONReader(PushbackReader reader, JSONErrorHandler errorHandler)__

>  Creates a new JSONReader instance.  

_Parameters_

> _reader_ - The PushbackReader to read from. 

> _errorHandler_ - The handler for errors. 

__protected char getChar() throws IOException__

>  Returns the next character on the specified input stream, setting EOF state checkable with isEOF().  

_Throws_

> _IOException_ - on IO problems. 

__protected char getChar(boolean handleEscapes) throws IOException__

>  Returns the next character on the specified input stream, setting EOF state checkable with isEOF().  

_Parameters_

> _handleEscapes_ - If true then \* escape character are handled. 

_Throws_

> _IOException_ - on IO problems. 

__protected void ungetChar(char c) throws IOException__

>  Unreads the specified character so that the next call to getNextChar() will return it again.  

_Parameters_

> _c_ - The character to unget. 

__protected char skipWhitespace(char c) throws IOException__

>  Skips whitespace returning the first non whitespace character. This also sets the EOF flag.  

_Parameters_

> _c_ - The first char already read from the input stream. 

_Throws_

> _IOException_

__protected char skipWhitespace() throws IOException__

>  Skips whitespace returning the first non whitespace character. This also sets the EOF flag.  

_Throws_

> _IOException_



__protected char readUntil(String until, char c, StringBuilder sb, boolean handleEscapes) throws IOException__

>  Reads until any of a specified set of characters occur.  

_Returns_

> 

_Parameters_

> _until_ - The characters to stop reading at. The stopping character will be returned unless EOF. 

> _c_ - The first preread character. 

> _sb_ - If not null read characters are added to this. The stopping character will not be included. 

> _handleEscapes_ - True if we are reading a string that should handle escape characters. 

_Throws_

> _IOException_

__protected char readUntil(String until, StringBuilder sb, boolean string) throws IOException__

>  Reads until any of a specified set of characters occur.  

_Parameters_

> _until_ - The characters to stop reading at. The stopping character will be returned unless EOF. 

> _sb_ - If not null read characters are added to this. The stopping character will not be included. 

> _string_ - True if we are rading a string that should be escaped. 

_Throws_

> _IOException_

__protected char readUntil(String until, StringBuilder sb) throws IOException__

>  Reads until any of a specified set of characters occur.  

_Parameters_

> _until_ - The characters to stop reading at. The stopping character will be returned unless EOF. 

> _sb_ - If not null read characters are added to this. The stopping character will not be included. 

_Throws_

> _IOException_

__protected boolean checkValidChar(char c, String validChars)__

>  Returns true if c is one of the characters in validChars.  

_Parameters_

> _c_ - The character to check. 

> _validChars_ - The valid characters. 

__protected void assertChar(char a, char e, String message)__

>  Asserts that char a equals expected char c.  

_Parameters_

> _a_ - The char to assert. 

> _e_ - The expected value. 

> _message_ - Failure message. 

__protected void assertChar(char a, String expected, String message)__

>  Asserts that char a equals expected char c.  

_Parameters_

> _a_ - The char to assert. 

> _expected_ - String of valid characters. 

> _message_ - Failure message. 

protected _static_ _class_ __JSONWriter__   [se.natusoft.osgi.aps.json] {

>  For subclasses to use in writeJSON(JSONWriter writer). 



__protected JSONWriter(Writer writer)__

>  Creates a new JSONWriter instance.  

_Parameters_

> _writer_ - The writer to write to. 

__protected void write(String json) throws IOException__

>  Writes JSON output.  

_Parameters_

> _json_ - The JSON output to write. 

_Throws_

> _IOException_ - on IO failure. 

__protected void writeln(String json) throws  IOException__

>  Writes JSON output plus a newline.  

_Parameters_

> _json_ - The JSON output to write. 

_Throws_

> _IOException_

}

----

    

public _class_ __BeanInstance__   [se.natusoft.osgi.aps.json.tools] {

>  This wraps a Java Bean instance allowing it to be populated with data using setProperty(String, Object) methods handling all reflection calls. 



__public BeanInstance(Object modelInstance)__

>  Creates a new ModelInstance.  

_Parameters_

> _modelInstance_ - The model instance to wrap. 

__public Object getModelInstance()__

>  Returns the test model instance held by this object. 

__public List<String> getSettableProperties()__

>  Returns a list of settable properties. 

__public List<String> getGettableProperties()__

>  Returns a list of gettable properties. 

__public void setProperty(String property, Object value) throws JSONConvertionException__

>  Sets a property  

_Parameters_

> _property_ - The name of the property to set. 

> _value_ - The value to set with. 

_Throws_

> _JSONConvertionException_ - on any failure to set the property. 

__public Object getProperty(String property) throws JSONConvertionException__

>  Returns the value of the specified property.  

_Returns_

> The property value.

_Parameters_

> _property_ - The property to return value of. 

_Throws_

> _JSONConvertionException_ - on failure (probably bad property name!). 



__public Class getPropertyType(String property) throws JSONConvertionException__

>  Returns the type of the specified property.  

_Returns_

> The class representing the property type.

_Parameters_

> _property_ - The property to get the type for. 

_Throws_

> _JSONConvertionException_ - if property does not exist. 

}

----

    

public _class_ __JavaToJSON__   [se.natusoft.osgi.aps.json.tools] {

>  Takes a JavaBean and produces a JSONObject. 

__public static JSONObject convertObject(Object javaBean) throws JSONConvertionException__

>  Converts a JavaBean object into a JSONObject.  

_Returns_

> A JSONObject containing all values from the JavaBean.

_Parameters_

> _javaBean_ - The JavaBean object to convert. 

_Throws_

> _JSONConvertionException_ - on converting failure. 

__public static JSONObject convertObject(JSONObject jsonObject, Object javaBean) throws JSONConvertionException__

>  Converts a JavaBean object into a JSONObject.  

_Returns_

> A JSONObject containing all values from the JavaBean.

_Parameters_

> _jsonObject_ - The jsonObject to convert the bean into or null for a new JSONObject. 

> _javaBean_ - The JavaBean object to convert. 

_Throws_

> _JSONConvertionException_ - on converting failure. 

__public static JSONValue convertValue(Object value)__

>  Converts a value from a java value to a JSONValue.  

_Returns_

> The converted JSONValue.

_Parameters_

> _value_ - The java value to convert. It can be one of String, Number, Boolean, null, JavaBean, or an array of those. 

}

----

    

public _class_ __JSONConvertionException__ extends  RuntimeException    [se.natusoft.osgi.aps.json.tools] {

>  This exception is thrown on failure to convert from JSON to Java or Java to JSON. 

> Almost all exceptions within the APS services and libraries extend either APSException or APSRuntimeException. I decided to just extend RuntimeException here to avoid any other dependencies for this library since it can be useful outside of APS and can be used as any jar if not deployed in OSGi container. 

__public JSONConvertionException(final String message)__

>  Creates a new JSONConvertionException.  

_Parameters_

> _message_ - The exception message 

__public JSONConvertionException(final String message, final Throwable cause)__

>  Creates a new JSONConvertionException.  

_Parameters_

> _message_ - The exception message 

> _cause_ - The cause of this exception. 

}

----

    

public _class_ __JSONToJava__   [se.natusoft.osgi.aps.json.tools] {

>  Creates a JavaBean instance and copies data from a JSON value to it. 

> The following mappings are made in addition to the expected ones: <ul  <li JSONArray only maps to an array property. li  <li Date properties in bean are mapped from JSONString "yyyy-MM-dd HH:mm:ss" li  <li Enum properties in bean are mapped from JSONString which have to contain enum constant name. li  ul 

__public static <T> T convert(InputStream jsonStream, Class<T> javaClass) throws IOException, JSONConvertionException__

>  Returns an instance of a java class populated with data from a json object value read from a stream.  

_Returns_

> A populated instance of javaClass.

_Parameters_

> _jsonStream_ - The stream to read from. 

> _javaClass_ - The java class to instantiate and populate. 

_Throws_

> _IOException_ - on IO failures. 

> _JSONConvertionException_ - On JSON to Java failures. 

__public static <T> T convert(String json, Class<T> javaClass) throws IOException, JSONConvertionException__

>  Returns an instance of a java class populated with data from a json object value read from a String containing JSON.  

_Returns_

> A populated instance of javaClass.

_Parameters_

> _json_ - The String to read from. 

> _javaClass_ - The java class to instantiate and populate. 

_Throws_

> _IOException_ - on IO failures. 

> _JSONConvertionException_ - On JSON to Java failures. 

__public static <T> T convert(JSONValue json, Class<T> javaClass) throws JSONConvertionException__

>  Returns an instance of java class populated with data from json.   

_Returns_

> A converted Java object.

_Parameters_

> _json_ - The json to convert to java. 

> _javaClass_ - The class of the java object to convert to. 

_Throws_

> _JSONConvertionException_ - On failure to convert. 







}

----

    

public _class_ __SystemOutErrorHandler__ implements  JSONErrorHandler    [se.natusoft.osgi.aps.json.tools] {

>  A simple implementation of JSONErrorHandler that simply displays messages on System.out and throws a RuntimeException on fail. This is used by the tests. In a non test case another implementation is probably preferred. 

__public void warning(String message)__

>  Warns about something.  

_Parameters_

> _message_ - The warning message. 

__public void fail(String message, Throwable cause) throws RuntimeException__

>  Indicate failure.  

_Parameters_

> _message_ - The failure message. 

> _cause_ - The cause of the failure. Can be null! 

_Throws_

> _RuntimeException_ - This method must throw a RuntimeException. 

}

----

    

