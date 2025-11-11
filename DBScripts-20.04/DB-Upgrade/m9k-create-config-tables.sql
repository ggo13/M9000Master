DELIMITER $$
use m9000$$

CREATE TABLE IF NOT EXISTS emailReportsSettings (
  id enum('1') NOT NULL,
  enableDailyStatusEmails tinyint DEFAULT '0',
  dailyStatusRepeatInterval smallint DEFAULT '1440',
  dailyStatusReportTime char(5) DEFAULT '00:00',
  dailyStatusReportTitle varchar(100) DEFAULT 'DME Daily Health Report',
  stationSpecificStatusReportTitle varchar(100) DEFAULT 'DME Alarms Report',
  enableSerEmail tinyint DEFAULT '0',
  serRunFrequency smallint DEFAULT '300',
  serEmailFileType char(10) DEFAULT NULL,
  serEmailAttachementSizeLimit smallint DEFAULT '2',
  enableFaultEmail tinyint DEFAULT '0',
  enableFaultsBooleanLogicFilter tinyint DEFAULT '0',
  enableFaultsWithAttachment tinyint DEFAULT '0',
  faultEmailAttachementSizeLimit smallint DEFAULT '2',
  faultsEmailDailyLimit smallint DEFAULT '10',
  masterHealthStatusPoll tinyint DEFAULT '0',
  masterHealthStatusPollingFrequency smallint DEFAULT '30',
  masterNotificationListener tinyint DEFAULT '0',
  enableConfigChangeEmail TINYINT(1) NULL DEFAULT '0',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1  $$

CREATE TABLE IF NOT EXISTS emailSettings (
  id enum('1') NOT NULL,
  enableEmail tinyint DEFAULT NULL,
  emailServerHost varchar(100) DEFAULT NULL,
  emailSmtpPort smallint unsigned DEFAULT NULL,
  fromEmail varchar(320) DEFAULT NULL,
  emailServerPassword varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1  $$

CREATE TABLE IF NOT EXISTS emailsSubscriptionList (
  id int NOT NULL AUTO_INCREMENT,
  emailAddress varchar(320) DEFAULT NULL,
  firstName varchar(45) DEFAULT NULL,
  lastName varchar(45) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY emailAddress_UNIQUE (emailAddress)
) ENGINE=InnoDB DEFAULT CHARSET=latin1  $$



delimiter ;
