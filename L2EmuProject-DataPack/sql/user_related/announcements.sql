DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `announceId` int(10) NOT NULL,
  `announcement` text,
  `dateStart` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  `dateEnd` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`announceId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;