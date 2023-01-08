package se.natusoft.aps.core.api

import groovy.transform.CompileStatic

/**
 * Transforms between APS Map JSON and real JSON.
 */
@CompileStatic
interface JsonTransformer {

    /**
     * Takes real JSON from input stream and returns an APSJson object.
     *
     * @param is The InputStreamn to read from.
     *
     * @return An APSJson.
     */
    APSJson fromJSON(InputStream is)

    /**
     * Takes a real JSON as a String and prodcues an APSJson.
     *
     * @param json The JSON String to convert.
     *
     * @return An APSJson.
     */
    APSJson fromString(String json)

    /**
     * Takes and APSJson and returns real JSON as a String.
     *
     * @param apsJson The APSJson to convert.
     *
     * @return A String of JSON.
     */
    String toJson(APSJson apsJson)

    /**
     * Takes an APSJson and writes as real JSON to an OutputStream.
     *
     * @param apsJson The APSJson to write.
     * @param OutputStream The OutputStreamn to write to.
     */
    void toJson(APSJson apsJson, OutputStream)
}

