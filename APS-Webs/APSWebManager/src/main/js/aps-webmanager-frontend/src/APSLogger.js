/**
 * Logging utility that automatically converts messages to logable type.
 *
 * ### Usage:
 *
 *     this._logger.info/warn/error/debug("Something: {}, something else: {}", [ someObject, "whatever" ]);
 *     this._logger.warn("{} is on a collision course with your house!", ["Santa"] );
 *     this._logger.error("{} ate all candied apples!", "Rudolf" );
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
    constructor( who: string ) {
        this.who = who;

        this.logging = {
            INFO:  { logger: console.info, log: true },
            WARN:  { logger: console.warn, log: true },
            ERROR: { logger: console.error, log: true },
            DEBUG: { logger: typeof console.debug === "function" ? console.debug : console.info, log: true }
        }
    }

    /**
     * Formats log message.
     *
     * @param {string} type                                     - The type of the log entry.
     * @param {string} message                                  - The log message.
     */
    _logMsg( type: string, ...message: string[] ) {

        let msg = "";
        for ( let msgPart of message ) {
            msg += msgPart;
        }

        let now = new Date();
        return "[ " + now.toLocaleDateString() + " " + now.toLocaleTimeString() + " | " + type + " | " + this.who +
            " ]: " + msg;
    }

    _logger( type: String, ...message: String[] ) {
        if ( this.logging[type].log ) {
            this.logging[type].logger( this._logMsg( type, message ) )
        }
    }

    /**
     * Makes an info log.
     *
     * @param {string} message      - The log message.
     */
    info( ...message: string[] ) {

        this._logger( "INFO", message );
    }

    /**
     * Makes a warning log.
     *
     * @param {string} message      - The log message.
     */
    warn( ...message: string[] ) {

        this._logger( "WARN", message );
    }

    /**
     * Makes an error log.
     *
     * @param {string} message      - The log message.
     */
    error( ...message: string[] ) {

        this._logger( "ERROR", message );
    }

    /**
     * Makes a debug log.
     *
     * @param {string} message      - The log message.
     */
    debug( ...message: string[] ) {

        this._logger( "DEBUG", message );
    }

    debugTrace( ...message: string[] ) {
        this.debug( message );
        console.trace();
    }

}