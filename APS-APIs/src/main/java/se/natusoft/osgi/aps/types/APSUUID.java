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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-04-08: Created!
 *
 */
package se.natusoft.osgi.aps.types;

import java.io.Serializable;

/**
 * This is an ID implementation based on java.util.UUID.
 */
public class APSUUID implements ID, Serializable {
    //
    // Private Members
    //

    /** The wrapped java.util.UUID. */
    private java.util.UUID uuid;

    //
    // Constructors
    //

    /**
     * Creates a new APSUUID.
     */
    public APSUUID() {
        this.uuid = java.util.UUID.randomUUID();
    }

    /**
     * Creates a new APSUUID from a string formatted UUID. This allows to create an APSUUID
     * instance for an already existing UUID. This is useful because it implements ID which
     * is the general ID type used in APS.
     *
     * @param uuid The UUID to hold in string format.
     */
    public APSUUID(String uuid) {
        this.uuid = java.util.UUID.fromString(uuid);
    }

    //
    // Methods
    //

    /**
     * Creates a new unique ID.
     *
     * @return A newly created ID.
     */
    @Override
    public ID newID() {
        return new APSUUID();
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
        return APSUUID.class.isAssignableFrom(obj.getClass()) && this.uuid.equals(((APSUUID)obj).uuid);
    }

    /**
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return this.uuid.hashCode();
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
        if (o instanceof APSUUID) {
            return this.uuid.compareTo(((APSUUID)o).uuid);
        }

        return -1;
    }

    public String toString() {
        return this.uuid.toString();
    }
}
