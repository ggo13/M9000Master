use m9000;
system echo "About to insert users..."
truncate users;
insert into users(userName, password,role) values('usi',SHA1('usi'),'admin');
insert into users(userName, password,role) values('guest',SHA1('guest'),'guest');
-- Rescue admin user created in case we are locked out of application
-- insert into users(userName, password,role) values('adminSupport911',SHA1('RescueMe'),'admin');
system echo "Done\n"