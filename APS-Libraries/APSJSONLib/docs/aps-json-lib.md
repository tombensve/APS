# APSJSONLib

This is a library (exports all its packages and provides no service) for reading and writing JSON. It can also write a JavaBean object as JSON and take a JSON value or inputstream containing JSON and produce a JavaBean.

This basically provides a class representing each JSON type: JSONObject, JSONString, JSONNumber, JSONBoolean, JSONArray, JSONNull, and a JSONValue class that is the common base class for all the other. Each class knows how to read and write the JSON type it represents. Then there is a JavaToJSON and a JSONToJava class with static methods for converting back and forth. This mapping is very primitive. There has to be one to one between the JSON and the Java objects.

## Changes

### 0.10.0

_readJSON(...)_ in the __JSONValue__ base class now throws JSONEOFException (extends IOException) on EOF. The reason for this is that internally it reads characters which cannot return -1 or any non JSON data valid char to represent EOF. Yes, it would be possible to replace _char_ with _Character_, but that will have a greater effect on existing code using this lib. If an JSONEOFException comes and is not handled it is still very much more clear what happened than a NullPointerException would be!

## APIs

Complete javadocs can be found at [http://apidoc.natusoft.se/APSJSONLib/](http://apidoc.natusoft.se/APSJSONLib/).

public _class_ __JSONConvertionException__ extends  RuntimeException    [se.natusoft.osgi.aps.json] {

This exception is thrown on failure to convert from JSON to Java or Java to JSON.

Almost all exceptions within the APS services and libraries extend either _APSException_ or _APSRuntimeException_. I decided to just extend RuntimeException here to avoid any other dependencies for this library since it can be useful outside of APS and can be used as any jar if not deployed in OSGi container.

__public JSONConvertionException(final String message)__

Creates a new _JSONConvertionException_.

_Parameters_

> _message_ - The exception message 

__public JSONConvertionException(final String message, final Throwable cause)__

Creates a new _JSONConvertionException_.

_Parameters_

> _message_ - The exception message 

> _cause_ - The cause of this exception. 

}

----

    





__public JSONArray()__

Creates a new JSONArray for wrinting JSON output.

__public JSONArray(JSONErrorHandler errorHandler)__

Creates a new JSONArray for reading JSON input and writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 





__public void addValue(JSONValue value)__

Adds a value to the array.

_Parameters_

> _value_ - The value to add. 







}

----

    





__public BeanInstance(Object modelInstance)__

Creates a new ModelInstance.

_Parameters_

> _modelInstance_ - The model instance to wrap. 

__public Object getModelInstance()__

Returns the test model instance held by this object.

__public List<String> getSettableProperties()__

Returns a list of settable properties.

__public List<String> getGettableProperties()__

Returns a list of gettable properties.

__public void setProperty(String property, Object value) throws JSONConvertionException__

Sets a property

_Parameters_

> _property_ - The name of the property to set. 

> _value_ - The value to set with. 

_Throws_

> _JSONConvertionException_ - on any failure to set the property. 

__public Object getProperty(String property) throws JSONConvertionException__

Returns the value of the specified property.

_Returns_

> The property value.

_Parameters_

> _property_ - The property to return value of. 

_Throws_

> _JSONConvertionException_ - on failure (probably bad property name!). 



__public Class getPropertyType(String property) throws JSONConvertionException__

Returns the type of the specified property.

_Returns_

> The class representing the property type.

_Parameters_

> _property_ - The property to get the type for. 

_Throws_

> _JSONConvertionException_ - if property does not exist. 

}

----

    



__public static void read( InputStream jsonIn, APSHandler<APSResult<JSONValue>> resultHandler )__

Reads any JSON object from the specified _InputStream_.

_Parameters_

> _jsonIn_ - The InputStream to read from. 

> _resultHandler_ - The handler to call with result. 

__public static void readToMap( InputStream jsonIn, APSHandler<APSResult<Map<String, Object>>> resultHandler )__

