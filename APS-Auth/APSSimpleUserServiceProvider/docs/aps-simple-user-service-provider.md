# APSSimpleUserService

This is an simple, easy to use service for handling logged in users. It provides two services: APSSimpleUserService and APSSimpleUserServiceAdmin. The latter handles all creation, editing, and deletion of roles and users. This service in itself does not require any authentication to use! Thereby you have to trust all code in the server! The APSUserAdminWeb WAB bundle however does require a user with role ’apsadmin’ to be logged in or it will simply repsond with a 401 (UNAUTHORIZED).

So why this and not org.osgi.service.useradmin ? Well, maybe I’m just stupid, but _useradmin_ does not make sense to me. It seems to be missing things, specially for creating. You can create a role, but you cannot create a user. There is no obvious authentication of users. Maybee that should be done via the credentials Dictionary, but what are the expected keys in there ? This service is intended to make user and role handling simple and clear.

## Basic example

To login a user do something like this:

        APSSimpleUserService userService ...
        ...
        User user = userService.getUser(userId);
        if (user == null) {
            throw new AuthException(”Bad login!”);
        }
        if (!userService.authenticateUser(user, password, APSSimpleUserService.AUTH_METHOD_PASSWORD)) {
            throw new AuthException(”Bad login!”);
        }
        ...
        if (user.isAuthenticated() && user.hasRole(”apsadmin”)) {
            ...
        }
        

## Setup

The following SQL is needed to create the database tables used by the service.

        /*
         * This represents one role.
         */
        create table role (
          /* The id and key of the role. */
          id varchar(50) not null primary key,
        
          /* A short description of what the role represents. */
          description varchar(200),
        
          /* 1 == master role, 0 == sub-role. */
          master int
        );
        
        /*
         * This represents one user.
         */
        create table svcuser (
          /* User id and also key. */
          id varchar(50) not null primary key,
        
          /* For the provided implementation this is a password. */
          auth varchar(2000),
        
          /*
           * The service stores string properties for the user here as one long string.
           * These are not meant to be searchable only to provide information about the
           * user.
           *
           * You might want to adapt this size to the amount of data you will be adding
           * to a user.
           */
          user_data varchar(4000)
        );
        
        /*
         * A user can have one or more roles.
         */
        create table user_role (
          user_id varchar(50) not null,
          role_id varchar(50) not null,
          primary key (user_id, role_id),
          foreign key (user_id) references svcuser (id),
          foreign key (role_id) references role (id)
        );
        
        /*
         * A role can have one ore more sub-roles.
         */
        create table role_role (
          master_role_id varchar(50) not null,
          role_id varchar(50) not null,
          primary key (master_role_id, role_id),
          foreign key (master_role_id) references role (id),
          foreign key (role_id) references role (id)
        );
        
        /*
         * ---- This part is mostly an example ----
         * WARNING: You do however need a role called 'apsadmin' to be able to login to
         * /apsadminweb! The name of the user having that role does not matter. As long
         * as it is possible to login to /apsadminweb new roles and users can be created
         * there.
         */
        
        /* The following adds an admin user. */
        insert into role VALUES ('apsadmin', 'Default admin for APS', 1);
        insert into svcuser VALUES ('apsadmin', 'admin', '');
        insert into user_role VALUES ('apsadmin', 'apsadmin');
        
        /* This adds a role for non admin users. */
        insert into role VALUES ('user', 'Plain user', 1);
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

<!--
  
The above empty lines are required due to a bug in iText used to render the PDF. It will move the picture to the next page completely out of context since text after the picture will then come before it. 
-->
After the tables have been created you need to configure a datasource for it in /apsadminweb configuration tab:

