delimiter $$
USE m9000 $$

# create dat_updates table for Comtrade record updates to be pushed to Master

CREATE TABLE IF NOT EXISTS dat_updates 
(
  faultId 	INT(11)	NOT NULL,
  updated 	DATETIME,
  PRIMARY KEY (faultId)
) ENGINE=INNODB $$

# create dat_updates table for Comtrade record updates to be pushed to Master

CREATE TABLE IF NOT EXISTS error_dat_updates 
(
  faultId 	INT(11)	NOT NULL,
  updated 	DATETIME,
  PRIMARY KEY (faultId)
) ENGINE=INNODB $$


# Trigger on dat_staging to update the dat_updates table to know about the recently created faults
DROP TRIGGER IF EXISTS dat_changes$$
CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.dat_changes
AFTER INSERT ON m9000.dat_staging
FOR EACH ROW
BEGIN
  INSERT INTO dat_updates (faultId, updated)
  VALUES (NEW.faultId, NOW());
END 
$$

# create ser_updates table for ser updates to be pushed to Master

CREATE TABLE IF NOT EXISTS ser_updates 
(
  id 		int(11)		NOT NULL,
  updated 	datetime,
  PRIMARY KEY (id)
) ENGINE=INNODB $$


# Trigger on ser to update the ser_updates table to know about the recently created ser
DROP TRIGGER IF EXISTS new_ser$$

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

DROP TABLE IF EXISTS ltr_updates$$
CREATE TABLE ltr_updates (
  id int(11) NOT NULL,
  updated datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=INNODB $$

DROP TRIGGER IF EXISTS ltr_dat_changes$$

CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.ltr_dat_changes
AFTER INSERT ON m9000.long_term_dat
FOR EACH ROW
BEGIN
  INSERT INTO ltr_updates (id, updated)
  VALUES (NEW.id, NOW());
END
$$

# create alarms_log_updates table for alarms updates to be pushed to Master

CREATE TABLE IF NOT EXISTS alarms_log_updates 
(
  id 		int(11)		NOT NULL,
  updated 	datetime,
  PRIMARY KEY (id)
) ENGINE=INNODB $$


# Trigger on alarms to update the alarms_updates table to know about the recently created ser
DROP TRIGGER IF EXISTS new_alarms_log$$

CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.new_alarms_log
AFTER INSERT ON m9000.alarms_log
FOR EACH ROW
BEGIN
  INSERT INTO alarms_log_updates (id, updated)
  VALUES (NEW.idalarms_log, NOW());
END
$$

# create reports_updates table for reports updates to be pushed to Master

CREATE TABLE IF NOT EXISTS reports_updates 
(
  id 		int(11)		NOT NULL,
  updated 	datetime,
  PRIMARY KEY (id)
) ENGINE=INNODB $$


# Trigger on ser to update the ser_updates table to know about the recently created ser
DROP TRIGGER IF EXISTS new_reports$$

CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.new_reports
AFTER INSERT ON m9000.reports
FOR EACH ROW
BEGIN
  INSERT INTO reports_updates (id, updated)
  VALUES (NEW.idreports, NOW());
END$$

CREATE TABLE IF NOT EXISTS continuous_comtrade_data_updates 
(
  id 		int(11)		NOT NULL,
  updated 	datetime,
  PRIMARY KEY (id)
) ENGINE=INNODB $$

DROP TRIGGER IF EXISTS new_continuous_comtrade_data$$
CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.new_continuous_comtrade_data
AFTER INSERT ON m9000.continuous_comtrade_data
FOR EACH ROW
BEGIN
  INSERT INTO continuous_comtrade_data_updates (id, updated)
  VALUES (NEW.id, NOW());
END
$$

CREATE TABLE IF NOT EXISTS m9000.comtrade_details_updates 
(   
	faultId 	INT(11)	NOT NULL,   
	updated 	DATETIME,   
	PRIMARY KEY (faultId) 
) ENGINE=INNODB $$

DROP TRIGGER IF EXISTS comtrade_details_changes$$
CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.comtrade_details_changes
AFTER INSERT ON m9000.comtrade_details
FOR EACH ROW
BEGIN 
	INSERT INTO comtrade_details_updates (faultId, updated) VALUES (NEW.fault_id, NOW());
END
$$


delimiter ;