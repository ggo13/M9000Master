DELIMITER $$
use m9000$$
system echo "About to alter dfrlog to include new column and index"
system echo "Clearing the logs"
truncate dfrlog$$
system echo "Adding column and index to the dfrlog table"
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

SELECT count(*)
INTO @exist
FROM information_schema.columns
WHERE table_schema = 'm9000'
AND table_name = 'dfrlog'
and COLUMN_NAME = 'ts'
and DATA_TYPE = 'BIGINT'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`dfrlog` CHANGE COLUMN `ts` `ts` BIGINT(20) NULL DEFAULT NULL',
'select \'Column and Index Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Drop the after trigger that was updating ts "
DROP TRIGGER IF EXISTS `m9000`.`dfrlog_insert` $$
USE `m9000`;

DELIMITER $$

DROP TRIGGER IF EXISTS m9000.dfrlog_insert$$
CREATE DEFINER=`dfr`@`%` TRIGGER m9000.dfrlog_insert
BEFORE INSERT ON m9000.dfrlog
FOR EACH ROW
BEGIN
	IF NEW.`ts` IS NULL OR NEW.`ts` <= 0 THEN
		SET NEW.`updated` = NOW();
	ELSE
		SET NEW.`updated` = FROM_UNIXTIME(NEW.`ts`/1000000);
	END IF;
END$$

system echo "Altering dfrlog successful. New columns and index added to dfrlog"
DELIMITER ;


