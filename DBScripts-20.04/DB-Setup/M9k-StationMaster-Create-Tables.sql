# Create Database in case not created
delimiter $$
CREATE DATABASE IF NOT EXISTS m9000 $$
USE m9000 $$
system echo "About to Create tables and triggers..."
# Create dfr_details records table

CREATE TABLE IF NOT EXISTS dfr_details (
  dfrid smallint(5) unsigned NOT NULL,
  ipaddress char(16) NOT NULL,
  analog_count int(11) NOT NULL DEFAULT '0',
  digital_count int(11) NOT NULL DEFAULT '0',
  analog_offset int(11) NOT NULL DEFAULT '0',
  digital_offset int(11) DEFAULT '0',
  PRIMARY KEY (dfrid)
) ENGINE=INNODB $$

CREATE TABLE IF NOT EXISTS users (
  idusers int(11) NOT NULL AUTO_INCREMENT,
  userName varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  role varchar(45) NOT NULL,
  created timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated datetime DEFAULT NULL,
  PRIMARY KEY (idusers),
  # uncomment for 12.04
  # UNIQUE KEY userName_UNIQUE (userName)
  # uncomment for 20.04
  UNIQUE KEY userName_UNIQUE (userName(100))
  
) ENGINE=INNODB $$

# drop trigger if already exists
DROP TRIGGER IF EXISTS users_Insert_updateTime $$

CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.users_Insert_updateTime
BEFORE INSERT ON m9000.users
FOR EACH ROW
BEGIN
        -- Set the creation date
    SET new.updated = now();

END
$$

# drop trigger if already exists
DROP TRIGGER IF EXISTS users_update_updateTime $$

CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.users_update_updateTime
BEFORE UPDATE ON m9000.users
FOR EACH ROW
BEGIN
        -- Set the creation date
    SET new.updated = now();

END
$$
CREATE TABLE IF NOT EXISTS alarms_log (
  idalarms_log int(11) NOT NULL AUTO_INCREMENT,
  led_name varchar(45) DEFAULT NULL,
  led_status varchar(45) DEFAULT NULL,
  relays varchar(45) DEFAULT NULL,
  description text,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idalarms_log)
) ENGINE=INNODB DEFAULT CHARSET=latin1$$

# Crea table if does not exists
CREATE TABLE IF NOT EXISTS station_details (
	stationId 		int	 	NOT NULL,			-- Id
	name 			varchar(64) 	NULL,			-- Name
	ParentZoneId TEXT NULL DEFAULT (9999),
	recordingDevId 		varchar(255)	NULL DEFAULT 'USI_M9000',	-- Value to display in comtrade cfg file
  	analogs_count smallint(11) NULL DEFAULT '0',
  	digitals_count smallint(11) NULL DEFAULT '0',
	preFaultTime 		smallint(6) 	NULL,			-- Global  prefault time
	postFaultTime 		smallint(6) 	NULL,			-- Global  postfault time
	ltrPreFaultTime 	smallint(6) NULL DEFAULT '0',
  	ltrPostFaultTime	smallint(6) NULL DEFAULT '0',	
	lineFreq 		double 		 NULL DEFAULT '60',		-- Line Frequency
	sampleRate 		double 		 NULL,			-- Sample rate
	longTermSampleRate 	double 		 NULL DEFAULT '1200',	-- Sample rate for long term records
	configXml 		longtext 	 NULL,			-- The complete XML configuration text
	status 			varchar(15) 	 NULL DEFAULT 'INCOMPLETE',
	created timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	updated timestamp NULL DEFAULT NULL,
	PRIMARY KEY (stationId),
	UNIQUE KEY stationId (stationId)
)ENGINE=INNODB   $$

