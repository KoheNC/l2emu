 CREATE TABLE IF NOT EXISTS `item_market` (
  `ownerId` VARCHAR(45) NOT NULL DEFAULT '',
  `ownerName` VARCHAR(35) NOT NULL,
  `itemName` VARCHAR(45) NOT NULL,
  `enchLvl` VARCHAR(45) NOT NULL,
  `itemGrade` VARCHAR(45) NOT NULL,
  `l2Type` VARCHAR(45) NOT NULL DEFAULT '',
  `itemType` VARCHAR(45) NOT NULL,
  `itemId` VARCHAR(45) NOT NULL DEFAULT '',
  `itemObjId` VARCHAR(45) NOT NULL DEFAULT '',
  `_count` VARCHAR(45) NOT NULL DEFAULT '1',
  `price` VARCHAR(10) NOT NULL DEFAULT '',
  PRIMARY KEY (`ownerId`,`ownerName`,`itemId`,`itemObjId`)
);