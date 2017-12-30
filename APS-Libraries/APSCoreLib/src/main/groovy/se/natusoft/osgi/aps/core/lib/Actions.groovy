package se.natusoft.osgi.aps.core.lib

/**
 * Handles actions to perform.
 */
class Actions implements Runnable {
    /** The list of actions. */
    private List<Closure> actions = [ ]

    /** This will be called if non null on error. An exception will be passed. */
    Closure errorHandler

    /**
     * Adds an action to execute at some later time.
     *
     * @param action The action to execute.
     */
    synchronized void addAction( Closure action ) {
        this.actions += action
    }

    /**
     * Allow left shift operator to add action.
     *
     * @param action The action to add.
     */
    void leftShift( Closure action ) {
        addAction( action )
    }

    /**
     * Allows plus operator.
     *
     * @param action The action to add.
     */
    void plus( Closure action ) {
        addAction( action )
    }

    /**
     * Execute all actions and clear the action list.
     */
    synchronized void run() {

        if ( !this.actions.isEmpty() ) {

            this.actions.each { Closure closure ->

                try {
                    closure.call()
                }

                catch ( Exception e ) {

                    if ( this.errorHandler != null ) {
                        this.errorHandler.call( e )
                    }
                }
            }

            this.actions.clear()
        }
    }

    /**
     * Discards current actions.
     */
    synchronized void discardActions() {
        this.actions.clear()
    }
}
