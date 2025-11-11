DELIMITER $$
system echo "About to create procedures to drop partitions on daily basis..."
DROP PROCEDURE IF EXISTS DropPartitions $$

-- Procedure to delete old partitions
-- partitions older than (today_date - days_past) will be dropped
CREATE PROCEDURE DropPartitions (dbname TEXT, tblname TEXT, today_date DATE, days_past INT)
BEGIN

DECLARE partition_count integer;
DECLARE part_name_to_drop varchar(10);
set days_past = days_past;

SELECT COUNT(*)
INTO partition_count
FROM INFORMATION_SCHEMA.PARTITIONS
WHERE TABLE_NAME=tblname
AND TABLE_SCHEMA=dbname
AND date(from_unixtime(partition_description div 1000000))< date(DATE_SUB(now(), interval days_past day));


-- pruning old partitions
WHILE (partition_count > 0)
DO

select partition_name INTO part_name_to_drop FROM INFORMATION_SCHEMA.PARTITIONS
   WHERE TABLE_NAME=tblname
   AND TABLE_SCHEMA=dbname
   AND date(from_unixtime(partition_description div 1000000)) <  date(DATE_SUB(now(), interval days_past day)) limit 1;
-- SELECT minpart;

 SET @sql := CONCAT('ALTER TABLE '
                    , tblname
                    , ' DROP PARTITION '
                    , part_name_to_drop
                    , ';');

 -- SELECT @sql;
 PREPARE stmt FROM @sql;
 EXECUTE stmt;
 DEALLOCATE PREPARE stmt;

SELECT COUNT(*)
  INTO partition_count
  FROM INFORMATION_SCHEMA.PARTITIONS
  WHERE TABLE_NAME=tblname
  AND TABLE_SCHEMA=dbname
  AND date(from_unixtime(partition_description div 1000000)) < date(DATE_SUB(now(), interval days_past day));

-- SELECT partition_count;

END WHILE;

END $$

DELIMITER ;
system echo "Done\n"