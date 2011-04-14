SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for raidboss_status
-- ----------------------------
CREATE TABLE IF NOT EXISTS `raidboss_status` (
  `bossId` int(11) NOT NULL DEFAULT '0',
  `currentHp` decimal(8,0) DEFAULT NULL,
  `currentMp` decimal(8,0) DEFAULT NULL,
  `respawn_time` timestamp NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`bossId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `raidboss_status` VALUES ('25001', '95986', '545', null);
INSERT INTO `raidboss_status` VALUES ('25004', '168366', '763', null);
INSERT INTO `raidboss_status` VALUES ('25007', '331522', '1062', null);
INSERT INTO `raidboss_status` VALUES ('25010', '624464', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25013', '507285', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25016', '188376', '2368', null);
INSERT INTO `raidboss_status` VALUES ('25019', '206185', '606', null);
INSERT INTO `raidboss_status` VALUES ('25020', '156584', '893', null);
INSERT INTO `raidboss_status` VALUES ('25023', '208019', '1415', null);
INSERT INTO `raidboss_status` VALUES ('25026', '352421', '1660', null);
INSERT INTO `raidboss_status` VALUES ('25029', '156190', '1911', null);
INSERT INTO `raidboss_status` VALUES ('25032', '229722', '2707', null);
INSERT INTO `raidboss_status` VALUES ('25035', '888658', '3058', null);
INSERT INTO `raidboss_status` VALUES ('25038', '116581', '699', null);
INSERT INTO `raidboss_status` VALUES ('25041', '165289', '927', null);
INSERT INTO `raidboss_status` VALUES ('25044', '319791', '1296', null);
INSERT INTO `raidboss_status` VALUES ('25047', '352421', '1660', null);
INSERT INTO `raidboss_status` VALUES ('25050', '771340', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25051', '818959', '2707', null);
INSERT INTO `raidboss_status` VALUES ('25054', '945900', '3420', null);
INSERT INTO `raidboss_status` VALUES ('25057', '288415', '2235', null);
INSERT INTO `raidboss_status` VALUES ('25060', '99367', '575', null);
INSERT INTO `raidboss_status` VALUES ('25063', '330579', '927', null);
INSERT INTO `raidboss_status` VALUES ('25064', '218810', '1120', null);
INSERT INTO `raidboss_status` VALUES ('25067', '554640', '1598', null);
INSERT INTO `raidboss_status` VALUES ('25070', '451391', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25073', '875948', '2987', null);
INSERT INTO `raidboss_status` VALUES ('25076', '103092', '606', null);
INSERT INTO `raidboss_status` VALUES ('25079', '168366', '763', null);
INSERT INTO `raidboss_status` VALUES ('25082', '206753', '1062', null);
INSERT INTO `raidboss_status` VALUES ('25085', '371721', '1355', null);
INSERT INTO `raidboss_status` VALUES ('25088', '702418', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25089', '512194', '2301', null);
INSERT INTO `raidboss_status` VALUES ('25092', '888658', '3058', null);
INSERT INTO `raidboss_status` VALUES ('25095', '121941', '731', null);
INSERT INTO `raidboss_status` VALUES ('25098', '330579', '927', null);
INSERT INTO `raidboss_status` VALUES ('25099', '273375', '1355', null);
INSERT INTO `raidboss_status` VALUES ('25102', '576831', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25103', '451391', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25106', '526218', '2570', null);
INSERT INTO `raidboss_status` VALUES ('25109', '935092', '3347', null);
INSERT INTO `raidboss_status` VALUES ('25112', '127782', '763', null);
INSERT INTO `raidboss_status` VALUES ('25115', '294846', '1120', null);
INSERT INTO `raidboss_status` VALUES ('25118', '330579', '1415', null);
INSERT INTO `raidboss_status` VALUES ('25119', '507285', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25122', '467209', '2235', null);
INSERT INTO `raidboss_status` VALUES ('25125', '1637918', '2707', null);
INSERT INTO `raidboss_status` VALUES ('25126', '1974940', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25127', '198734', '763', null);
INSERT INTO `raidboss_status` VALUES ('25128', '148507', '860', null);
INSERT INTO `raidboss_status` VALUES ('25131', '369009', '1415', null);
INSERT INTO `raidboss_status` VALUES ('25134', '218810', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25137', '451391', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25143', '977229', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25146', '90169', '485', null);
INSERT INTO `raidboss_status` VALUES ('25149', '103092', '606', null);
INSERT INTO `raidboss_status` VALUES ('25152', '165289', '927', null);
INSERT INTO `raidboss_status` VALUES ('25155', '294846', '1120', null);
INSERT INTO `raidboss_status` VALUES ('25158', '920790', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25159', '435256', '1975', null);
INSERT INTO `raidboss_status` VALUES ('25163', '888658', '3058', null);
INSERT INTO `raidboss_status` VALUES ('25166', '134813', '606', null);
INSERT INTO `raidboss_status` VALUES ('25169', '336732', '763', null);
INSERT INTO `raidboss_status` VALUES ('25170', '195371', '1028', null);
INSERT INTO `raidboss_status` VALUES ('25173', '288415', '1415', null);
INSERT INTO `raidboss_status` VALUES ('25176', '451391', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25179', '526218', '2368', null);
INSERT INTO `raidboss_status` VALUES ('25182', '512194', '2707', null);
INSERT INTO `raidboss_status` VALUES ('25185', '165289', '927', null);
INSERT INTO `raidboss_status` VALUES ('25188', '255564', '763', null);
INSERT INTO `raidboss_status` VALUES ('25189', '156584', '893', null);
INSERT INTO `raidboss_status` VALUES ('25192', '258849', '1296', null);
INSERT INTO `raidboss_status` VALUES ('25198', '1777317', '2639', null);
INSERT INTO `raidboss_status` VALUES ('25199', '912634', '2707', null);
INSERT INTO `raidboss_status` VALUES ('25202', '935092', '2777', null);
INSERT INTO `raidboss_status` VALUES ('25205', '956490', '3274', null);
INSERT INTO `raidboss_status` VALUES ('25208', '218810', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25211', '174646', '1975', null);
INSERT INTO `raidboss_status` VALUES ('25214', '218810', '2368', null);
INSERT INTO `raidboss_status` VALUES ('25217', '369009', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25220', '924022', '3274', null);
INSERT INTO `raidboss_status` VALUES ('25223', '165289', '1237', null);
INSERT INTO `raidboss_status` VALUES ('25226', '768537', '2502', null);
INSERT INTO `raidboss_status` VALUES ('25229', '1891801', '3420', null);
INSERT INTO `raidboss_status` VALUES ('25230', '482650', '2169', null);
INSERT INTO `raidboss_status` VALUES ('25233', '1256671', '3643', null);
INSERT INTO `raidboss_status` VALUES ('25234', '1052436', '2707', null);
INSERT INTO `raidboss_status` VALUES ('25235', '912634', '3202', null);
INSERT INTO `raidboss_status` VALUES ('25238', '512194', '2846', null);
INSERT INTO `raidboss_status` VALUES ('25241', '624464', '2639', null);
INSERT INTO `raidboss_status` VALUES ('25244', '1891801', '3420', null);
INSERT INTO `raidboss_status` VALUES ('25245', '977229', '3643', null);
INSERT INTO `raidboss_status` VALUES ('25248', '1825269', '3274', null);
INSERT INTO `raidboss_status` VALUES ('25249', '945900', '3420', null);
INSERT INTO `raidboss_status` VALUES ('25252', '888658', '3058', null);
INSERT INTO `raidboss_status` VALUES ('25255', '1637918', '2707', null);
INSERT INTO `raidboss_status` VALUES ('25256', '526218', '2368', null);
INSERT INTO `raidboss_status` VALUES ('25259', '1248928', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25260', '392985', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25263', '848789', '2846', null);
INSERT INTO `raidboss_status` VALUES ('25266', '945900', '3420', null);
INSERT INTO `raidboss_status` VALUES ('25269', '888658', '3058', null);
INSERT INTO `raidboss_status` VALUES ('25272', '233163', '1415', null);
INSERT INTO `raidboss_status` VALUES ('25276', '1891801', '3420', null);
INSERT INTO `raidboss_status` VALUES ('25277', '507285', '1722', null);
INSERT INTO `raidboss_status` VALUES ('25280', '1248928', '2039', null);
INSERT INTO `raidboss_status` VALUES ('25281', '1777317', '3058', null);
INSERT INTO `raidboss_status` VALUES ('25282', '1891801', '3420', null);
INSERT INTO `raidboss_status` VALUES ('25293', '977229', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25299', '714778', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25302', '743801', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25305', '1532678', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25309', '714778', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25312', '743801', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25315', '1532678', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25319', '1048567', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25322', '834231', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25325', '888658', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25328', '900867', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25352', '127782', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25354', '165289', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25357', '90169', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25360', '107186', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25362', '95986', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25365', '214372', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25366', '95986', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25369', '103092', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25372', '175392', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25373', '90169', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25375', '87696', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25378', '87696', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25380', '90169', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25383', '156584', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25385', '174646', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25388', '165289', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25391', '297015', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25392', '141034', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25394', '390743', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25395', '288415', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25398', '165289', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25401', '141034', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25404', '148507', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25407', '526218', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25410', '218810', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25412', '319791', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25415', '218810', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25418', '273375', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25420', '335987', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25423', '539706', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25426', '103092', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25429', '103092', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25431', '273375', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25434', '451391', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25437', '576831', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25438', '273375', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25441', '288415', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25444', '588136', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25447', '645953', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25450', '987470', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25453', '888658', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25456', '352421', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25460', '385670', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25463', '467209', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25473', '402319', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25475', '451391', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25478', '588136', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25481', '66938', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25484', '369009', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25493', '451391', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25496', '402319', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25498', '288415', '3718', null);
INSERT INTO `raidboss_status` VALUES ('25524', '956490', '3247', null);
INSERT INTO `raidboss_status` VALUES ('25527', '1608553', '3718', null);
INSERT INTO `raidboss_status` VALUES ('29095', '2289038', '2746', null);
INSERT INTO `raidboss_status` VALUES ('29096', '759807', '2277', null);
