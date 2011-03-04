-- ----------------------------
-- Table structure for global_drops
-- ----------------------------
DROP TABLE IF EXISTS `global_drops`;
CREATE TABLE `global_drops` (
  `eventId` int(11) NOT NULL,
  `eventName` varchar(128) NOT NULL,
  `itemId` int(11) NOT NULL,
  `countMin` int(11) NOT NULL,
  `countMax` int(11) NOT NULL,
  `chance` int(11) NOT NULL,
  `dateStart` date DEFAULT NULL,
  `dateEnd` date DEFAULT NULL,
  PRIMARY KEY (`eventId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `global_drops` VALUES ('1', 'Glittering Medals Event', '6393', '5', '20', '700000', '2011-02-27', '2011-02-28');
