DELIMITER $$
system echo "About to drop procedures and events owned by root..."
USE m9000 $$
GRANT SELECT,INSERT,UPDATE,DELETE,ALTER, CREATE,DROP,TRIGGER, EXECUTE, EVENT,CREATE ROUTINE, ALTER ROUTINE,REFERENCES   ON m9000.* TO 'dfr'@'localhost';
DROP PROCEDURE IF EXISTS CreatePartitions $$
DROP PROCEDURE IF EXISTS DropPartitions $$
DROP PROCEDURE if EXISTS m9k_remove_old_data$$
DROP EVENT IF EXISTS drop_cont_partitions$$
DROP EVENT IF EXISTS drop_contAnalog_partitions$$
DROP EVENT IF EXISTS create_cont_partitions$$
DROP EVENT IF EXISTS create_contAnalog_partitions$$
DROP EVENT IF EXISTS remove_old_ser$$
DROP EVENT IF EXISTS remove_old_dfrlog$$
DROP EVENT if EXISTS drop_old_cont_partitions$$
DROP EVENT if EXISTS drop_old_contAnalog_partitions$$
DELIMITER ;

system echo "Done\n"