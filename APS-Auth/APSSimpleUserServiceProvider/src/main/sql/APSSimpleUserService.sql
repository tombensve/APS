/*
 * This defines the tables needed by APSSimpleUserService.
 */

/* ---- This part is required! ---- */

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

