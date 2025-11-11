# Create Database in case not created
delimiter $$
CREATE DATABASE IF NOT EXISTS m9000$$
USE m9000$$

# Create the table if it already exists

CREATE TABLE IF NOT EXISTS users (
  idusers int(11) NOT NULL AUTO_INCREMENT,
  userName varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  role varchar(45) NOT NULL,
  created timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated datetime DEFAULT NULL,
  PRIMARY KEY (idusers),
  UNIQUE KEY userName_UNIQUE (userName)
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

# Drop the table if it already exists

CREATE TABLE IF NOT EXISTS station_details (
	stationId 		int	 	NOT NULL,			-- Id
	name 			varchar(64) 	NOT NULL,			-- Name
	recordingDevId 		varchar(255)	NOT NULL DEFAULT 'USI_M9000',	-- Value to display in comtrade cfg file
  	analogs_count int(11) NOT NULL DEFAULT '0',
  	digitals_count int(11) NOT NULL DEFAULT '0',
	preFaultTime 		smallint(6) 	NOT NULL,			-- Global  prefault time
	postFaultTime 		smallint(6) 	NOT NULL,			-- Global  postfault time
	ltrPreFaultTime 	smallint(6) NOT NULL DEFAULT '0',
  	ltrPostFaultTime	smallint(6) NOT NULL DEFAULT '0',	
	lineFreq 		double 		NOT NULL DEFAULT '60',		-- Line Frequency
	sampleRate 		double 		NOT NULL,			-- Sample rate
	longTermSampleRate 	double 		NOT NULL DEFAULT '1200',	-- Sample rate for long term records
	configXml 		longtext 	NOT NULL,			-- The complete XML configuration text
	status 			varchar(15) 	NOT NULL DEFAULT 'INCOMPLETE',
	created timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	updated timestamp NULL DEFAULT NULL,
	PRIMARY KEY (stationId),
	UNIQUE KEY stationId (stationId)
)ENGINE=INNODB  $$

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
)ENGINE=INNODB  $$

# Create staging table for dat
CREATE TABLE IF NOT EXISTS dat_staging (
  faultId 	int(10) 		NOT NULL,
  stationId 	int 	 		NOT NULL,
  type 		char(6) 		NOT NULL DEFAULT 'ASCII',
  tsPrefault 	bigint(20) unsigned 	NOT NULL,
  tsTrigger 	bigint(20) unsigned 	NOT NULL,
  lineFreq 	double 			NOT NULL DEFAULT '60',
  sampleRate 	double 			NOT NULL,
  sampleCnt 	bigint(20) unsigned 	NOT NULL,
  analogs 	longtext 			NOT NULL,
  events 	longtext 			NOT NULL,
  data 		longtext 		,
  dataBlob 		longblob 		,
  faultLocation longtext,
  faultLogic	TINYINT(1),
  lineGroups	TEXT,
  comments	TEXT,  
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (faultId, stationId),
  # KEY faultId_UNIQUE (faultId, stationId,type,tsPrefault,tsTrigger,lineFreq,sampleRate,sampleCnt,analogs(100),events(100),data(100),dataBlob(100)),
  KEY stationid_idx (stationId)
)ENGINE=INNODB  $$

CREATE TABLE IF NOT EXISTS error_comtrade (
  faultId int(10) NOT NULL,
  stationId int(10) unsigned NOT NULL,
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
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  KEY stationid_idx (stationId)
) ENGINE=INNODB DEFAULT CHARSET=latin1$$

# create ser table for sequence of events recorder
CREATE TABLE IF NOT EXISTS ser
(
	id 		INT(11) NOT NULL AUTO_INCREMENT,
	stationId	INT 			NOT NULL,	-- Number assigned to the Substation
	eventNum	INT UNSIGNED		NOT NULL,
	phase	CHAR(4)				NOT NULL,	-- A, B, C, or N.
	name		VARCHAR(64)		NOT NULL,	-- Name of this Event
	ts		BIGINT UNSIGNED		NOT NULL,
	normal		BOOL			NOT NULL,
	state		BOOL			NOT NULL,
	locked		BOOL			NOT NULL,
	updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	ser_date datetime DEFAULT NULL,
	PRIMARY KEY (id),
	KEY idx_stationId (stationId),
  KEY idx_ts_eventnum (ts,eventNum),
  KEY idx_updated (updated),
  KEY idx_eventnum_name (eventNum,name),
  KEY idx_ts (ts),
  KEY idx_ser_date (ser_date)
)ENGINE=INNODB  $$


# create error_dat table for storing the errored fault records

CREATE TABLE IF NOT EXISTS error_dat (
  id 		INT(10) unsigned NOT NULL AUTO_INCREMENT,
  stationId	INT NOT NULL,
  faultId	INT(10) NOT NULL,
  type 		CHAR(6) NOT NULL DEFAULT 'ASCII',
  tsPrefault 	BIGINT(20) UNSIGNED NOT NULL,
  tsTrigger 	BIGINT(20) UNSIGNED NOT NULL,
  lineFreq 	DOUBLE NOT NULL DEFAULT '60',
  sampleRate DOUBLE NOT NULL,
  sampleCnt BIGINT(20) UNSIGNED NOT NULL,
  analogs TEXT NOT NULL,
  events TEXT NOT NULL,
  data LONGTEXT NOT NULL,
  dataBlob 		longblob 		,
  faultLocation longtext,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=INNODB$$


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
  PRIMARY KEY (id,stationId),
  KEY idx_faultId (faultId)
) ENGINE=INNODB $$

# LTR Data staging table creation
CREATE TABLE IF NOT EXISTS alarms_log (
  idalarms_log int(10) unsigned NOT NULL AUTO_INCREMENT,
  stationId int(11) NOT NULL,
  led_name varchar(45) DEFAULT NULL,
  led_status varchar(45) DEFAULT NULL,
  relays varchar(45) DEFAULT NULL,
  description text,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idalarms_log)
) ENGINE=INNODB $$

CREATE TABLE IF NOT EXISTS reports (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  stationId int(11) NOT NULL,
  report_action varchar(45) NOT NULL,
  report_time timestamp NULL DEFAULT NULL,
  report longtext,
  status tinytext,
  status_msg text,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (id)
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

CREATE TABLE IF NOT EXISTS continuous_comtrade_data (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  stationId int(11) NOT NULL,
  contDataType TEXT NOT NULL,
  startDateTime DATETIME  NOT NULL,
  endDateTime DATETIME  NOT NULL,
  fileName tinytext NOT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id,stationId)
) ENGINE=INNODB  $$

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

delimiter ;

