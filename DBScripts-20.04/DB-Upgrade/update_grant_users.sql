CREATE database IF NOT EXISTS m9000;
system echo "\nAbout to create DB users and assigns permissions..."
DELETE FROM mysql.user where user='dfr';

FLUSH PRIVILEGES;
-- GRANT USAGE ON *.* TO 'dfr'@'localhost';
-- DROP USER 'dfr'@'localhost';
-- uncomment for Ubuntu 12.04 Comment for Ubuntu 20.04
-- CREATE USER 'dfr'@'localhost' IDENTIFIED BY 'usi';
-- GRANT SELECT,INSERT,UPDATE,DELETE,ALTER, CREATE,DROP,TRIGGER ON m9000.* TO 'dfr'@'localhost' IDENTIFIED BY 'usi'  WITH GRANT OPTION;
-- GRANT USAGE,SELECT,INSERT,UPDATE,DELETE,ALTER, CREATE,DROP,TRIGGER ON m9000.* TO 'dfr'@'%' IDENTIFIED BY 'usi' WITH GRANT OPTION ;

-- uncomment for Ubuntu 20.04 Comment for Ubuntu 12.04
DROP USER if exists 'dfr'@'localhost';
DROP USER if exists 'dfr'@'%';
CREATE USER  if not exists 'dfr'@'localhost' IDENTIFIED WITH caching_sha2_password BY 'usi';
CREATE USER  if not exists 'dfr'@'%' IDENTIFIED WITH caching_sha2_password BY 'usi';
GRANT SELECT,INSERT,UPDATE,DELETE,ALTER, CREATE,DROP,TRIGGER, EXECUTE, EVENT,CREATE ROUTINE, ALTER ROUTINE,REFERENCES   ON m9000.* TO 'dfr'@'localhost';
GRANT USAGE,SELECT,INSERT,UPDATE,DELETE,ALTER, CREATE,DROP,TRIGGER ON m9000.* TO 'dfr'@'%';
-- END of 20.04
FLUSH PRIVILEGES;
system echo "Done\n"