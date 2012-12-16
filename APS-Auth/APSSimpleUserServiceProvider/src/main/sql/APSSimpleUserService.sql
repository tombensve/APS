/*
 * This defines the tables needed by APSSimpleUserService.
 */

create table role (
  id varchar(50) not null primary key,
  description varchar(200),
  master int
);

create table svcuser (
  id varchar(50) not null primary key,
  auth varchar(2000),
  user_data varchar(4000) /* You might want to adapt this size to the amount of data you will be adding to a user. */
);

create table user_role (
  user_id varchar(50) not null,
  role_id varchar(50) not null,
  primary key (user_id, role_id),
  foreign key (user_id) references svcuser (id),
  foreign key (role_id) references role (id)
);

create table role_role (
  master_role_id varchar(50) not null,
  role_id varchar(50) not null,
  primary key (master_role_id, role_id),
  foreign key (master_role_id) references role (id),
  foreign key (role_id) references role (id)
);
