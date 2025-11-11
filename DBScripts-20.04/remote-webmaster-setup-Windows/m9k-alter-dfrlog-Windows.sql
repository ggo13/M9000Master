DELIMITER $$
use m9000$$
truncate dfrlog$$
SELECT count(*)
INTO @exist
FROM information_schema.columns
WHERE table_schema = 'm9000'
AND table_name = 'dfrlog'
and COLUMN_NAME = 'id'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`dfrlog` ADD COLUMN `id` INT(11) NOT NULL AUTO_INCREMENT FIRST, ADD PRIMARY KEY (`id`), CHANGE COLUMN `ts` `ts` BIGINT(20) NULL DEFAULT NULL,  ADD INDEX `idx_dfrId_ts` (`dfrId` ASC, `ts` ASC)',
'select \'Column and Index Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Drop the after trigger that was updating ts "
DROP TRIGGER IF EXISTS `m9000`.`dfrlog_insert` $$
USE `m9000`;

DROP EVENT IF EXISTS remove_old_dfrlog$$
CREATE EVENT remove_old_dfrlog
ON SCHEDULE EVERY 24 hour
STARTS str_to_date( date_format(now(), '%Y%m%d 0410'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call m9k_remove_old_data('m9000','dfrlog',1);
END$$
DELIMITER ;