Reads a JSON InputSteam and returns the JSON structure as a Map<String, Object>.

_Parameters_

> _jsonIn_ - The JSON stream to read. 

> _resultHandler_ - The handler to receive the result. 

__public static void resourceToMap(String resource, APSHandler<APSResult<Map<String, Object>>> resultHandler)__

Reads a JSON classpath resource and returns the JSON structure as a Map<String,Object>.

_Parameters_

> _resource_ - The resource to read. 

> _resultHandler_ - The handler to receive the result. 

__public static JSONValue read( InputStream jsonIn, JSONErrorHandler errorHandler )__

Reads any JSON object from the specified _InputStream_.

_Returns_

> A JSONValue subclass. Which depends on what was found on the stream.

_Parameters_

> _jsonIn_ - The InputStream to read from. 

> _errorHandler_ - An implementation of this interface should be supplied by the user to handle any errors during JSON parsing. 

_Throws_

> _APSIOException_ - on any IO failures. 



__public static void write( OutputStream jsonOut, JSONValue value ) throws APSIOException__

Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

_Throws_

> _APSIOException_ - on failure. 

__public static void write( OutputStream jsonOut, JSONValue value, boolean compact, APSHandler<APSResult<Void>> resultHandler )__

Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

> _resultHandler_ - handler for result. only success() or failure() is relevant. 

__public static void write( OutputStream jsonOut, JSONValue value, boolean compact ) throws APSIOException__

Writes a _JSONValue_ to an _OutputStream_.

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

> _compact_ - If true the written JSON is made very compact and hard to read but produce less data. 

_Throws_

> _APSIOException_ - on IO problems. 

__public static byte[] jsonToBytes( JSONValue jsonValue ) throws APSIOException__

Converts a JSONValue into bytes.

_Returns_

> A byte array.

_Parameters_

> _jsonValue_ - The JSONValue to convert. 

_Throws_

> _APSIOException_ - on any IO failure. 

__public static JSONValue bytesToJson( byte[] bytes )__

Converts a byte array into a JSONValue object. For this to work the byte array of course must contain valid JSON!

_Parameters_

> _bytes_ - The bytes to conve rt. 

__public static String jsonToString( JSONValue jsonValue ) throws APSIOException__

Converts a JSONValue to a String of JSON.

_Returns_

> A String of JSON.

_Parameters_

> _jsonValue_ - The json value to convert. 

_Throws_

> _APSIOException_ - on failure. Since the JSON is valid and we are writing to memory this is unlikely ... 

__public static JSONValue stringToJson( String jsonString ) throws APSIOException__

Converts a String with JSON into a JSONValue.

_Returns_

> Whatever JSON object the string contained, as a base JSONValue.

_Parameters_

> _jsonString_ - The JSON String to convert. 

_Throws_

> _APSIOException_ - on failure, like bad JSON in string. 





__public static Map<String, Object> jsonObjectToMap( JSONObject jsonObject )__

This takes a JSONObject and returns a Map.

_Returns_

> The converted Map.

_Parameters_

> _jsonObject_ - The JSONObject to convert to a Map. 

__public static JSONObject mapToJSONObject( Map<String, Object> map )__

Converts a `Map<String,``Object>` to a JSONObject.

_Returns_

> A converted JSONObject.

_Parameters_

> _map_ - The Map to convert. 



__public static Map<String, Object> readJSONAsMap( InputStream jsonIn, JSONErrorHandler errorHandler )__

For consitency. The same as doing JSON.jsonObjectToMap(InputStream, JSONErrorHandler).

_Returns_

> A Map\<String, Object\> of JSON data.

_Parameters_

> _jsonIn_ - The input stream to read. 

> _errorHandler_ - The error handler to use. 

__public static Map<String, Object> stringToMap( String json )__

Converts from String to JSON to Map.

_Returns_

> A Map representation of the JSON.

_Parameters_

