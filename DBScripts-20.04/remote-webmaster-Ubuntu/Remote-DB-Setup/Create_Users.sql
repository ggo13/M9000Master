CREATE database IF NOT EXISTS m9000;
system echo "\nAbout to create DB users and assigns permissions..."
DELETE FROM mysql.user where user='dfr';

FLUSH PRIVILEGES;
DROP USER if exists 'dfr'@'localhost';
DROP USER if exists 'dfr'@'%';
CREATE USER  if not exists 'dfr'@'localhost' IDENTIFIED BY 'usi';
CREATE USER  if not exists 'dfr'@'%' IDENTIFIED WITH mysql_native_password BY 'usi';
GRANT SELECT,INSERT,UPDATE,DELETE,ALTER, CREATE,DROP,TRIGGER, EXECUTE, EVENT,CREATE ROUTINE, ALTER ROUTINE,REFERENCES   ON m9000.* TO 'dfr'@'localhost';
GRANT USAGE,SELECT,INSERT,UPDATE,DELETE,ALTER, CREATE,DROP,TRIGGER ON m9000.* TO 'dfr'@'%';
FLUSH PRIVILEGES;
system echo "Done\n"