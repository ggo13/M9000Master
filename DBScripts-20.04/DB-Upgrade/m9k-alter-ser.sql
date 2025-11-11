DELIMITER $$
use m9000$$
system echo "Creating a new ser table like ser"
drop table if exists ser_new$$
create table ser_new like ser$$
system echo "Adding column and index to the new SER table"
SELECT count(*)
INTO @exist
FROM information_schema.columns
WHERE table_schema = 'm9000'
AND table_name = 'ser'
and COLUMN_NAME = 'ser_date'$$

set @query = IF(@exist <= 0, 'ALTER TABLE `m9000`.`ser_new` ENGINE = InnoDB, ADD COLUMN `ser_date` DATETIME NULL AFTER `updated`, DROP INDEX `idx_ts_eventnum`, ADD INDEX `idx_ts_eventnum` (`ts` ASC, `eventNum` ASC), DROP INDEX `idx_updated`, ADD INDEX `idx_updated` (`updated` ASC), DROP INDEX `idx_eventnum`, ADD INDEX `idx_eventnum_name` (`eventNum` ASC, `name` ASC), DROP INDEX `idx_ts`, ADD INDEX `idx_ts` (`ts` ASC) ,ADD INDEX idx_ser_date (ser_date ASC);',
'select \'Column and Index Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$
system echo "New columns and index added"

system echo "Creating the required triggers in the new table"
DROP TRIGGER IF EXISTS m9000.update_ser$$
USE `m9000`$$
CREATE DEFINER=`dfr`@`%` TRIGGER m9000.update_ser
BEFORE INSERT ON m9000.ser_new
FOR EACH ROW
BEGIN
  set NEW.eventNum = (select (NEW.eventNum+digital_offset) from dfr_details where NEW.dfrId = dfrId);
  set NEW.ser_date = date(from_unixtime(NEW.ts div 1000000));
END$$

system echo "Creating triggers done."

system echo "Copying the data from old SER table to new SER table"
insert into ser_new(`id`, `dfrId`, `eventNum`, `phase`, `name`, `ts`, `normal`, `state`, `locked`, `ser_date`, `updated` ) SELECT `id`, `dfrId`, `eventNum`, `phase`, `name`, `ts`, `normal`, `state`, `locked`, date(from_unixtime(ts div 1000000)),`updated` FROM `m9000`.`ser`$$
system echo "Successfully copied"

system echo "Dropping the old ser table"
drop table ser$$
system echo "Old SER table dropped."
system echo "Renaming the new ser table to actual ser table"
rename table ser_new to ser$$
system echo "Rename successful"

system echo "SER table setup successful"

system echo "Create new events that runs to delete daily to remove old data"
DROP EVENT IF EXISTS remove_old_ser$$
CREATE EVENT remove_old_ser
ON SCHEDULE EVERY 24 hour
STARTS str_to_date( date_format(now(), '%Y%m%d 0430'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call m9k_remove_old_data('m9000','ser',3);
END$$

system echo "Events created successfully"
DELIMITER ;