CREATE TABLE IF NOT EXISTS comtrade_details 
(
	fault_id 	int(11) 			NOT NULL,	-- Unique fault id for each faults
	station_id 	int(11) 			NOT NULL,	-- Station Id from which faults generated
	time_stamp 	bigint(20) unsigned DEFAULT 	NULL,		-- Time at which fault record created
	events 		text				NULL,		-- List of events in the fault
	length 		double unsigned DEFAULT 	NULL,		-- Duration of the fault
	pre_fault 	double DEFAULT 			NULL,		-- Pre fault time
	post_fault 	double DEFAULT 			NULL,		-- Post fault time
  	fault_location longtext,
	file_name 	varchar(255) DEFAULT 		NULL,		-- Comtrade file name created
	fault_logic	TINYINT(1),
	line_groups	TEXT,
	comments	TEXT,

	updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (fault_id,station_id)
)ENGINE=INNODB   $$

# Create staging table for dat
CREATE TABLE IF NOT EXISTS dat_staging (
  faultId 	int(10) 		NOT NULL AUTO_INCREMENT,
  stationId	int(11) 		NOT NULL,
  type 		char(6) 		NOT NULL,
  tsPrefault 	bigint(20) unsigned 	NOT NULL,
  tsTrigger 	bigint(20) unsigned 	NOT NULL,
  lineFreq 	double 			NOT NULL,
  sampleRate 	double 			NOT NULL,
  sampleCnt 	bigint(20) unsigned 	NOT NULL,
  analogs 	longtext 			NOT NULL,
  events 	longtext 			NOT NULL,
  data 		longtext,
  dataBlob longblob,
  faultLocation longtext,
  faultLogic	TINYINT(1),
  lineGroups	TEXT,
  comments	TEXT,
  updated 	timestamp NULL DEFAULT CURRENT_TIMESTAMP,  
  PRIMARY KEY (faultId,stationId)
  # KEY faultId_UNIQUE (faultId,stationId,type,tsPrefault,tsTrigger,lineFreq,sampleRate,sampleCnt,analogs(100),events(100),data(100),dataBlob(100))
)ENGINE=INNODB  $$


# create dat table for Comtrade record
CREATE TABLE IF NOT EXISTS dat
(
	id 		INT(11) 		NOT NULL AUTO_INCREMENT, -- Unique identifier
	dfrId		SMALLINT UNSIGNED	NOT NULL,	-- Number assigned to the DFR
	type		CHAR(6)	DEFAULT "ASCII"	NOT NULL,	-- Either ASCII or BINARY. Determines how to parse 'data' field.
	tsPrefault	BIGINT UNSIGNED		NOT NULL,	-- Timestamp of first sample. Number of microSeconds since Unix epoch
	tsTrigger	BIGINT UNSIGNED		NOT NULL,	-- Timestamp of Trigger. Number of microSeconds since Unix epoch
	lineFreq	DOUBLE	DEFAULT '60'	NOT NULL,	-- Electrical grid line frequency, ie: 50 or 60Hz
	sampleRate	DOUBLE			NOT NULL,	-- Number of samples per second
	sampleCnt	BIGINT UNSIGNED		NOT NULL,	-- Total number of samples in 'data' field.
	analogs		TEXT			NOT NULL,	-- List of Analogs in 'data' field.
	events		TEXT			NOT NULL,	-- List of Events in 'data' field.
	data		LONGTEXT			,	-- AllASCII data samples combined together.
	dataBlob 	LONGBLOB			,	-- ALL Binary data samoles
  	signature 	int(11) 		NOT NULL,	-- Hand shake variable
	updated 	timestamp NULL DEFAULT CURRENT_TIMESTAMP,  
	PRIMARY KEY (id),
	KEY idx_signature (signature)
)ENGINE=INNODB  $$

