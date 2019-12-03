/**
 * Logging utility providing a little bit more info.
 *
 * ### Usage:
 *
 *   this.logger.info/warn/error/debug(`Something: ${something}, something else: ${else}`);
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