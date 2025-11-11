DELIMITER $$
system echo "About to alter cont table to add new columns and indices"
SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'description' 
AND table_name = 'cont'$$

set @query = IF(@exist <= 0, 'DROP TABLE if exists new_cont; CREATE TABLE new_cont like cont; DROP TABLE if exists cont_backup; RENAME TABLE cont TO cont_backup, new_cont TO cont;ALTER TABLE `m9000`.`cont` ADD COLUMN `description` VARCHAR(64) NULL  AFTER `name` , ADD COLUMN `measurementType` VARCHAR(64) NULL  AFTER `description` 

, ADD INDEX `idx_units` (`units` ASC), ADD INDEX `idx_measurement_type` (`measurementType` ASC) ', 
'select \'Columns Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Cont updates Done"
DELIMITER ;