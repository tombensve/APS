/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-03-07: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.models;

import java.io.Serializable;
import java.util.Iterator;

/**
 * This represents a simple ID backed by an integer.
 */
public class IntID implements ID, Serializable {
    //
    // Private Members
    //

    /**
     * The internal id representation.
     */
    private int id = 0;

    //
    // Constructors
    //

    /**
     * Creates a new ItemId.
     */
    public IntID() {}

    /**
     * Creates a new ItemId.
     *
     * @param id The internal id of this ItemId.
     */
    private IntID(int id) {
        this.id = id;
    }

    //
    // Methods
    //

    /**
     * @return Returns a new ItemId.
     */
    public IntID nextId() {
        return new IntID(this.id + 1);
    }

    /**
     * Returns an Iterator of ID's between the start and stop ID.
     *
     * @param start The first ID to return.
     * @param stop The last ID to return.
     */
    public static Iterator<IntID> rangeIterator(IntID start, IntID stop) {
        return new IDRangeIterator(start.id, stop.id);
    }

    /**
     * Creates a new unique ID.
     *
     * @return A newly created ID.
     */
    @Override
    public ID newID() {
        return nextId();
    }

    /**
     * Tests for equality.
     *
     * @param obj The object to compare with.
     *
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntID) {
            return ((IntID) obj).id == this.id;
        }
        return false;
    }

    /**
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return this.id;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * The implementor must ensure sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))` for all `x` and `y`.  (This
     * implies that `x.compareTo(y)` must throw an exception iff
     * `y.compareTo(x)` throws an exception.)
     *
     * The implementor must also ensure that the relation is transitive:
     * `(x.compareTo(y)>0 && y.compareTo(z)>0)` implies
     * `x.compareTo(z)>0`.
     *
     * Finally, the implementor must ensure that `x.compareTo(y)==0`
     * implies that `sgn(x.compareTo(z)) == sgn(y.compareTo(z))`, for
     * all `z`.
     *
     * It is strongly recommended, but _not_ strictly required that
     * `(x.compareTo(y)==0) == (x.equals(y))`.  Generally speaking, any
     * class that implements the _Comparable_ interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * In the foregoing description, the notation
     * `sgn(`_expression_`)` designates the mathematical
     * _signum_ function, which is defined to return one of `-1`,
     * `0`, or `1` according to whether the value of
     * _expression_ is negative, zero or positive.
     *
     * @param o the object to be compared.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    public int compareTo(Integer o) {
        if (o < this.id) {
            return -1;
        } else if (o > this.id) {
            return 1;
        }
        return 0;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * The implementor must ensure sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))` for all `x` and `y`.  (This
     * implies that `x.compareTo(y)` must throw an exception iff
     * `y.compareTo(x)` throws an exception.)
     *
     * The implementor must also ensure that the relation is transitive:
     * `(x.compareTo(y)>0 && y.compareTo(z)>0)` implies
     * `x.compareTo(z)>0`.
     *
     * Finally, the implementor must ensure that `x.compareTo(y)==0`
     * implies that `sgn(x.compareTo(z)) == sgn(y.compareTo(z))`, for
     * all `z`.
     *
     * It is strongly recommended, but _not_ strictly required that
     * `(x.compareTo(y)==0) == (x.equals(y))`.  Generally speaking, any
     * class that implements the _Comparable_ interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * In the foregoing description, the notation
     * `sgn(`_expression_`)` designates the mathematical
     * _signum_ function, which is defined to return one of `-1`,
     * `0`, or `1` according to whether the value of
     * _expression_ is negative, zero or positive.
     *
     * @param o the object to be compared.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(ID o) {
        if (o instanceof IntID) {
            return compareTo(((IntID)o).id);
        }
        return -1; // Decided that this is better than a RuntimeException!
    }

    //
    // Inner Classes
    //

    /**
     * Iterates over a range of ids.
     */
    private static class IDRangeIterator implements Iterator<IntID> {
        //
        // Private Members
        //
        
        /** The current id of this iterator. */
        private int current = 0;
        
        /** The end of id iteration. */
        private int end = 0;
        
        //
        // Constructors
        //

        /**
         * Creates a new IDRangeIterator.
         * 
         * @param start The start id. 
         * @param end The end id.
         */
        public IDRangeIterator(int start, int end) {
            this.current = start;
            this.end = end;
        }
        
        /**
         * Returns _true_ if the iteration has more elements. (In other
         * words, returns _true_ if _next: would return an element
         * rather than throwing an exception.)
         *
         * @return true if the iterator has more elements.
         */
        @Override
        public boolean hasNext() {
            return this.current < this.end;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         *
         * @throws java.util.NoSuchElementException
         *          iteration has no more elements.
         */
        @Override
        public IntID next() {
            return new IntID(this.current++);
        }

        /**
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).  This method can be called only once per
         * call to _next_.  The behavior of an iterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @throws UnsupportedOperationException if the remove operation is not supported by this Iterator.
         * @throws IllegalStateException         if the next() method has not
         *                                       yet been called, or the remove() method has already
         *                                       been called after the last call to the next()
         *                                       method.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported by this iterator!");
        }
    }
}
