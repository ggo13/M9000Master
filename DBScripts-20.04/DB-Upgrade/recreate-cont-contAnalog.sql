delimiter $$
USE m9000 $$
drop table contAnalog$$
CREATE TABLE IF NOT EXISTS contAnalog (
  id int(11) NOT NULL AUTO_INCREMENT,
  recId smallint(5) unsigned NOT NULL,
  expId smallint(5) unsigned NOT NULL,
  name varchar(64) NOT NULL,
  phase char(4) NOT NULL,
  units char(8) NOT NULL,
  tsLast bigint(20) unsigned NOT NULL,
  sampleRate int(11) NOT NULL,
  sampleCnt int(10) unsigned NOT NULL,
  scale float NOT NULL,
  offset float NOT NULL,
  data longblob NOT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id,tsLast),
  KEY idx_expId_tsLast (expId,tsLast,name,sampleCnt,sampleRate),
  KEY idx_tsLast (tsLast),
  KEY idx_recId (recId),
  KEY idx_updated (updated)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED 
/*!50500 PARTITION BY RANGE  COLUMNS(tsLast)
(PARTITION p20130301 VALUES LESS THAN (unix_timestamp('2018-03-01')*1000000) ENGINE = InnoDB,
 PARTITION p20130302 VALUES LESS THAN (unix_timestamp('2018-03-02')*1000000) ENGINE = InnoDB
 ) */$$

DROP TRIGGER IF EXISTS update_contAnalog$$
CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.update_contAnalog
BEFORE INSERT ON m9000.contAnalog
FOR EACH ROW
BEGIN

  set NEW.expId = (select (NEW.expId+analog_offset) from dfr_details where NEW.recId = dfrId), NEW.updated=now();

END
$$

drop table cont$$
CREATE TABLE IF NOT EXISTS cont (
  id int(11) NOT NULL AUTO_INCREMENT,
  recId smallint(5) unsigned NOT NULL,
  expId smallint(5) unsigned NOT NULL,
  name varchar(64) NOT NULL,
  description varchar(64) DEFAULT 'default',
  measurementType varchar(64) NOT NULL,
  phase char(4) NOT NULL,
  units char(8) NOT NULL,
  tsLast bigint(20) unsigned NOT NULL,
  sampleRate double NOT NULL,
  sampleCnt int(10) unsigned NOT NULL,
  type char(8) NOT NULL DEFAULT 'Float',
  data mediumblob NOT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id, tsLast),
  KEY idx_expId_tsLast (expId,tsLast,name,sampleCnt,sampleRate,type),
  KEY idx_tsLast (tsLast),
  KEY idx_updated (updated),
  KEY idx_units (units),
  KEY idx_measurement_type (measurementType),
  KEY idx_recId (recId)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED 
/*!50500 PARTITION BY RANGE  COLUMNS(tsLast)
(PARTITION p20130301 VALUES LESS THAN (unix_timestamp('2018-03-01')*1000000) ENGINE = InnoDB,
 PARTITION p20130302 VALUES LESS THAN (unix_timestamp('2018-03-02')*1000000) ENGINE = InnoDB
 ) */$$


delimiter ;