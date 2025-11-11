DELIMITER $$
use m9000$$

ALTER TABLE `m9000`.`continuous_comtrade_data` 
CHANGE COLUMN `contDataType` `contDataType` TEXT NOT NULL $$

SELECT auto_increment
INTO @auto_increment_val
FROM information_schema.TABLES 
WHERE table_schema = 'm9000'
AND table_name = 'dat_staging'$$

SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'faultLogic' 
AND table_name = 'dat_staging'$$

set @query = IF(@exist <= 0, 'TRUNCATE table dat_staging', 
'select \'Columns Already Exists. Not truncating\' Status')$$
PREPARE stmt FROM @query$$
EXECUTE stmt $$

DEALLOCATE PREPARE stmt$$


SET @alter_statement = IF(@exist <= 0, concat('ALTER TABLE dat_staging AUTO_INCREMENT = ', @auto_increment_val), 
'select \'Columns Already Exists.Not resetting auto_increment\' Status')$$
PREPARE stmt FROM @alter_statement$$
EXECUTE stmt $$

DEALLOCATE PREPARE stmt$$

system echo "Adding columns to dat_staging table"


set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`dat_staging` ADD COLUMN `faultLogic` TINYINT(1)  NULL  DEFAULT (false)  AFTER `faultLocation`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'lineGroups' 
AND table_name = 'dat_staging'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`dat_staging`  ADD COLUMN `lineGroups` TEXT NULL AFTER `faultLogic`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'comments' 
AND table_name = 'dat_staging'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`dat_staging` ADD COLUMN `comments` TEXT NULL AFTER `lineGroups`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Done."
system echo "Adding columns to comtrade_details table"
SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'fault_logic'
AND table_name = 'comtrade_details'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`comtrade_details` ADD COLUMN `fault_logic` TINYINT(1)  NULL AFTER `file_name`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'line_groups'
AND table_name = 'comtrade_details'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`comtrade_details`  ADD COLUMN `line_groups` TEXT NULL AFTER `fault_logic`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'comments'
AND table_name = 'comtrade_details'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`comtrade_details` ADD COLUMN `comments` TEXT NULL AFTER `line_groups`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Done."  
system echo "Adding comments column to continuous data in long_term_dat table"
SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'comments' 
AND table_name = 'long_term_dat'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`long_term_dat` ADD COLUMN `comments` TEXT NULL  AFTER `fileName`', 
'select \'Column Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Done."
system echo "Adding offset column for external calibration"
SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'offset' 
AND table_name = 'extCal'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`extCal` ADD COLUMN `offset` DOUBLE NULL AFTER `gain` ', 
'select \'Column Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Adding columns to error_comtrade table"
SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'faultLogic' 
AND table_name = 'error_comtrade'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`error_comtrade`  ADD COLUMN `faultLogic` TINYINT(1)  NULL  AFTER `faultLocation` , ADD COLUMN `lineGroups` TEXT NULL  AFTER `faultLogic` , ADD COLUMN `comments` TEXT NULL  AFTER `lineGroups` ', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Adding index to SER table"
SELECT count(*)
INTO @exist
FROM information_schema.statistics 
WHERE table_schema = 'm9000'
AND table_name = 'ser'
AND index_name="idx_ts_eventnum"$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`ser` ENGINE = InnoDB, ADD INDEX `idx_ts_eventnum` (`ts` ASC, `eventNum` ASC), ADD INDEX `idx_updated` (`updated` ASC), ADD INDEX `idx_eventnum` (`eventNum` ASC), ADD INDEX `idx_ts` (`ts` ASC) ', 
'select \'Index Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$
DELIMITER $$

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

drop EVENT if exists drop_old_cont_partitions$$
drop EVENT if exists drop_old_contAnalog_partitions$$

drop table if exists new_cont$$
drop table if exists new_contAnalog$$
drop table if exists cont_backup$$
drop table if exists contAnalog_backup$$

system echo "Create a stored procedure to remove old data"
source create-sp-to-remove-old-data.sql$$
system echo "done"


delimiter ;