![Picture of datasource config gui.](http://download.natusoft.se/Images/APS/APS-Auth/APSSimpleUserServiceProvider/docs/images/DataSourceConfig.png)

Please note that the above picture is just an example. The data source name __APSSimpleUserServiceDS__ is however important. The service will be looking up the entry with that name! The rest of the entry depends on your database and where it is running. Also note that the ”(default)” after the field names in the above picture are the name of the currently selected configuration environment. This configuration is configuration environment specific. You can point out different database servers for different environments for example.

When the datasource is configured and saved then you can go to _”Configuration tab_,_Configurations/aps/adminweb”_ and enable the ”requireauthentication” config. If you do this before setting up the datasource and you have choosen to use the provided implementation of APSAuthService that uses APSSimpleUserService to login then you will be completely locked out.

## Troubleshooting

If you have managed to lock yourself out of /apsadminweb as described above then I suggest editing the _APSFilesystemService root_/filesystems/se.natusoft.osgi.aps.core.config.service.APSConfigServiceProvider/apsconfig-se.natusoft.aps.adminweb-1.0.properties file and changeing the following line:

        se.natusoft.aps.adminweb_1.0_requireauthentication=true
        

to _false_ instead. Then restart the server. Also se the APSFilesystemService documentation for more information. The APSConfigService is using that service to store its configurations.

## APIs

public _interface_ __APSSimpleUserService__   [se.natusoft.osgi.aps.api.auth.user] {

>  This is the API of a simple user service that provide basic user handling that will probably be enough in many cases, but not all. 

> Please note that this API does not declare any exceptions! In the case of an exception being needed the APSSimpleUserServiceException should be thrown. This is a runtime exception. 

__public static final String AUTH_METHOD_PASSWORD = "password"__

>  Password authentication method for authenticateUser(). 

__public Role getRole(String roleId)__

>  Gets a role by its id.  

_Returns_

> A Role object representing the role or null if role was not found.

_Parameters_

> _roleId_ - The id of the role to get. 

__public User getUser(String userId)__

>  Gets a user by its id.  

_Returns_

> A User object representing the user or null if userId was not found.

_Parameters_

> _userId_ - The id of the user to get. 

__public boolean authenticateUser(User user, Object authentication, String authMethod)__

>  Authenticates a user using its user id and user provided authentication.  

_Returns_

> true if authenticated, false otherwise. If true user.isAuthenticated() will also return true.

_Parameters_

> _user_ - The User object representing the user to authenticate. 

> _authentication_ - The user provided authentication data. For example if AuthMethod is AUTH_METHOD_PASSWORD 

> _authMethod_ - Specifies what authentication method is wanted. 

}

----

    

public _interface_ __APSSimpleUserServiceAdmin__ extends  APSSimpleUserService    [se.natusoft.osgi.aps.api.auth.user] {

>  Admin API for APSSimpleUserService. 

__public RoleAdmin createRole(String name, String description)__

>  Creates a new role.  

_Returns_

> a new Role object representing the role.

_Parameters_

> _name_ - The name of the role. This is also the key and cannot be changed. 

> _description_ - A description of the role. This can be updated afterwards. 

__public void updateRole(Role role)__

>  Updates a role.  

_Parameters_

> _role_ - The role to update. 

__public void deleteRole(Role role)__

>  Deletes a role.  

_Parameters_

> _role_ - The role to delete. This will likely fail if there are users still having this role! 

__public List<RoleAdmin> getRoles()__

>  Returns all available roles. 

__public UserAdmin createUser(String id)__

>  Creates a new user. Please note that you get an empty user back. You probably want to add roles and also possibly properties to the user. After you have done that call updateUser(user).  

_Returns_

> A User object representing the new user.

_Parameters_

> _id_ - The id of the user. This is key so it must be unique. 

__public void updateUser(User user)__

>  Updates a user.  

_Parameters_

> _user_ - The user to update. 

__public void deleteUser(User user)__

>  Deletes a user.  

_Parameters_

> _user_ - The user to delete. 

__public List<UserAdmin> getUsers()__

>  Returns all users. 

__public void setUserAuthentication(User user, String authentication)__

>  Sets authentication for the user.  

_Parameters_

> _user_ - The user to set authentication for. 

> _authentication_ - The authentication to set. 

}

----

    

public _class_ __APSAuthMethodNotSupportedException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.auth.user.exceptions] {

>  This is thrown by APSAuthService when the implementation does not support the selected auth method. 

__public APSAuthMethodNotSupportedException(String message)__

>  Creates a new APSAuthMethodNotSupportedException instance.  

_Parameters_

> _message_ - The exception message. 

__public APSAuthMethodNotSupportedException(String message, Throwable cause)__

>  Creates a new APSAuthMethodNotSupportedException instance.  

_Parameters_

> _message_ - The exception message. 

> _cause_ - The exception that is the cause of this one. 

}

