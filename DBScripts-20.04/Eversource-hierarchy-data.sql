-- ALTER TABLE `m9000`.`station_details` 
-- ADD COLUMN `ParentZoneId` INT NULL DEFAULT 9999 AFTER `name`;

-- ALTER TABLE `m9000`.`emailReportsSettings` 
-- ADD COLUMN `enableConfigChangeEmail` TINYINT(1) NULL DEFAULT '0' AFTER `masterNotificationListener`;

INSERT INTO hierarchy(name,parent_id) VALUES('Eversource Energy',NULL);
INSERT INTO hierarchy(name,parent_id) VALUES('Massachusetts',1);
INSERT INTO hierarchy(name,parent_id) VALUES('Connecticut',1);
INSERT INTO hierarchy(name,parent_id) VALUES('New Hampshire',1);
INSERT INTO hierarchy(name,parent_id) VALUES('Other',1);
INSERT INTO hierarchy(name,parent_id) VALUES('Western MA',2);
INSERT INTO hierarchy(name,parent_id) VALUES('Eastern MA',2);
INSERT INTO hierarchy(name,parent_id) VALUES('Connecticut',3);
INSERT INTO hierarchy(name,parent_id) VALUES('New Hampshire',4);
