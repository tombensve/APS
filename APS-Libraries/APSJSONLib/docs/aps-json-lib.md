# APSJSONLib

This is a library (exports all its packages and provides no service) for reading and writing JSON. It can also write a JavaBean object as JSON and take a JSON value or inputstream containing JSON and produce a JavaBean.

This basically provides a class representing each JSON type: JSONObject, JSONString, JSONNumber, JSONBoolean, JSONArray, JSONNull, and a JSONValue class that is the common base class for all the other. Each class knows how to read and write the JSON type it represents. Then there is a JavaToJSON and a JSONToJava class with static methods for converting back and forth. This mapping is very primitive. There has to be one to one between the JSON and the Java objects.

## Changes

### 0.10.0

_readJSON(...)_ in the __JSONValue__ base class now throws JSONEOFException (extends IOException) on EOF. The reason for this is that internally it reads characters which cannot return -1 or any non JSON data valid char to represent EOF. Yes, it would be possible to replace _char_ with _Character_, but that will have a greater effect on existing code using this lib. If an JSONEOFException comes and is not handled it is still very much more clear what happened than a NullPointerException would be!

## APIs

Complete javadocs can be found at [http://apidoc.natusoft.se/APSJSONLib/](http://apidoc.natusoft.se/APSJSONLib/).

public _class_ __JSON__   [se.natusoft.osgi.aps.json] {

This is the official API for reading and writing JSON values.

__public static JSONValue read(InputStream jsonIn, JSONErrorHandler errorHandler) throws IOException__

Reads any JSON object from the specified _InputStream_.

_Returns_

> A JSONValue subclass. Which depends on what was found on the stream.

_Parameters_

> _jsonIn_ - The InputStream to read from. 

> _errorHandler_ - An implementation of this interface should be supplied by the user to handle any errors during JSON parsing. 

_Throws_

> _IOException_ - on any IO failures. 

__public static void write(OutputStream jsonOut, JSONValue value) throws APSIOException__

Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

_Throws_

> _APSIOException_ - on failure. 

__public static void write(OutputStream jsonOut, JSONValue value, boolean compact) throws APSIOException__

Writes a _JSONValue_ to an _OutputStream_.

_Parameters_

> _jsonOut_ - The OutputStream to write to. 

> _value_ - The value to write. 

> _compact_ - If true the written JSON is made very compact and hard to read but produce less data. 

_Throws_

> _APSIOException_ - on IO problems. 

}

----

    





__public JSONArrayProvider()__

Creates a new JSONArray for wrinting JSON output.

__public JSONArrayProvider(JSONErrorHandler errorHandler)__

Creates a new JSONArray for reading JSON input and writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 





__public void addValue(JSONValueProvider value)__

Adds a value to the array.

_Parameters_

> _value_ - The value to add. 



__public List<se.natusoft.osgi.aps.api.misc.json.model.JSONValue> getAsList()__

Returns the array values as a List.

__public <T extends se.natusoft.osgi.aps.api.misc.json.model.JSONValue> List<T> getAsList(Class<T> type)__

Returns the array values as a list of a specific type.

_Returns_

> A list of specified type if type is the same as in the list.

_Parameters_

> _type_ - The class of the type to return values as a list of. 

> _<T>_ - One of the JSONValue subclasses. 





}

----

    





__public JSONBooleanProvider(boolean value)__

Creates a new JSONBoolean instance for writing JSON output.

_Parameters_

> _value_ - The value for this boolean. 

__public JSONBooleanProvider(JSONErrorHandler errorHandler)__

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







}

----

    



__public JSONNullProvider()__

Creates a new JSONNull instance for writing JSON output.

__public JSONNullProvider(JSONErrorHandler errorHandler)__

Creates a new JSONNull instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 



__public String toString()__

_Returns_

> as String.





}

----

    





__public JSONNumberProvider(Number value)__

Creates a new JSONNumber instance for writing JSON output.

_Parameters_

> _value_ - The numeric value. 

__public JSONNumberProvider(JSONErrorHandler errorHandler)__

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

    





__public JSONObjectProvider()__

Creates a JSONObject instance for writing JSON output.

__public JSONObjectProvider(JSONErrorHandler errorHandler)__

Creates a new JSONObject instance for reading JSON input or writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 























__public void addValue(se.natusoft.osgi.aps.api.misc.json.model.JSONString name, JSONValueProvider value)__

