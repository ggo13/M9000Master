use m9000;
system echo "About to insert users..."
truncate users;
insert into users(userName, password,role) values('usi',SHA1('usi'),'admin');
insert into users(userName, password,role) values('guest',SHA1('guest'),'guest');
system echo "Done\n"