# create ser table for sequence of events recorder
CREATE TABLE IF NOT EXISTS ser
(
	id 		INT(11) NOT NULL AUTO_INCREMENT,
	dfrId		SMALLINT UNSIGNED	NOT NULL,	-- Number assigned to the DFR
	eventNum	INT UNSIGNED		NOT NULL,
	phase		CHAR(4)			NOT NULL,	-- A, B, C, or N.
	name		VARCHAR(64)		NOT NULL,	-- Name of this Event
	ts			bigint(20) unsigned NOT NULL,
	normal		BOOL			NOT NULL,
	state		BOOL			NOT NULL,
	locked		BOOL			NOT NULL,
	updated 	timestamp NULL DEFAULT CURRENT_TIMESTAMP,  
	ser_date datetime DEFAULT NULL,
    PRIMARY KEY (id),
  KEY idx_ts_eventnum (ts,eventNum),
  KEY idx_updated (updated),
  KEY idx_eventnum_name (eventNum,name),
  KEY idx_ts (ts),
  KEY idx_ser_date (ser_date)
)ENGINE=INNODB  $$


DROP TRIGGER IF EXISTS update_ser$$
CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.update_ser
BEFORE INSERT ON m9000.ser
FOR EACH ROW
BEGIN
  set NEW.eventNum = (select (NEW.eventNum+digital_offset) from dfr_details where NEW.dfrId = dfrId);
  set NEW.ser_date = date(from_unixtime(NEW.ts div 1000000));
END$$


# create cal table for Calibration
CREATE TABLE IF NOT EXISTS cal (
  idCal int(11) NOT NULL AUTO_INCREMENT,
  dfrId smallint(5) unsigned NOT NULL,
  ch smallint(5) unsigned NOT NULL,
  id int(11) NOT NULL,
  ts bigint(20) unsigned NOT NULL,
  gx1 double NOT NULL,
  gx3 double NOT NULL,
  gx6 double NOT NULL,
  gx8 double NOT NULL,
  ox1 double NOT NULL,
  ox3 double NOT NULL,
  ox6 double NOT NULL,
  ox8 double NOT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idCal)
) ENGINE=INNODB $$

CREATE TABLE IF NOT EXISTS reports (
  idreports int(11) NOT NULL AUTO_INCREMENT,
  report_action varchar(45) NOT NULL,
  report_time timestamp NULL DEFAULT NULL,
  report longtext,
  status tinytext,
  status_msg text,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idreports)
) ENGINE=INNODB $$

CREATE TABLE IF NOT EXISTS reports_health (
  reportsId int(11) NOT NULL AUTO_INCREMENT,
  stationId smallint(5) unsigned NOT NULL,
  report_type varchar(45) NOT NULL,
  report_time timestamp NULL DEFAULT NULL,
  report longtext,
  report_file_name varchar(255) DEFAULT 		NULL, 
  status tinytext,
  status_msg text,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (reportsId)
) ENGINE=INNODB $$

CREATE TABLE IF NOT EXISTS calApplied (
  idcalApplied int(11) NOT NULL AUTO_INCREMENT,
  dfrId smallint(5) unsigned NOT NULL,
  id int(11) DEFAULT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idcalApplied)
) ENGINE=INNODB$$

CREATE TABLE IF NOT EXISTS extCal (
  idextCal int(11) NOT NULL AUTO_INCREMENT,
  dfrId int(11) DEFAULT NULL,
  ch int(11) DEFAULT NULL,
  gain double DEFAULT NULL,
  offset double DEFAULT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idextCal)
) ENGINE=InnoDB $$

CREATE TABLE IF NOT EXISTS cal_report (
  idcal_report int(11) NOT NULL AUTO_INCREMENT,
  cal_action varchar(45) NOT NULL,
  cal_time timestamp NULL DEFAULT NULL,
  report longtext,
  status tinytext,
  status_msg text,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idcal_report)
) ENGINE=INNODB $$


