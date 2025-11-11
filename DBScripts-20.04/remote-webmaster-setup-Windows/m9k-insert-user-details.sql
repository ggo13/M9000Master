use m9000;
truncate users;
insert into users(userName, password,role) values('usi',SHA1('usi'),'admin');
insert into users(userName, password,role) values('guest',SHA1('guest'),'guest');
