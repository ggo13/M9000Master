DELIMITER $$
system echo "About to create procedures to create partitions at the start for currrent and future days..."
DROP PROCEDURE IF EXISTS CreatePartitions $$

-- Enough new partitions will be made to cover until (today_date + days_future)
CREATE PROCEDURE CreatePartitions  (dbname TEXT, tblname TEXT, today_date DATE, days_future INT)
BEGIN

DECLARE partition_count integer;
DECLARE futurepart varchar(10);
DECLARE newpart_date date;
DECLARE newpart_name date;
declare i integer default 1;
DECLARE CONTINUE HANDLER FOR NOT FOUND set futurepart=null;
set days_future = days_future + 1;

WHILE (i <= days_future)
DO

   select partition_name INTO futurepart FROM INFORMATION_SCHEMA.PARTITIONS
   WHERE TABLE_NAME=tblname
   AND TABLE_SCHEMA=dbname
   AND date(from_unixtime(partition_description div 1000000)) = DATE(DATE_ADD(now(), interval i day)) limit 1;

    if futurepart is null then
        SET newpart_date := date(DATE_ADD(now(), interval i day));
        SET newpart_name := date(DATE_ADD(now(), interval i-1 day));
        SET @sql := CONCAT('ALTER TABLE '
                    , tblname
                    , ' ADD PARTITION (PARTITION p'
                    , CAST((newpart_name+0) as char(8))
                    , ' VALUES LESS THAN ('
                    , unix_timestamp(newpart_date)*1000000
                    , '));');

        -- SELECT @sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
   end if;

set i = i + 1;

END WHILE;


END $$

DELIMITER ;

system echo "Done\n"