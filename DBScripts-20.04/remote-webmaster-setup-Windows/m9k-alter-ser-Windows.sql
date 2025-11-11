DELIMITER $$
use m9000$$

# Alter SER table to include the latest changes to handle huge data
drop table if exists ser_new$$
create table ser_new like ser$$

SELECT @exist:=count(*)
FROM information_schema.statistics 
WHERE table_schema = 'm9000'
AND table_name = 'ser'
and COLUMN_NAME = 'ser_date'$$

set @query := IF(@exist <= 0, 'ALTER TABLE `m9000`.`ser_new` ENGINE = InnoDB, ADD COLUMN `ser_date` DATETIME NULL AFTER `updated`, DROP INDEX `idx_ts_eventnum`, ADD INDEX `idx_ts_eventnum` (`ts` ASC, `eventNum` ASC), DROP INDEX `idx_updated`, ADD INDEX `idx_updated` (`updated` ASC), DROP INDEX `idx_eventnum`, ADD INDEX `idx_eventnum_name` (`eventNum` ASC, `name` ASC), DROP INDEX `idx_ts`, ADD INDEX `idx_ts` (`ts` ASC) ,ADD INDEX idx_ser_date (ser_date ASC),ADD INDEX `idx_ser_date_station_id` (`ser_date`,`stationId`),ADD INDEX `idx_eventnum_name_station_id` (`eventNum`,`name`,`stationId`);', 
'select \'Column and Index Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

insert into ser_new(`id`, `stationId`, `eventNum`, `phase`, `name`, `ts`, `normal`, `state`, `locked`, `ser_date`, `updated` ) SELECT `id`, `stationId`, `eventNum`, `phase`, `name`, `ts`, `normal`, `state`, `locked`, date(from_unixtime(ts div 1000000)), `updated` FROM `m9000`.`ser`$$

drop table ser$$
rename table ser_new to ser$$

DROP EVENT IF EXISTS remove_old_ser$$
CREATE EVENT remove_old_ser
ON SCHEDULE EVERY 24 hour
STARTS str_to_date( date_format(now(), '%Y%m%d 0430'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY
DO 
BEGIN
call m9k_remove_old_data('m9000','ser',3);
END$$

CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.new_ser
AFTER INSERT ON m9000.ser
FOR EACH ROW
BEGIN
  INSERT INTO ser_updates (id, updated)
  VALUES (NEW.id, NOW());
END
$$

DELIMITER $$
  