# create error_comtrade table for storing the errored comtrade records
CREATE TABLE IF NOT EXISTS error_comtrade (
  id  int(11) NOT NULL AUTO_INCREMENT,
  faultId int(10) NOT NULL,
  stationId int(11) NOT NULL,
  type char(6) NOT NULL DEFAULT 'ASCII',
  tsPrefault bigint(20) unsigned NOT NULL,
  tsTrigger bigint(20) unsigned NOT NULL,
  lineFreq double NOT NULL DEFAULT '60',
  sampleRate double NOT NULL,
  sampleCnt bigint(20) unsigned NOT NULL,
  analogs text NOT NULL,
  events text NOT NULL,
  data longtext NOT NULL,
  dataBlob longblob,
  faultLocation longtext,
  faultLogic tinyint(1) DEFAULT NULL,
  lineGroups text,
  comments text,
  updated timestamp NULL ,
PRIMARY KEY ( id )
) ENGINE=INNODB $$


# Logs for the DFRs
CREATE TABLE IF NOT EXISTS dfrlog
(
	id int(11) NOT NULL AUTO_INCREMENT,
    dfrId                      SMALLINT UNSIGNED    NOT NULL,          -- Number assigned to the DFR
    ts                                            BIGINT                                                           NULL,    -- Timestamp. Number of Seconds since Unix epoch
    severity                      SMALLINT UNSIGNED    NOT NULL,          -- Level of message: Trace, Debug, Info, Notice, Warning, Error, Critical, Fatal
    source tinytext,  -- Source of the log file
    msg                                        TEXT                                                      NOT NULL,           -- Text body of log message.
    updated timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
  	KEY idx_dfrId_ts (dfrId,ts)
)ENGINE=INNODB $$


# Create trigger on dfrLog table to update the current time to datetime field
DROP TRIGGER IF EXISTS dfrlog_insert$$

CREATE DEFINER=`dfr`@`%` TRIGGER m9000.dfrlog_insert
BEFORE INSERT ON m9000.dfrlog
FOR EACH ROW
BEGIN
	IF NEW.`ts` IS NULL OR NEW.`ts` <= 0 THEN
		SET NEW.`updated` = NOW();
	ELSE
		SET NEW.`updated` = FROM_UNIXTIME(NEW.`ts`/1000000);
	END IF;
END$$

# Create Triggers table for start and stop times
DROP TABLE IF EXISTS triggers$$


CREATE TABLE triggers (
  idtriggers int(11) NOT NULL AUTO_INCREMENT,
  dfrId smallint(5) unsigned DEFAULT '0',
  start bigint(20) unsigned DEFAULT '0',
  stop bigint(20) unsigned DEFAULT '0',
  updated datetime DEFAULT NULL,
  PRIMARY KEY (idtriggers)
) ENGINE=INNODB$$

DROP TRIGGER IF EXISTS triggers_updateTime$$


CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.triggers_updateTime
BEFORE INSERT ON m9000.triggers
FOR EACH ROW
BEGIN
        -- Set the creation date
    SET new.updated = now();

END
$$

DROP TRIGGER IF EXISTS new_triggers$$

CREATE
DEFINER='dfr'@'localhost'
TRIGGER m9000.new_triggers
AFTER INSERT ON m9000.triggers
FOR EACH ROW
BEGIN
  INSERT INTO triggers_updates (idtriggers, dfrId, start, stop, updated)
  VALUES (NEW.idtriggers, NEW.dfrId, NEW.start, NEW.stop, NOW());
END
$$

# Creat trigger_updates table to keep track of the latest updates

CREATE TABLE IF NOT EXISTS triggers_updates (
  idtriggers int(11) NOT NULL AUTO_INCREMENT,
  dfrId smallint(5) unsigned DEFAULT '0',
  start bigint(20) unsigned DEFAULT '0',
  stop bigint(20) unsigned DEFAULT '0',
  updated datetime DEFAULT NULL,
  PRIMARY KEY (idtriggers)
) ENGINE=INNODB $$


