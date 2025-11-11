# Create federated tables to talk to the remote master
delimiter $$
USE m9000 $$
system echo "About to create federated tables for remote configuration..."
# Create a FEDERATED table to talk to remote

CREATE TABLE IF NOT EXISTS federated_dat_staging 
(
  faultId 	int(10) 		NOT NULL,
  stationId 	smallint(5) unsigned 	NOT NULL DEFAULT '18',
  type 		char(6) 		NOT NULL DEFAULT 'ASCII',
  tsPrefault 	bigint(20)  unsigned 	NOT NULL,
  tsTrigger 	bigint(20)  unsigned 	NOT NULL,
  lineFreq 	double 			NOT NULL DEFAULT '60',
  sampleRate 	double 			NOT NULL,
  sampleCnt 	bigint(20)  unsigned 	NOT NULL,
  analogs 	longtext 			NOT NULL,
  events 	longtext 			NOT NULL,
  data 		longtext,
  dataBlob 		longblob,
  faultLocation longtext,
  faultLogic	TINYINT(1),
  lineGroups	TEXT,
  comments	TEXT,
  updated	timestamp NULL DEFAULT CURRENT_TIMESTAMP  
 ) ENGINE=FEDERATED  CONNECTION='fedlink/dat_staging'$$


CREATE TABLE IF NOT EXISTS federated_comtrade_details (
  fault_id int(11) NOT NULL,
  station_id int(11) NOT NULL,
  time_stamp bigint(20) unsigned DEFAULT NULL,
  events text,
  length double unsigned DEFAULT NULL,
  pre_fault double DEFAULT NULL,
  post_fault double DEFAULT NULL,
  fault_location longtext,
  file_name varchar(255) DEFAULT NULL,
  fault_logic	TINYINT(1),
  line_groups	TEXT,
  comments	TEXT,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=FEDERATED  CONNECTION='fedlink/comtrade_details'$$


CREATE TABLE IF NOT EXISTS federated_ser
(
	stationId	SMALLINT(10) 	UNSIGNED	NOT NULL,	-- Number assigned to the DFR
	eventNum	INT 		UNSIGNED	NOT NULL,
	phase		CHAR(4)				NOT NULL,	-- A, B, C, or N.
	name		VARCHAR(64)			NOT NULL,	-- Name of this Event
	ts		BIGINT 		UNSIGNED	NOT NULL,
	normal		BOOL				NOT NULL,
	state		BOOL				NOT NULL,
	locked		BOOL				NOT NULL,
	updated	timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	ser_date datetime DEFAULT NULL  
)ENGINE=FEDERATED  CONNECTION='fedlink/ser'$$

# Federated LTR table
CREATE TABLE IF NOT EXISTS federated_long_term_dat (
  id int(10) unsigned NOT NULL,
  stationId int(10) unsigned NOT NULL,
  faultId int(11) NOT NULL,
  ltType varchar(45) DEFAULT NULL,
  tsTrigger bigint(20) NOT NULL,
  sampleRate int(11) NOT NULL,
  length double unsigned NOT NULL,
  preFault int(11) NOT NULL,
  postFault int(11) NOT NULL,
  fileName tinytext NOT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=FEDERATED  CONNECTION='fedlink/long_term_dat'$$

CREATE TABLE IF NOT EXISTS remote_server_station_details (
  stationId int(11) NOT NULL,
  name varchar(64) DEFAULT NULL,
  ParentZoneId text,
  recordingDevId varchar(255) DEFAULT 'USI_M9000',
  analogs_count int(11) DEFAULT '0',
  digitals_count int(11) DEFAULT '0',
  preFaultTime smallint(6) DEFAULT NULL,
  postFaultTime smallint(6) DEFAULT NULL,
  ltrPreFaultTime smallint(6) DEFAULT '0',
  ltrPostFaultTime smallint(6) DEFAULT '0',
  lineFreq double DEFAULT '60',
  sampleRate double DEFAULT NULL,
  longTermSampleRate double DEFAULT '1200',
  configXml longtext,
  status varchar(15) DEFAULT 'INCOMPLETE',
  created timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated timestamp NULL DEFAULT NULL,
  PRIMARY KEY (stationId)
) ENGINE=FEDERATED DEFAULT CHARSET=latin1 CONNECTION='fedlink/station_details'$$

CREATE TABLE IF NOT EXISTS federated_alarms_log (
  stationId int(11) NOT NULL,
  led_name varchar(45) DEFAULT NULL,
  led_status varchar(45) DEFAULT NULL,
  relays varchar(45) DEFAULT NULL,
  description text,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=FEDERATED DEFAULT CHARSET=latin1 CONNECTION='fedlink/alarms_log'$$

CREATE TABLE IF NOT EXISTS federated_reports (
  stationId int(11) NOT NULL,
  report_action varchar(45) NOT NULL,
  report_time timestamp NULL DEFAULT NULL,
  report longtext,
  status tinytext,
  status_msg text,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=FEDERATED DEFAULT CHARSET=latin1 CONNECTION='fedlink/reports'$$

# Continuous comtrade details table creation

CREATE TABLE IF NOT EXISTS federated_continuous_comtrade_data (
  stationId int(11) NOT NULL,
  contDataType TEXT NOT NULL,
  startDateTime DATETIME  NOT NULL,
  endDateTime DATETIME  NOT NULL,
  fileName tinytext NOT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=FEDERATED DEFAULT CHARSET=latin1 CONNECTION='fedlink/continuous_comtrade_data' $$

delimiter ;
system echo "Done\n"