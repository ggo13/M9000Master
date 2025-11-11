delimiter $$
USE m9000 $$
drop table if exists federated_dat_staging$$
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

drop table if exists federated_comtrade_details $$
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

# Federated LTR table
drop table if exists federated_long_term_dat $$
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
  comments	TEXT,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=FEDERATED  CONNECTION='fedlink/long_term_dat'$$

# Continuous comtrade details table creation
drop table if exists federated_continuous_comtrade_data $$
CREATE TABLE IF NOT EXISTS federated_continuous_comtrade_data (
  stationId int(11) NOT NULL,
  contDataType TEXT NOT NULL,
  startDateTime DATETIME  NOT NULL,
  endDateTime DATETIME  NOT NULL,
  fileName tinytext NOT NULL,
  updated timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=FEDERATED DEFAULT CHARSET=latin1 CONNECTION='fedlink/continuous_comtrade_data' $$

drop table if exists federated_ser $$
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


delimiter ;