# LTR table creation
CREATE TABLE IF NOT EXISTS long_term_dat (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  stationId int(10) unsigned NOT NULL,
  faultId int(11) NOT NULL,
  ltType varchar(45) DEFAULT NULL,
  tsTrigger bigint(20) NOT NULL,
  sampleRate int(11) NOT NULL,
  length double unsigned NOT NULL,
  preFault int(11) NOT NULL,
  postFault int(11) NOT NULL,
  fileName tinytext NOT NULL,
  comments	TEXT,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_faultId (faultId)
) ENGINE=INNODB  $$

# Create dfr_details records table
CREATE TABLE IF NOT EXISTS dfr_details (
  dfrid smallint(5) unsigned NOT NULL,
  ipaddress varchar(100) NOT NULL,
  analog_count int(11) NOT NULL DEFAULT '0',
  digital_count int(11) NOT NULL DEFAULT '0',
  analog_offset int(11) NOT NULL DEFAULT '0',
  digital_offset int(11) DEFAULT '0',
  PRIMARY KEY (dfrid)
) ENGINE=INNODB $$

CREATE TABLE IF NOT EXISTS error_dat  (
   id  int(11) NOT NULL,
   dfrId  smallint(5) unsigned NOT NULL,
   type  char(6) NOT NULL,
   tsPrefault  bigint(20) unsigned NOT NULL,
   tsTrigger  bigint(20) unsigned NOT NULL,
   lineFreq  double NOT NULL,
   sampleRate  double NOT NULL,
   sampleCnt  bigint(20) unsigned NOT NULL,
   analogs  text NOT NULL,
   events  text NOT NULL,
   data  longtext,
   dataBlob  longblob,
   signature  int(11) NOT NULL,
   updated	timestamp NULL DEFAULT CURRENT_TIMESTAMP,  
  PRIMARY KEY ( id )
) ENGINE=INNODB $$



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
  -- uncomment for Ubuntu 12.04 Comment for Ubuntu 20.04
  -- updated datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  -- uncomment for Ubuntu 20.04 Comment for Ubuntu 12.04
  updated TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id,tsLast),
  KEY idx_expId_tsLast (expId,tsLast,name,sampleCnt,sampleRate),
  KEY idx_tsLast (tsLast),
  KEY idx_recId (recId),
  KEY idx_updated (updated)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED 
/*!50500 PARTITION BY RANGE  COLUMNS(tsLast)
(PARTITION p20130301 VALUES LESS THAN (unix_timestamp('2013-03-01')*1000000) ENGINE = InnoDB,
 PARTITION p20130302 VALUES LESS THAN (unix_timestamp('2013-03-02')*1000000) ENGINE = InnoDB
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

CREATE TABLE IF NOT EXISTS cont (
  id int(11) NOT NULL AUTO_INCREMENT,
  recId smallint(5) unsigned NOT NULL,
  expId smallint(5) unsigned NOT NULL,
  name varchar(64) NOT NULL,
  description varchar(64) DEFAULT 'default',
  measurementType varchar(64) NOT NULL,
  phase char(4) NOT NULL,
  units char(80) NOT NULL,
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
(PARTITION p20130301 VALUES LESS THAN (unix_timestamp('2013-03-01')*1000000) ENGINE = InnoDB,
 PARTITION p20130302 VALUES LESS THAN (unix_timestamp('2013-03-02')*1000000) ENGINE = InnoDB
 ) */$$

# Continuous comtrade details table creation

