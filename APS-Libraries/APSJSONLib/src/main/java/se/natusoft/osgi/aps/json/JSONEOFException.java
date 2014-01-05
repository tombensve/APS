package se.natusoft.osgi.aps.json;

import java.io.IOException;

/**
 * Thrown if a JSON structure is tried to be read from a stream that has no more data.
 */
public class JSONEOFException extends IOException {}
