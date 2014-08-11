/* 
 * 
 * PROJECT
 *     Name
 *         APS Simple User Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides an implementation of APSSimpleUserService backed by a database.
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
 *         2012-08-24: Created!
 *         
 */
package se.natusoft.osgi.aps.userservice.entities;

import se.natusoft.osgi.aps.api.auth.user.exceptions.APSSimpleUserServiceException;
import se.natusoft.osgi.aps.api.auth.user.model.Role;
import se.natusoft.osgi.aps.api.auth.user.model.RoleAdmin;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This is both a JPA entity representing a Role.
 */
@Entity(name = "Role")
public class RoleEntity implements RoleAdmin {

    //
    // Persistent members
    //

    @Id @Column(name="id")
    private String id;

    @Column(name="description")
    private String description;

    @ManyToMany(fetch= FetchType.EAGER)
    @JoinTable(
            name="role_role",
            joinColumns = @JoinColumn(name="master_role_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="role_id", referencedColumnName="id")
    )
    private List<RoleEntity> roles;

    @Column(name="master")
    private boolean master;


    //
    // Constructors
    //

    /**
     * Creates a new RoleEntity instance.
     */
    public RoleEntity() {}

    /**
     * Creates a new RoleEntity instance.
     *
     * @param id The id of the role.
     */
    public RoleEntity(String id) {
        this.id = id;
    }

    //
    // Methods
    //

    /**
     * @return The id of the role.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * @return A description of the role.
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns true if the role has the specified sub role name.
     *
     * @param roleName The name of the role to check for.
     */
    @Override
    public boolean hasRole(String roleName) {
        boolean hasRole = false;

        if (this.roles != null) {
            // Lets check the "local" roles first.
            for (Role role : this.roles) {
                hasRole = role.getId().equals(roleName);
                if (hasRole) break;
            }

            // Then we check the sub roles if no match has been found.
            if (!hasRole) {
                for (Role role : this.roles) {
                    hasRole = role.hasRole(roleName);
                    if (hasRole) break;
                }
            }
        }

        return hasRole;
    }

    /**
     * @return true if this role is a master role. Only master roles can be added to users.
     */
    @Override
    public boolean isMasterRole() {
        return this.master;
    }

    /**
     * Changes the descriptiopn of the role.
     *
     * @param description The new description.
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns all sub roles for this role.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List getRoles() { // Can't use generics here since the java compiler cannot figure out that RoleEntity implements Role!
        return this.roles;
    }

    /**
     * Adds a sub role to this role.
     *
     * @param role The role to add.
     */
    @Override
    public void addRole(Role role) {
        if (role.isMasterRole()) {
            throw new APSSimpleUserServiceException("Bad role '" + role.getId() + "'! A master role cannot be added to another role!");
        }
        if (this.roles == null) {
            this.roles = new LinkedList<RoleEntity>();
        }
        this.roles.add((RoleEntity)role);
    }

    /**
     * Removes a sub role from this role.
     *
     * @param role The role to remove.
     */
    @Override
    public void removeRole(Role role) {
        if (role == null) {
            throw new APSSimpleUserServiceException("Bad argument! Can't remove null role!");
        }
        if (RoleEntity.class.isAssignableFrom(role.getClass())) {
            throw new APSSimpleUserServiceException("Don't support other implementations of Role! Must be a RoleEntity! Got:" +
                    role.getClass());
        }
        if (role.isMasterRole()) {
            throw new APSSimpleUserServiceException("Bad role '" + role.getId() + "'! A master role cannot be added to another role!");
        }
        // Hmm ... the IDE thinks the cast to RoleEntity is redundant, but the roles List is of RoleEntity type, and even
        // if RoleEntity implements Role it cannot be sure that role is a RoleEntity! It should complain about unsafe cast!
        // But if I remove the cast it complains about suspicious call instead! So there is no way out here! Maybe the problem
        // here is that I'm lazy and reusing the JPA entity class as a result object for my service by having the entity class
        // implement the Role interface. Maybe I need to separate them into 2 different objects and copy back and forth.
        // That feels somewhat annoying though. They will be identical with the exception that the Role implementation will not
        // have the JPA annotations. Reusing the RoleEntity work perfectly fine (with the exception of this annoying warning).
        // Sigh!
        if (!roles.contains((RoleEntity)role)) {
            throw new APSSimpleUserServiceException("Can't remove nonexistent role: " + role.getId());
        }
        if (this.roles != null) {
            this.roles.remove((RoleEntity)role);
        }
    }

    /**
     * Sets whether this is a master role or not.
     *
     * @param masterRole true for master role.
     */
    @Override
    public void setMasterRole(boolean masterRole) {
        this.master = masterRole;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p/>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p/>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p/>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p/>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p/>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param role the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Role role) {
        return this.id.compareTo(((RoleEntity)role).id);
    }
}
