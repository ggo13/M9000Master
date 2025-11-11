use m9000;
CREATE TABLE `m9000`.`LcItems` (
  `clientId` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `serverId` BIGINT NOT NULL DEFAULT -1,
  `triggerTime` DATETIME NOT NULL,
  `faultInfo` LONGTEXT NOT NULL,
  `infPath` LONGTEXT NOT NULL,
  `hasCorrelation` TINYINT(1) NOT NULL DEFAULT 0,
  `correlationResult` LONGTEXT NULL,
  `InfUpdated` TINYINT(1) NOT NULL DEFAULT 0,
  `correlationReceived` TINYINT(1) NOT NULL DEFAULT 0,
  `serverNotified` TINYINT(1) NOT NULL DEFAULT 0,
  `infBroadcasted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`clientId`),
  UNIQUE INDEX `clientId_UNIQUE` (`clientId` ASC) VISIBLE);