Adds a property to this JSONObject instance.

_Parameters_

> _name_ - The name of the property. 

> _value_ - The property value. 

__public void addValue(String name, se.natusoft.osgi.aps.api.misc.json.model.JSONValue value)__

Adds a property to this JSONObject instance.

_Parameters_

> _name_ - The name of the property. 

> _value_ - The property value. 





}

----

    





__public JSONStringProvider(String value)__

Creates a new JSONString for writing JSON output.

_Parameters_

> _value_ - The value of this JSONString. 

__public JSONStringProvider(JSONErrorHandler errorHandler)__

Creates a new JSONString for reading JSON input and writing JSON output.

_Parameters_

> _errorHandler_ - The error handler to use. 















}

----

    







__protected JSONValueProvider()__

Creates a new JSONValue.

__protected JSONValueProvider(JSONErrorHandler errorHandler)__

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

    



__public static JSONObjectProvider convertObject(Object javaBean) throws JSONConvertionException__

Converts a JavaBean object into a _JSONObject_.

_Returns_

> A JSONObject containing all values from the JavaBean.

_Parameters_

> _javaBean_ - The JavaBean object to convert. 

_Throws_

> _JSONConvertionException_ - on converting failure. 

__public static JSONObjectProvider convertObject(JSONObjectProvider jsonObject, Object javaBean) throws JSONConvertionException__

Converts a JavaBean object into a _JSONObject_.

_Returns_

> A JSONObject containing all values from the JavaBean.

_Parameters_

> _jsonObject_ - The jsonObject to convert the bean into or null for a new JSONObject. 

> _javaBean_ - The JavaBean object to convert. 

_Throws_

> _JSONConvertionException_ - on converting failure. 

__public static JSONValueProvider convertValue(Object value)__

Converts a value from a java value to a _JSONValue_.

_Returns_

> The converted JSONValue.

_Parameters_

> _value_ - The java value to convert. It can be one of String, Number, Boolean, null, JavaBean, or an array of those. 

}

----

    

public _class_ __JSONConvertionException__ extends  RuntimeException    [se.natusoft.osgi.aps.json.tools] {

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

    

public _class_ __JSONMapConv__   [se.natusoft.osgi.aps.json.tools] {

This converts between a Java Map and JSON. Do note that this of course uses this library to read and write JSON, but this specific public API only deals with Java and JSON as String or on/in a stream. [p/](p/) This class becomes more useful when used from Groovy since the latter provides much nicer usage of data in Maps. Yes, I know about JSONSlurper and JSONBuilder in Groovy. Those however does not work with @CompileStatic. Maps does.

__public static Map<String, Object> jsonObjectToMap(String json) throws APSIOException__

This takes a String containing a JSON object and returns it as a Map.

_Parameters_

> _json_ - The JSON content to convert to a Map. 

_Throws_

> _APSIOException_ - on failure. 





__public static Map<String, Object> jsonObjectToMap(se.natusoft.osgi.aps.api.misc.json.model.JSONObject jsonObject)__

This takes a JSONObject and returns a Map.

_Returns_

> The converted Map.

_Parameters_

> _jsonObject_ - The JSONObject to convert to a Map. 







__public static String mapToJSONObjectString(Map<String, Object> map) throws APSIOException__

This takes a Map (as created by jsonObjectToMap(...)) and returns a JSON String.

_Parameters_

> _map_ - The Map to convert to JSON. 

_Throws_

> _APSIOException_ - on I/O failures. 

__public static se.natusoft.osgi.aps.api.misc.json.model.JSONObject mapToJSONObject(Map<String, Object> map)__

Converts a `Map<String,``Object>` to a JSONObject.

_Returns_

> A converted JSONObject.

_Parameters_

> _map_ - The Map to convert. 









}

----

    

public _class_ __JSONToJava__   [se.natusoft.osgi.aps.json.tools] {

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

__public static <T> T convert(JSONValue json, Class<T> javaClass) throws JSONConvertionException__

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

    

public _class_ __SystemOutErrorHandler__ implements  JSONErrorHandler    [se.natusoft.osgi.aps.json.tools] {

A simple implementation of _JSONErrorHandler_ that simply displays messages on System.out and throws a _RuntimeException_ on fail. This is used by the tests. In a non test case another implementation is probably preferred.





}

----

    

