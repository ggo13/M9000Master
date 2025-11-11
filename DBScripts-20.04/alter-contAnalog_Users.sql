DELIMITER $$
USE m9000 $$
system echo "About to modify contAnalog table "
SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = 'm9000'
and COLUMN_NAME = 'updated' 
and COLUMN_TYPE='timestamp'
AND table_name = 'contAnalog'$$

system echo "About to clear data from contAnalog table to update column type "
set @query = IF(@exist <= 0, 'truncate m9000.contAnalog;', 
'select \'contAnalog Table already up-to-date \' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "About to alter contAnalog table to update column type "
set @query = IF(@exist <= 0, 'ALTER TABLE m9000.contAnalog CHANGE COLUMN updated updated TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;', 
'select \'contAnalog Table already up-to-date\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "About to create future partitions "
set @query = IF(@exist <= 0, 'call CreatePartitions(\'m9000\',\'contAnalog\',date(now()),5);', 
'select \'contAnalog Table already up-to-date\' Status')$$

prepare newstmt from @query$$

EXECUTE newstmt$$
DEALLOCATE PREPARE newstmt$$

system echo "Cont updates Done"

system echo "About to alter Users table to update index"
SELECT count(*)
INTO @exist
FROM information_schema.STATISTICS 
WHERE table_schema = 'm9000'
AND table_name = 'users'
AND index_name = 'userName_UNIQUE'
AND sub_part = 100$$

set @query = IF(@exist <= 0, 'ALTER TABLE m9000.users DROP INDEX userName_UNIQUE ,ADD UNIQUE INDEX userName_UNIQUE (userName(100) ASC);', 
'select \'Index Already Exists\' Status')$$

prepare stmt from @query$$

EXECUTE stmt$$
DEALLOCATE PREPARE stmt$$

system echo "Users index updates Done"
DELIMITER ;