> _json_ - The JSON String to convert. 

__public static String mapToString( Map<String, Object> map )__

Converts from Map to JSONObject to String.

_Returns_

> A String containing JSON.

_Parameters_

> _map_ - The Map to convert. 













}

----

    

public _class_ __CollectingErrorHandler__ implements  JSONErrorHandler    [se.natusoft.osgi.aps.json] {

Utility implementation of JSONErrorHandler.

__public CollectingErrorHandler(boolean printWarnings)__

_Parameters_

> _printWarnings_ - If true warnings will be printed to stderr. 





__public boolean hasMessages()__

_Returns_

> true if there are any messages.

__public String toString()__

_Returns_

> All messages as one string.

}

----

    







__protected JSONValue()__

Creates a new JSONValue.

__protected JSONValue(JSONErrorHandler errorHandler)__

Creates a new JSONValue

__protected abstract void readJSON(char c, JSONReader reader) throws APSIOException__

This will read the vale from an input stream.

_Parameters_

> _c_ - The first character already read from the input stream. 

> _reader_ - The reader to read from. 

_Throws_

> _APSIOException_ - on IO failure. 

__protected abstract void writeJSON(JSONWriter writer, boolean compact) throws APSIOException__

This will write the data held by this JSON value in JSON format on the specified stream.

_Parameters_

> _writer_ - A JSONWriter instance to write with. 

> _compact_ - If true write the JSON as compact as possible. false means readable, indented. 

_Throws_

> _APSIOException_ - On IO failure. 

__protected JSONErrorHandler getErrorHandler()__

_Returns_

> The user supplied error handler.

__/*package*/__

Reads and resolves what JSON type is the next in the input and returns it.

_Returns_

> The read JSONValue.

_Parameters_

> _c_ - The first already read character. 

> _reader_ - The reader to read from. 

> _errorHandler_ - The user supplied error handler. 

_Throws_

> _APSIOException_ - on IOFailure. 







__protected void fail(String message, Throwable cause)__

Fails the job.

_Parameters_

> _message_ - The failure message. 

> _cause_ - An eventual cause of the failure. Can be null. 

__protected void fail(String message)__

Fails the job.

_Parameters_

> _message_ - The failure message. 

__public void readJSON(InputStream is) throws APSIOException__

This will read the value from an input stream.

_Parameters_

> _is_ - The input stream to read from. 

_Throws_

> _APSIOException_ - on IO failure. 

__public void writeJSON(OutputStream os) throws APSIOException__

This writes JSON to the specified OutputStream.

_Parameters_

> _os_ - The outoutStream to write to. 

_Throws_

> _APSIOException_ - on IO failure. 

__public void writeJSON(OutputStream os, boolean compact) throws APSIOException__

This writes JSON to the specified OutputStream.

_Parameters_

> _os_ - The outoutStream to write to. 

> _compact_ - If true write JSON as compact as possible. If false write it readable with indents. 

_Throws_

> _APSIOException_ - on IO failure. 



__/*package*/__

Method for creating a JSONString instance.

_Parameters_

> _errorHandler_ - The user error handler. 

__/*package*/__

Method for creating a JSONNumber instance.

_Parameters_

> _errorHandler_ - The user error handler. 

__/*package*/__

Method for creating a JSONNull instance.

_Parameters_

> _errorHandler_ - The user error handler. 

__/*package*/__

Method for creating a JSONBoolean instance.

_Parameters_

> _errorHandler_ - The user error handler. 

__/*package*/__

Method for creating a JSONArray instance.

_Parameters_

> _errorHandler_ - The user error handler. 

__/*package*/__

Method for creating a JSONObject instance.

_Parameters_

> _errorHandler_ - The user error handler. 







__protected JSONReader(PushbackReader reader, JSONErrorHandler errorHandler)__

Creates a new JSONReader instance.

_Parameters_

> _reader_ - The PushbackReader to read from. 

> _errorHandler_ - The handler for errors. 

