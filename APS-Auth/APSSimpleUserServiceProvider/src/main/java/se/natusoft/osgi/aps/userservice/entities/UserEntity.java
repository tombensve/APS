/* 
 * 
 * PROJECT
 *     Name
 *         APS Simple User Service Provider
 *     
 *     Code Version
 *         0.9.1
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
import se.natusoft.osgi.aps.api.auth.user.model.User;
import se.natusoft.osgi.aps.api.auth.user.model.UserAdmin;
import se.natusoft.osgi.aps.userservice.util.Toolkit;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * This entity represents a user.
 */
@Entity(name = "SvcUser")
public class UserEntity implements UserAdmin {
    //
    // Private Members
    //

    @Id @Column(name="id")
    private String id;

    @Column(name="auth")
    private String authentication;

    @Column(name="user_data")
    private String userData;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="user_role",
            joinColumns = @JoinColumn(name="user_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="role_id", referencedColumnName="id")
    )
    private List<RoleEntity> roles;

    @Transient
    private boolean authenticated = false;

    //
    // Constructors
    //

    /**
     * Creates a new UserEntity.
     */
    public UserEntity() {}

    /**
     * Creates a new UserEntity.
     *
     * @param id The id of the user.
     */
    public UserEntity(String id) {
        this.id = id;
    }

    //
    // Methods
    //

    /**
     * Returns the id of the user.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns true if this user is authenticated.
     */
    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    /**
     * Sets the authenticated flag.
     *
     * @param authenticated The new value.
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    /**
     * Returns all roles for this user.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List getRoles() { // Can't use generics here since the java compiler cannot figure out that RoleEntity implements Role!
        return this.roles;
    }

    /**
     * Returns the authentication string for this user.
     */
    public String getAuthentication() {
        return this.authentication;
    }

    /**
     * Sets the authentication string for this user.
     *
     * @param authentication The authentication string to set.
     */
    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    /**
     * Returns true if the user has the specified role name.
     *
     * @param roleName The name of the role to check for.
     */
    @Override
    public boolean hasRole(String roleName) {
        boolean hasRole = false;

        if (this.roles != null) {
            for (Role role : this.roles) {
                if (role.getId().equals(roleName)) {
                    hasRole = true;
                    break;
                }
            }

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
     * Returns properties for the user. Changing these have no effect!!
     */
    public Properties getUserProperties() {
        return Toolkit.stringToProperties(this.userData);
    }

    /**
     * Adds a user property.
     *
     * @param key The key of the property.
     * @param value The value of the property.
     */
    public void addUserProperty(String key, String value) {
        Properties props = getUserProperties();
        props.setProperty(key, value);
        setUserProperties(props);
    }

    /**
     * Removes a user property.
     *
     * @param key The key of the property to remove.
     */
    public void removeUserProperty(String key) {
        Properties props = getUserProperties();
        props.remove(key);
        setUserProperties(props);
    }

    /**
     * Sets properties for the user.
     * <p/>
     * To update the user properties either first do getProperties() do your changes, and then call this method
     * with the changed properties or just use the addUserProperty() and removeUserProperty() methods.
     *
     * @param properties The properties to set.
     */
    public void setUserProperties(Properties properties) {
        this.userData = Toolkit.propertiesToString(properties);
    }

    /**
     * Adds a role to this user.
     *
     * @param role The role to add.
     */
    public void addRole(Role role) {
        if (!role.isMasterRole()) {
            throw new APSSimpleUserServiceException("Bad Role '" + role.getId() + "'! Only master roles can be added to a user!");
        }
        if (this.roles == null) {
            this.roles = new LinkedList<RoleEntity>();
        }
        this.roles.add((RoleEntity)role);
    }

    /**
     * Removes a role from this user.
     *
     * @param role The role to remove.
     */
    public void removeRole(Role role) {
        if (!role.isMasterRole()) {
            throw new APSSimpleUserServiceException("Bad Role '" + role.getId() + "'! A user can only have master roles!");
        }
        if (this.roles != null) {
            RoleEntity roleEntity = (RoleEntity)role;
            this.roles.remove(roleEntity);
        }
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
     * @param user the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(User user) {
        return this.id.compareTo(((UserEntity)user).id);
    }
}
