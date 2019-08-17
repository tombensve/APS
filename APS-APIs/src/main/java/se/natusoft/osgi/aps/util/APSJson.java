/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
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
 *     tommy ()
 *         Changes:
 *         2019-08-17: Created!
 *         
 */
package se.natusoft.osgi.aps.util;

import com.fasterxml.jackson.jr.ob.JSON;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Provides static functions for reading and writing JSON.
 * <p>
 * Current under the surface implementation is Jackson Jr.
 */
@SuppressWarnings( "PackageAccessibility" )
public class APSJson {

    /**
     * Reads a JSON object from an InputStream and returns it as a Map.
     *
     * @param jsonStream The stream to read from.
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
     * @param handler    The handler to call with result.
     */
    @SuppressWarnings( "unused" )
    public static void readObject( InputStream jsonStream, APSHandler<APSResult<Map<String, Object>>> handler ) {
        try {
            handler.handle( APSResult.success( readObject( jsonStream ) ) );
        } catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure( ioe ) );
        }
    }

    /**
     * Reads a JSON object from a String of JSON.
     *
     * @param jsonString The JSON string to parse as JSON.
     */
    @SuppressWarnings( "unused" )
    public static Map<String, Object> readObject( String jsonString ) {
        try {
            InputStream is = new ByteArrayInputStream( jsonString.getBytes() );
            Map<String, Object> map = readObject( is );
            is.close();
            return map;
        } catch ( IOException ioe ) {
            throw new APSIOException( "Failed to read JSON from String!" );
        }
    }

    /**
     * Reactive API version. Reads a JSON object from a String of JSON.
     *
     * @param jsonString The JSON string to parse as JSON.
     */
    @SuppressWarnings( "unused" )
    public static void readObject( String jsonString, APSHandler<APSResult<Map<String, Object>>> handler ) {
        try {
            InputStream bis = new ByteArrayInputStream( jsonString.getBytes() );
            handler.handle( APSResult.success( readObject( bis ) ) );
            bis.close();
        } catch ( APSIOException | IOException ioe ) {
            Exception ioee = ioe;
            if ( ioee instanceof IOException ) {
                ioee = new APSIOException( ioee.getMessage(), ioee );
            }
            handler.handle( APSResult.failure( ioee ) );
        }
    }

    /**
     * Writes a JSON object represented by a Map to an OutputStream.
     *
     * @param json       The JSON map object to write.
     * @param jsonStream The OutputStream to write to.
     */
    @SuppressWarnings( "WeakerAccess" )
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
     * @param json       The JSON map object to write.
     * @param jsonStream The OutputStream to write to.
     * @param handler    The handler to call with result.
     */
    @SuppressWarnings( "unused" )
    public static void writeObject( Map<String, Object> json, OutputStream jsonStream, APSHandler<APSResult<Void>> handler ) {
        try {
            writeObject( json, jsonStream );
            handler.handle( APSResult.success( null ) );
        } catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure( ioe ) );
        }
    }

    /**
     * Reads a JSON array from an InputStream and returns it as a List.
     *
     * @param jsonStream The stream to read from.
     * @return A Map containing the read JSON.
     */
    @SuppressWarnings( "WeakerAccess" )
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
     * @param handler    The handler to call with result.
     */
    @SuppressWarnings( "unused" )
    public static void readArray( InputStream jsonStream, APSHandler<APSResult<List<Object>>> handler ) {
        try {
            handler.handle( APSResult.success( readArray( jsonStream ) ) );
        } catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure( ioe ) );
        }
    }

    /**
     * Writes a JSON array represented by a List to an OutputStream.
     *
     * @param json       The JSON map object to write.
     * @param jsonStream The OutputStream to write to.
     */
    @SuppressWarnings( "WeakerAccess" )
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
     * @param json       The JSON array object to write.
     * @param jsonStream The OutputStream to write to.
     * @param handler    The handler to call with result.
     */
    @SuppressWarnings( "unused" )
    public static void writeArray( List<Object> json, OutputStream jsonStream, APSHandler<APSResult<Void>> handler ) {
        try {
            writeArray( json, jsonStream );
            handler.handle( APSResult.success( null ) );
        } catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure( ioe ) );
        }
    }


}
