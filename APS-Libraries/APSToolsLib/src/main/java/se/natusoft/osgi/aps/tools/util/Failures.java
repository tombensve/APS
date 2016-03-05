package se.natusoft.osgi.aps.tools.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * This represents a series of collected exceptions.
 */
public class Failures extends RuntimeException {

    //
    // Private Members
    //

    /** The collected exceptions. */
    private List<Exception> exceptionList = new LinkedList<>();

    //
    // Methods
    //

    /**
     * Returns true if there are any exceptions in this object.
     */
    public boolean hasFailures() {
        return !this.exceptionList.isEmpty();
    }

    /**
     * Adds a new exception to this object.
     *
     * @param e The exception to add.
     */
    public void addException(Exception e) {
        this.exceptionList.add(e);
    }

    /**
     * Returns a list of all contained exceptions.
     */
    public List<Exception> getExceptionList() {
        return this.exceptionList;
    }

    /**
     * Prints the stack traces of all contained exceptions.
     *
     * @param ps The PrintStream to write the stack traces to.
     */
    @Override
    public void printStackTrace(PrintStream ps) {
        this.exceptionList.forEach(exception -> exception.printStackTrace(ps));
    }

    /**
     * Prints the stack traces of all contained exceptions.
     *
     * @param pw The PrintWriter to write the stack traces to.
     */
    @Override
    public void printStackTrace(PrintWriter pw) {
        this.exceptionList.forEach(exception -> exception.printStackTrace(pw));
    }
}
