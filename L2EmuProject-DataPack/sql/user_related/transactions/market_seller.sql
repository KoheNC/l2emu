 CREATE TABLE IF NOT EXISTS `market_seller` (
  `sellerId` int(45) NOT NULL,
  `money` int(45) NOT NULL default '0',
  PRIMARY KEY  (`sellerId`)
);