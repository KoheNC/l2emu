SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `announceId` int(10) NOT NULL,
  `announcement` text,
  `dateStart` date DEFAULT NULL,
  `dateEnd` date DEFAULT NULL,
  PRIMARY KEY (`announceId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
