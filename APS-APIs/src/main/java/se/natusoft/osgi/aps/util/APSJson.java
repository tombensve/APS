package se.natusoft.osgi.aps.util;

import com.fasterxml.jackson.jr.ob.JSON;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Provides static functions for reading and writing JSON.
 *
 * Current under the surface implementation is Jackson Jr.
 */
public class APSJson {

    /**
     * Reads a JSON object from an InputStream and returns it as a Map.
     *
     * @param jsonStream The stream to read from.
     *
     * @return A Map containing the read JSON.
     */
    public static Map<String, Object> readObject( InputStream jsonStream ) {
        try {
            return JSON.std.mapFrom( jsonStream );
        } catch ( IOException ioe ) {
            throw new APSIOException( "Failed to read JSON!", ioe );
        }
    }

    /**
     * Reactive API version. Reads a JSON object from an InputStream and returns it as a Map.
     *
     * @param jsonStream The stream to read from.
     * @param handler The handler to call with result.
     */
    public static void readObject( InputStream jsonStream, APSHandler<APSResult<Map<String, Object>>> handler) {
        try {
            handler.handle( APSResult.success( readObject(jsonStream) ) );
        }
        catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure(  ioe ) );
        }
    }

    /**
     * Writes a JSON object represented by a Map to an OutputStream.
     *
     * @param json The JSON map object to write.
     * @param jsonStream The OutputStream to write to.
     */
    public static void writeObject( Map<String, Object> json, OutputStream jsonStream ) {
        try {
            JSON.std.write( json, jsonStream );
        } catch ( IOException ioe ) {
            throw new APSIOException( "Failed to write JSON!", ioe );
        }
    }

    /**
     * Reactive API version. Writes a JSON object represented by a Map to an OutputStream.
     *
     * @param json The JSON map object to write.
     * @param jsonStream The OutputStream to write to.
     * @param handler The handler to call with result.
     */
    public static void writeObject( Map<String, Object> json, OutputStream jsonStream, APSHandler<APSResult<Void>> handler) {
        try {
            writeObject( json, jsonStream );
            handler.handle( APSResult.success( null ) );
        }
        catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure( ioe ) );
        }
    }

    /**
     * Reads a JSON array from an InputStream and returns it as a List.
     *
     * @param jsonStream The stream to read from.
     *
     * @return A Map containing the read JSON.
     */
    public static List<Object> readArray( InputStream jsonStream ) {
        try {
            Object[] array = JSON.std.arrayFrom( jsonStream );
            return Arrays.asList( array );
        } catch ( IOException ioe ) {
            throw new APSIOException( "Failed to read JSON!", ioe );
        }
    }

    /**
     * Reactive API version. Reads a JSON array from an InputStream and returns it as a List.
     *
     * @param jsonStream The stream to read from.
     * @param handler The handler to call with result.
     */
    public static void readArray( InputStream jsonStream, APSHandler<APSResult<List<Object>>> handler) {
        try {
            handler.handle( APSResult.success( readArray(jsonStream) ) );
        }
        catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure(  ioe ) );
        }
    }

    /**
     * Writes a JSON array represented by a List to an OutputStream.
     *
     * @param json The JSON map object to write.
     * @param jsonStream The OutputStream to write to.
     */
    public static void writeArray( List<Object> json, OutputStream jsonStream ) {
        try {
            JSON.std.write( json, jsonStream );
        } catch ( IOException ioe ) {
            throw new APSIOException( "Failed to write JSON!", ioe );
        }
    }

    /**
     * Reactive API version. Writes a JSON array represented by a List to an OutputStream.
     *
     * @param json The JSON array object to write.
     * @param jsonStream The OutputStream to write to.
     * @param handler The handler to call with result.
     */
    public static void writeArray( List<Object> json, OutputStream jsonStream, APSHandler<APSResult<Void>> handler) {
        try {
            writeArray( json, jsonStream );
            handler.handle( APSResult.success( null ) );
        }
        catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure( ioe ) );
        }
    }


}
