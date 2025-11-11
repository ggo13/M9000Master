system echo "About to create mysql events..."
DELIMITER $$
DROP EVENT IF EXISTS drop_cont_partitions$$
CREATE EVENT drop_cont_partitions
ON SCHEDULE EVERY 1 day
STARTS str_to_date( date_format(now(), '%Y%m%d 0510'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call DropPartitions('m9000', 'cont', DATE(now()), 30);
END$$

DROP EVENT IF EXISTS drop_contAnalog_partitions$$
CREATE EVENT drop_contAnalog_partitions
ON SCHEDULE EVERY 1 day
STARTS str_to_date( date_format(now(), '%Y%m%d 0520'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call DropPartitions('m9000', 'contAnalog', DATE(now()), 5);
END$$

DROP EVENT IF EXISTS create_cont_partitions$$
CREATE EVENT create_cont_partitions
ON SCHEDULE EVERY 8 hour
STARTS str_to_date( date_format(now(), '%Y%m%d 0610'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call CreatePartitions('m9000', 'cont', DATE(now()), 5);
END$$

DROP EVENT IF EXISTS create_contAnalog_partitions$$
CREATE EVENT create_contAnalog_partitions
ON SCHEDULE EVERY 8 hour
STARTS str_to_date( date_format(now(), '%Y%m%d 0620'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call CreatePartitions('m9000', 'contAnalog', DATE(now()), 5);
END$$

DROP EVENT IF EXISTS remove_old_ser$$
CREATE EVENT remove_old_ser
ON SCHEDULE EVERY 24 hour
STARTS str_to_date( date_format(now(), '%Y%m%d 0430'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call m9k_remove_old_data('m9000','ser',3);
END$$

DROP EVENT IF EXISTS remove_old_dfrlog$$
CREATE EVENT remove_old_dfrlog
ON SCHEDULE EVERY 24 hour
STARTS str_to_date( date_format(now(), '%Y%m%d 0410'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call m9k_remove_old_data('m9000','dfrlog',1);
END$$

drop EVENT if exists drop_old_cont_partitions$$
drop EVENT if exists drop_old_contAnalog_partitions$$


DELIMITER ;

system echo "Done\n"
