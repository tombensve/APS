# APSSimpleUserService

This is an simple, easy to use service for handling logged in users. It provides two services: APSSimpleUserService and APSSimpleUserServiceAdmin. The latter handles all creation, editing, and deletion of roles and users. This service in itself does not require any authentication to use! Thereby you have to trust all code in the server! The APSUserAdminWeb WAB bundle however does require a user with role _apsadmin_ to be logged in or it will simply repsond with a 401 (UNAUTHORIZED). 

So why this and not org.osgi.service.useradmin ? Well, maybe I'm just stupid, but _useradmin_ does not make sense to me. It seems to be missing things, specially for creating. You can create a role, but you cannot create a user. There is no obvious authentication of users. Maybee that should be done via the credentials Dictionary, but what are the expected keys in there ? APSSimpleUserService is intended to make user and role handling simple and clear.

## Basic example

To login a user do something like this:
	
	APSSimpleUserService userService ...
	...
	User user = userService.getUser(userId);
	if (user == null) {
		throw new AuthException("Bad login!");
	}
	if (!userService.authenticateUser(user, password, APSSimpleUserService.AUTH_METHOD_PASSWORD)) {
		throw new AuthException("Bad login!");
	}
	...
	if (user.isAuthenticated() && user.hasRole("apsadmin")) {
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

Please note that the above picture is just an example. The data source name  _APSSimpleUserServiceDS_ in this example should be configured in the _persistence/dsRefs_ config. The service will be looking up the entry with that name! The rest of the datasource entry depends on your database and where it is running. Also note that the "(default)" after the field names in the above picture are the name of the currently selected configuration environment. This configuration is configuration environment specific. You can point out different database servers for different environments for example.

When the datasource is configured and saved then you can go to _"Configuration tab, Configurations/aps/adminweb"_ and enable the "requireauthentication" config. **If you do this before setting up the datasource and you have choosen to use the provided implementation of APSAuthService that uses APSSimpleUserService to login then you will be completely locked out!** 

## Troubleshooting

If you have managed to lock yourself out of /apsadminweb as described above then I suggest editing the _APSFilesystemService root_/filesystems/se.natusoft.osgi.aps.core.config.service.APSConfigServiceProvider/apsconfig-se.natusoft.aps.adminweb-1.0.properties file and changeing the following line:

	se.natusoft.aps.adminweb_1.0_requireauthentication=true
	
to _false_ instead. Then restart the server. Also se the APSFilesystemService documentation for more information. The APSConfigService is using that service to store its configurations.

## JDBC Drivers

There is a catch with OSGi and its classpath isolation. JDBC drivers for the databases that can be used for this service must be packaged into the bundle. JDBC drivers for the following databases are currently included:

* Derby 10.9.1.0
* MySQL 5.1.26

I will try to increase this list unless I can find a smarter workaround.

## APIs
