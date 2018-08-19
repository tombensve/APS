/**
 * Logging utility that automatically converts messages to logable type.
 *
 * ### Usage:
 *
 *     this.logger.info/warn/error/debug("Something: {}, something else: {}", [ someObject, "whatever" ]);
 *     this.logger.warn("{} is on a collision course with your house!", ["Santa"] );
 *     this.logger.error("{} ate all candied apples!", "Rudolf" );
 *
 * - All {} will be replaced with the string version of the corresponding value in 'data' array.
 *   The first {} will get the first value in the array and so on.
 *
 * - If the second argument is not an array it will be put in an array of one object. This allows you
 *   to skip the array brackets when there is only one data value.
 *
 * And no I did not write this at Christmas time. Santa and Rudolf was the only things that popped up
 * in my mind, don't ask me whu :-).
 */
export default class APSLogger {

    /**
     * Sets who we are logging for.
     *
     * @param {string} who
     */
    constructor( who ) {
        this.who = who;

    }

    /**
     * Formats log message.
     *
     * @param {string} type                                     - The type of the log entry.
     * @param {string} message                                  - The log message.
     * @param {array.<object|string|number>|string|object} data - Data to be inserted in message.
     */
    logMsg( type, message, ...data ) {

        if ( ! Array.isArray( data) ) {

            data = [ data ]
        }

        for ( let entry of data ) {

            if ( typeof entry === "object" ) {

                entry = JSON.stringify( entry );
            }
            message = message.replace( "{}", entry )
        }

        let now = new Date();
        return "[ " + now.toLocaleDateString() + " " + now.toLocaleTimeString() + " | " + type + " | " + this.who +
            " ]: " + message;
    }

    /**
     * Makes an info log.
     *
     * @param {string} message      - The log message.
     * @param {array.<object|string|number>|string|object} data - Data to be inserted in message.
     */
    info( message, ...data) {

        console.info( this.logMsg( "INFO", message, data ) );
    }

    /**
     * Makes a warning log.
     *
     * @param {string} message      - The log message.
     * @param {array.<object|string|number>|string|object} data - Data to be inserted in message.
     */
    warn( message, ...data ) {

        console.warn( this.logMsg( "WARN", message, data ) );
    }

    /**
     * Makes an error log.
     *
     * @param {string} message      - The log message.
     * @param {array.<object|string|number>|string|object} data - Data to be inserted in message.
     */
    error( message, ...data ) {

        console.error( this.logMsg( "ERROR", message, data ) );
    }

    /**
     * Makes a debug log.
     *
     * @param {string} message      - The log message.
     * @param {array.<object|string|number>|string|object} data - Data to be inserted in message.
     */
    debug( message, ...data) {

        console.debug( this.logMsg( "DEBUG", message, ...data ) );
    }

}