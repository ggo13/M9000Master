DELIMITER $$
use m9000$$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'faultLogic' 
AND table_name = 'dat_staging'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`dat_staging` ADD COLUMN `faultLogic` TINYINT(1)  NULL  DEFAULT false  AFTER `faultLocation`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'lineGroups' 
AND table_name = 'dat_staging'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`dat_staging`  ADD COLUMN `lineGroups` TEXT  AFTER `faultLogic`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'comments' 
AND table_name = 'dat_staging'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`dat_staging` ADD COLUMN `comments` TEXT  AFTER `lineGroups`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'fault_logic'
AND table_name = 'comtrade_details'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`comtrade_details` ADD COLUMN `fault_logic` TINYINT(1)   DEFAULT false  AFTER `file_name`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'line_groups'
AND table_name = 'comtrade_details'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`comtrade_details`  ADD COLUMN `line_groups` TEXT  AFTER `fault_logic`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'comments'
AND table_name = 'comtrade_details'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`comtrade_details` ADD COLUMN `comments` TEXT   AFTER `line_groups`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$


SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'comments' 
AND table_name = 'long_term_dat'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`long_term_dat` ADD COLUMN `comments` TEXT  AFTER `fileName`', 
'select \'Column Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT @exist:=count(*)
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'faultLogic' 
AND table_name = 'error_comtrade'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`error_comtrade`  ADD COLUMN `faultLogic` TINYINT(1)  NULL  DEFAULT false  AFTER `faultLocation` , ADD COLUMN `lineGroups` TEXT NULL  AFTER `faultLogic` , ADD COLUMN `comments` TEXT NULL  AFTER `lineGroups`, CHANGE COLUMN `data` `data` LONGTEXT NULL  ', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

ALTER TABLE `m9000`.`continuous_comtrade_data` 
CHANGE COLUMN `contDataType` `contDataType` TEXT NOT NULL $$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'faultLogic' 
AND table_name = 'error_comtrade'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`error_comtrade` ADD COLUMN `faultLogic` TINYINT(1)  NULL  DEFAULT false  AFTER `faultLocation`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'lineGroups' 
AND table_name = 'error_comtrade'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`error_comtrade`  ADD COLUMN `lineGroups` TEXT  AFTER `faultLogic`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

SELECT @exist:=count(*) 
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'comments' 
AND table_name = 'error_comtrade'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`error_comtrade` ADD COLUMN `comments` TEXT  AFTER `lineGroups`', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

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

DELIMITER ;
  