CREATE TABLE IF NOT EXISTS continuous_comtrade_data (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  contDataType TEXT NOT NULL,
  startDateTime DATETIME  NOT NULL,
  endDateTime DATETIME  NOT NULL,
  fileName tinytext NOT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=INNODB  $$

# User and EMail configuration tables
CREATE TABLE IF NOT EXISTS emailReportsSettings (
  id enum('1') NOT NULL,
  enableDailyStatusEmails tinyint(1) DEFAULT '0',
  dailyStatusRepeatInterval smallint(5) DEFAULT '1440',
  dailyStatusReportTime char(5) DEFAULT '00:00',
  dailyStatusReportTitle varchar(100) DEFAULT 'DME Daily Health Report',
  stationSpecificStatusReportTitle varchar(100) DEFAULT 'DME Alarms Report',
  enableSerEmail tinyint(1) DEFAULT '0',
  serRunFrequency smallint(5) DEFAULT '300',
  serEmailFileType char(10) DEFAULT NULL,
  serEmailAttachementSizeLimit smallint(3) DEFAULT '2',
  enableFaultEmail tinyint(1) DEFAULT '0',
  enableFaultsBooleanLogicFilter tinyint(1) DEFAULT '0',
  enableFaultsWithAttachment tinyint(1) DEFAULT '0',
  faultEmailAttachementSizeLimit smallint(5) DEFAULT '2',
  faultsEmailDailyLimit smallint(6) DEFAULT '10',
  masterHealthStatusPoll tinyint(1) DEFAULT '0',
  masterHealthStatusPollingFrequency smallint(5) DEFAULT '30',
  masterNotificationListener tinyint(1) DEFAULT '0',
  enableConfigChangeEmail TINYINT(1) NULL DEFAULT '0',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1  $$

CREATE TABLE IF NOT EXISTS emailSettings (
  id enum('1') NOT NULL,
  enableEmail tinyint(1) DEFAULT NULL,
  emailServerHost varchar(100) DEFAULT NULL,
  emailSmtpPort smallint(5) unsigned DEFAULT NULL,
  fromEmail varchar(320) DEFAULT NULL,
  emailServerPassword varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1  $$

CREATE TABLE IF NOT EXISTS emailsSubscriptionList (
  id int(11) NOT NULL AUTO_INCREMENT,
  emailAddress varchar(320) DEFAULT NULL,
  firstName varchar(45) DEFAULT NULL,
  lastName varchar(45) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY emailAddress_UNIQUE (emailAddress)
) ENGINE=InnoDB DEFAULT CHARSET=latin1  $$

CREATE TABLE IF NOT EXISTS hierarchy (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  parent_id int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (parent_id) REFERENCES hierarchy (id) 
    ON DELETE CASCADE ON UPDATE CASCADE
)$$

# Views Creation
# Create  views for cont and cont_anolog tables
DROP VIEW IF EXISTS cont_analog_view $$
CREATE ALGORITHM=MERGE DEFINER='dfr'@'localhost' SQL SECURITY DEFINER VIEW cont_analog_view AS SELECT contAnalog.expId AS expId,contAnalog.name AS name,contAnalog.phase AS phase,contAnalog.units AS units,contAnalog.tsLast AS tsLast,contAnalog.sampleRate AS sampleRate,contAnalog.sampleCnt AS sampleCnt,contAnalog.scale AS scale,contAnalog.offset AS offset,contAnalog.data AS data,FROM_UNIXTIME(((contAnalog.tsLast / 1000000) - (contAnalog.sampleCnt / contAnalog.sampleRate))) AS firstSampleTime,FROM_UNIXTIME((contAnalog.tsLast DIV 1000000)) AS LastSampleTime FROM contAnalog $$

DROP VIEW IF EXISTS cont_view $$
CREATE ALGORITHM=UNDEFINED DEFINER='dfr'@'localhost' SQL SECURITY DEFINER VIEW cont_view AS SELECT cont.expId AS expId,cont.name AS name,cont.phase AS phase,cont.units AS units,cont.tsLast AS tsLast,cont.sampleRate AS sampleRate,cont.sampleCnt AS sampleCnt,cont.type AS type,cont.data AS data,FROM_UNIXTIME(((cont.tsLast / 1000000) - (cont.sampleCnt / cont.sampleRate))) AS firstSampleTime,FROM_UNIXTIME((cont.tsLast DIV 1000000)) AS LastSampleTime FROM cont $$

system echo "Done\n"

delimiter ;