__protected char getChar() throws APSIOException__

Returns the next character on the specified input stream, setting EOF state checkable with isEOF().

_Throws_

> _APSIOException_ - on IO problems. 























protected _static_ _class_ __JSONWriter__   [se.natusoft.osgi.aps.json] {

For subclasses to use in writeJSON(JSONWriter writer).



__protected JSONWriter(Writer writer)__

Creates a new JSONWriter instance.

_Parameters_

> _writer_ - The writer to write to. 

__protected void write(String json) throws APSIOException__

Writes JSON output.

_Parameters_

> _json_ - The JSON output to write. 

_Throws_

> _APSIOException_ - on IO failure. 



}

----

    





__public JSONNumber(Number value)__

Creates a new JSONNumber instance for writing JSON output.

_Parameters_

> _value_ - The numeric value. 

__public JSONNumber(JSONErrorHandler errorHandler)__

Creates a new JSONNumber instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_ - The error handle to use. 



__public Number toNumber()__

Returns the number as a Number.



__public float toFloat()__

Returns the number as a float value.

__public int toInt()__

Returns the number as an int value.

__public long toLong()__

Returns the number as a long value.

__public short toShort()__

Returns the number as a short value.

__public byte toByte()__

Returns the number as a byte value.

__public String toString()__

_Returns_

> number as String.

__public Object to(Class type)__

Returns the number as a value of the type specified by the type parameter.

_Parameters_

> _type_ - The type of the returned number. 





}

----

    



__public static JSONObject convertObject(Object javaBean) throws JSONConvertionException__

Converts a JavaBean object into a _JSONObject_.

_Returns_

> A JSONObject containing all values from the JavaBean.

_Parameters_

> _javaBean_ - The JavaBean object to convert. 

_Throws_

> _JSONConvertionException_ - on converting failure. 

__public static JSONObject convertObject(JSONObject jsonObject, Object javaBean) throws JSONConvertionException__

Converts a JavaBean object into a _JSONObject_.

_Returns_

> A JSONObject containing all values from the JavaBean.

_Parameters_

> _jsonObject_ - The jsonObject to convert the bean into or null for a new JSONObject. 

> _javaBean_ - The JavaBean object to convert. 

_Throws_

> _JSONConvertionException_ - on converting failure. 

__public static JSONValue convertValue(Object value)__

Converts a value from a java value to a _JSONValue_.

_Returns_

> The converted JSONValue.

_Parameters_

> _value_ - The java value to convert. It can be one of String, Number, Boolean, null, JavaBean, or an array of those. 

}

----

    



__public JSONNull()__

Creates a new JSONNull instance for writing JSON output.

__public JSONNull(JSONErrorHandler errorHandler)__

Creates a new JSONNull instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 



__public String toString()__

_Returns_

> as String.





}

----

    





__public JSONObject()__

Creates a JSONObject instance for writing JSON output.

__public JSONObject(JSONErrorHandler errorHandler)__

Creates a new JSONObject instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 





__public Set<JSONString> getValueNames()__

Returns the names of the available properties.

__public JSONValue getValue(JSONString name)__

Returns the named property.

_Parameters_

> _name_ - The name of the property to get. 

__public JSONValue getValue(String name)__

Returns the named property.

_Parameters_

> _name_ - The name of the property to get. 

__public void setValue(JSONString name, JSONValue value)__

Adds a value to this JSONObject instance.

_Parameters_

> _name_ - The name of the value. 

> _value_ - The value. 

__public void setValue(String name, String value)__

Adds a string value.

_Parameters_

> _name_ - The name of the value. 

> _value_ - The value. 

__public void setValue(String name, Number value)__

Adds a numeric value.

_Parameters_

> _name_ - The name of the value. 

> _value_ - The value. 

__public void setValue(String name, boolean value)__

Adds a boolean vlaue.

_Parameters_

> _name_ - The name of the value. 

> _value_ - The value. 

