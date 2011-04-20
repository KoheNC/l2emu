DROP TABLE IF EXISTS `global_drops`;
CREATE TABLE `global_drops` (
  `eventId` int(11) NOT NULL,
  `eventName` varchar(128) NOT NULL,
  `itemId` int(11) NOT NULL,
  `countMin` int(11) NOT NULL,
  `countMax` int(11) NOT NULL,
  `chance` int(11) NOT NULL,
  `dateStart` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  `dateEnd` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`eventId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;