----

    

public _class_ __APSSimpleUserServiceException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.auth.user.exceptions] {

>  Indicates a problem with the APSSimpleUserService. 

__public APSSimpleUserServiceException(String message)__

>  Creates a new APSSimpleUserServiceException instance.  

_Parameters_

> _message_ - The exception message. 

__public APSSimpleUserServiceException(String message, Throwable cause)__

>  Creates a new APSSimpleUserServiceException instance.  

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of the exception. 

}

----

    

public _interface_ __Role__ extends  Comparable<Role>    [se.natusoft.osgi.aps.api.auth.user.model] {

>  This defines a role. 

__public String getId()__

>  

_Returns_

> The id of the role.

__public String getDescription()__

>  

_Returns_

> A description of the role.

__public boolean hasRole(String roleName)__

>  Returns true if the role has the specified sub role name.  

_Parameters_

> _roleName_ - The name of the role to check for. 

__boolean isMasterRole()__

>  

_Returns_

> true if this role is a master role. Only master roles can be added to users.

}

----

    

public _interface_ __RoleAdmin__ extends  Role    [se.natusoft.osgi.aps.api.auth.user.model] {

>  Provides update API for Role. 

__public void setDescription(String description)__

>  Changes the description of the role.  

_Parameters_

> _description_ - The new description. 

__public List<Role> getRoles()__

>  Returns all sub roles for this role. 

__public void addRole(Role role)__

>  Adds a sub role to this role.  

_Parameters_

> _role_ - The role to add. 

__public void removeRole(Role role)__

>  Removes a sub role from this role.  

_Parameters_

> _role_ - The role to remove. 

__public void setMasterRole(boolean masterRole)__

>  Sets whether this is a master role or not.  

_Parameters_

> _masterRole_ - true for master role. 

}

----

    

public _interface_ __User__ extends  Comparable<User>    [se.natusoft.osgi.aps.api.auth.user.model] {

>  This defines a User. 

__public String getId()__

>  Returns the unique id of the user. 

__public boolean isAuthenticated()__

>  Returns true if this user is authenticated. 

__public boolean hasRole(String roleName)__

>  Returns true if the user has the specified role name.  

_Parameters_

> _roleName_ - The name of the role to check for. 

__public Properties getUserProperties()__

>  This provides whatever extra information about the user you want. How to use this is upp to the user of the service. There are some constants in this class that provide potential keys for the user properties. 

> Please note that the returned properties are read only! 

__public static final String USER_NAME = "name"__

>  Optional suggestion for user properties key. 

__public static final String USER_PHONE = "phone"__

>  Optional suggestion for user properties key. 

__public static final String USER_PHONE_WORK = "phone.work"__

>  Optional suggestion for user properties key. 

__public static final String USER_PHONE_HOME = "phone.home"__

>  Optional suggestion for user properties key. 

__public static final String USER_EMAIL = "email"__

>  Optional suggestion for user properties key. 

}

----

    

public _interface_ __UserAdmin__ extends  User    [se.natusoft.osgi.aps.api.auth.user.model] {

>  Provides update API for the User. 

__public List<Role> getRoles()__

>  Returns all roles for this user. 

__public void addRole(Role role)__

>  Adds a role to this user.  

_Parameters_

> _role_ - The role to add. 

__public void removeRole(Role role)__

>  Removes a role from this user.  

_Parameters_

> _role_ - The role to remove. 

__public void addUserProperty(String key, String value)__

>  Adds a user property.  

_Parameters_

> _key_ - The key of the property. 

> _value_ - The value of the property. 

__public void removeUserProperty(String key)__

>  Removes a user property.  

_Parameters_

> _key_ - The key of the property to remove. 

__public void setUserProperties(Properties properties)__

>  Sets properties for the user. 

> To update the user properties either first do getProperties() do your changes, and then call this method with the changed properties or just use the addUserProperty() and removeUserProperty() methods.  

_Parameters_

> _properties_ - The properties to set. 

}

----

    