__public void fromMap(Map<String, Object> map)__

populates this JSONObject from the specified Map.

_Parameters_

> _map_ - The Map to import. 

__public Map<String, Object> toMap()__

Returns the JSONObject as a Map.

__public void setValue(String name, JSONValue value)__

Adds a property to this JSONObject instance.

_Parameters_

> _name_ - The name of the property. 

> _value_ - The property value. 





}

----

    





__public JSONString(String value)__

Creates a new JSONString for writing JSON output.

_Parameters_

> _value_ - The value of this JSONString. 

__public JSONString(JSONErrorHandler errorHandler)__

Creates a new JSONString for reading JSON input and writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 















}

----

    

public _class_ __JSONToJava__   [se.natusoft.osgi.aps.json] {

Creates a JavaBean instance and copies data from a JSON value to it.

The following mappings are made in addition to the expected ones:

*  _JSONArray_ only maps to an array property.

*  Date properties in bean are mapped from _JSONString_ "yyyy-MM-dd HH:mm:ss".

*  Enum properties in bean are mapped from _JSONString_ which have to contain enum constant name.

__public static <T> T convert(InputStream jsonStream, Class<T> javaClass) throws APSIOException, JSONConvertionException__

Returns an instance of a java class populated with data from a json object value read from a stream.

_Returns_

> A populated instance of javaClass.

_Parameters_

> _jsonStream_ - The stream to read from. 

> _javaClass_ - The java class to instantiate and populate. 

_Throws_

> _APSIOException_ - on IO failures. 

> _JSONConvertionException_ - On JSON to Java failures. 

__public static <T> T convert(String json, Class<T> javaClass) throws APSIOException, JSONConvertionException__

Returns an instance of a java class populated with data from a json object value read from a String containing JSON.

_Returns_

> A populated instance of javaClass.

_Parameters_

> _json_ - The String to read from. 

> _javaClass_ - The java class to instantiate and populate. 

_Throws_

> _APSIOException_ - on IO failures. 

> _JSONConvertionException_ - On JSON to Java failures. 

__public static <T> T convert( JSONValue json, Class<T> javaClass) throws JSONConvertionException__

Returns an instance of java class populated with data from json.

_Returns_

> A converted Java object.

_Parameters_

> _json_ - The json to convert to java. 

> _javaClass_ - The class of the java object to convert to. 

_Throws_

> _JSONConvertionException_ - On failure to convert. 







}

----

    

public _class_ __SystemOutErrorHandler__ implements  JSONErrorHandler    [se.natusoft.osgi.aps.json] {

A simple implementation of _JSONErrorHandler_ that simply displays messages on System.out and throws a _RuntimeException_ on fail. This is used by the tests. In a non test case another implementation is probably preferred.





}

----

    





__public JSONBoolean(boolean value)__

Creates a new JSONBoolean instance for writing JSON output.

_Parameters_

> _value_ - The value for this boolean. 

__public JSONBoolean(JSONErrorHandler errorHandler)__

Creates a new JSONBoolean instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 



__public void setBooleanValue(boolean value)__

Sets the value of this boolean.

_Parameters_

> _value_ - The value to set. 

__public boolean getAsBoolean()__

Returns the value of this boolean.

__public String toString()__

Returns the value of this boolean as a String.





__public Boolean toBoolean()__

_Returns_

> this JSONBoolean as a Java boolean.

}

----

    



}

----

    

public _interface_ __JSONErrorHandler__   [se.natusoft.osgi.aps.json] {

This is called on warnings or failures.

@author Tommy Svensson

__void warning(String message)__

Warns about something.

_Parameters_

> _message_ - The warning message. 

__void fail(String message, Throwable cause) throws RuntimeException__

Indicate failure.

_Parameters_

> _message_ - The failure message. 

> _cause_ - The cause of the failure. Can be null! 

_Throws_

> _RuntimeException_ - This method must throw a RuntimeException. 

}

----

    

