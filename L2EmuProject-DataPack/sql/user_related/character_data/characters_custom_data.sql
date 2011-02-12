-- ------------------------------------------
-- Table structure for characters_custom_data
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS `characters_custom_data` (
  `charId` decimal(11,0) NOT NULL,
  `char_name` varchar(35) NOT NULL DEFAULT '',
  `hero` decimal(1,0) NOT NULL DEFAULT '0',
  `noble` decimal(1,0) NOT NULL DEFAULT '0',
  `donator` decimal(1,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`)
) DEFAULT CHARSET=utf8;   
   
-- L2Emu Project