DELIMITER $$
use m9000$$

SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'ParentZoneId' 
AND table_name = 'station_details'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`station_details`  ADD COLUMN `ParentZoneId` TEXT NULL DEFAULT (9999) AFTER `name`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'enableConfigChangeEmail' 
AND table_name = 'emailReportsSettings'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`emailReportsSettings`  ADD COLUMN `enableConfigChangeEmail` TEXT NULL DEFAULT (0) AFTER `masterNotificationListener`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Done\n"

delimiter ;