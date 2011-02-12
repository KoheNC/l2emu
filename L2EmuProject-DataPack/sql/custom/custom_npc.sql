CREATE TABLE IF NOT EXISTS `custom_npc` (
  `id` decimal(11,0) NOT NULL DEFAULT '0',
  `idTemplate` int(11) NOT NULL DEFAULT '0',
  `name` varchar(200) DEFAULT NULL,
  `serverSideName` int(1) DEFAULT '0',
  `title` varchar(45) DEFAULT '',
  `serverSideTitle` int(1) DEFAULT '0',
  `class` varchar(200) DEFAULT NULL,
  `collision_radius` decimal(5,2) DEFAULT NULL,
  `collision_height` decimal(5,2) DEFAULT NULL,
  `level` decimal(2,0) DEFAULT NULL,
  `sex` varchar(6) DEFAULT NULL,
  `type` varchar(21) DEFAULT NULL,
  `attackrange` int(11) DEFAULT NULL,
  `hp` decimal(8,0) DEFAULT NULL,
  `mp` decimal(8,0) DEFAULT NULL,
  `hpreg` decimal(8,2) DEFAULT NULL,
  `mpreg` decimal(5,2) DEFAULT NULL,
  `str` decimal(7,0) DEFAULT NULL,
  `con` decimal(7,0) DEFAULT NULL,
  `dex` decimal(7,0) DEFAULT NULL,
  `int` decimal(7,0) DEFAULT NULL,
  `wit` decimal(7,0) DEFAULT NULL,
  `men` decimal(7,0) DEFAULT NULL,
  `exp` decimal(9,0) DEFAULT NULL,
  `sp` decimal(8,0) DEFAULT NULL,
  `patk` decimal(5,0) DEFAULT NULL,
  `pdef` decimal(5,0) DEFAULT NULL,
  `matk` decimal(5,0) DEFAULT NULL,
  `mdef` decimal(5,0) DEFAULT NULL,
  `atkspd` decimal(3,0) DEFAULT NULL,
  `aggro` decimal(6,0) DEFAULT NULL,
  `matkspd` decimal(4,0) DEFAULT NULL,
  `rhand` decimal(8,0) DEFAULT NULL,
  `lhand` decimal(8,0) DEFAULT NULL,
  `armor` decimal(1,0) DEFAULT NULL,
  `walkspd` decimal(3,0) DEFAULT NULL,
  `runspd` decimal(3,0) DEFAULT NULL,
  `faction_id` varchar(40) DEFAULT NULL,
  `faction_range` decimal(4,0) DEFAULT NULL,
  `isUndead` int(11) DEFAULT '0',
  `absorb_level` decimal(2,0) DEFAULT '0',
  `absorb_type` enum('FULL_PARTY','LAST_HIT','PARTY_ONE_RANDOM') NOT NULL DEFAULT 'LAST_HIT',
  `ss` int(4) DEFAULT '0',
  `bss` int(4) DEFAULT '0',
  `ss_rate` int(3) DEFAULT '0',
  `AI` varchar(8) DEFAULT 'fighter',
  `drop_herbs` enum('true','false') NOT NULL DEFAULT 'false',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `custom_npc`
--

-- 30038 - Wedding Priest
-- 50000 - Buffer
-- 50002 - GM Shop
-- 50003 - Item Marketer
-- 50004 - Craft Manager
-- 50009 - Announcer
-- 50011 - Level Changer
-- 50012 - Protector
-- 50015 - Class Master

INSERT INTO `custom_npc` (`id`, `idTemplate`, `name`, `serverSideName`, `title`, `serverSideTitle`, `class`, `collision_radius`, `collision_height`, `level`, `sex`, `type`, `attackrange`, `hp`, `mp`, `hpreg`, `mpreg`, `str`, `con`, `dex`, `int`, `wit`, `men`, `exp`, `sp`, `patk`, `pdef`, `matk`, `mdef`, `atkspd`, `aggro`, `matkspd`, `rhand`, `lhand`, `armor`, `walkspd`, `runspd`, `faction_id`, `faction_range`, `isUndead`, `absorb_level`, `absorb_type`, `ss`, `bss`, `ss_rate`, `AI`, `drop_herbs`) VALUES
(6001, 31774, 'Beryl the Cat', 0, 'ItemMall', 1, 'Monster2.queen_of_cat', 8.00, 15.00, 70, 'female', 'L2Npc', 40, 3862, 1493, NULL, NULL, 40, 43, 30, 21, 20, 10, 0, 0, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 0, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(6002, 35461, 'Caska', 1, 'NPC Buffer', 1, 'NPC.a_teleporter_FHuman', 8.00, 25.00, 70, 'female', 'L2Npc', 40, 3862, 1494, NULL, NULL, 40, 43, 30, 21, 20, 10, 5879, 590, 1444, 514, 760, 381, 253, 0, 253, 0, 0, 0, 80, 120, NULL, NULL, 0, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(7077, 31275, 'Tinkerbell', 1, 'Luxury Gatekeeper', 1, 'NPC.a_teleporter_FHuman', 8.00, 25.00, 70, 'female', 'L2Teleporter', 40, 3862, 1494, NULL, NULL, 40, 43, 30, 21, 20, 10, 5879, 590, 1444, 514, 760, 381, 253, 0, 253, 0, 0, 0, 80, 120, NULL, NULL, 0, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(2001, 29020, 'Baium', 1, 'Event', 1, 'Monster.baium', 65.00, 174.00, 75, 'male', 'L2GrandBoss', 40, 790857, 3347, 668.78, 3.09, 60, 57, 73, 76, 35, 80, 10253400, 1081544, 6559, 6282, 4378, 4601, 333, 0, 2362, 0, 0, 0, 80, 120, NULL, 0, 0, 12, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(2002, 25319, 'Ember', 1, 'Event', 1, 'Monster2.inferno_drake_100_bi', 48.00, 73.00, 85, 'male', 'L2RaidBoss', 40, 257725, 3718, 823.48, 9.81, 60, 57, 73, 76, 35, 80, 2535975, 1356048, 11906, 5036, 18324, 2045, 409, 0, 2901, 0, 0, 0, 80, 120, NULL, 0, 0, 13, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(2003, 29022, 'Zaken', 1, 'Event', 1, 'Monster.zaken', 16.00, 32.00, 60, 'male', 'L2GrandBoss', 40, 858518, 1975, 799.68, 2.45, 60, 57, 73, 76, 35, 80, 4879745, 423589, 7273, 2951, 19762, 1197, 333, 0, 2362, 0, 0, 0, 80, 120, NULL, 0, 1, 12, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(30038, 30175, 'Andromeda', 1, 'Wedding Priest', 1, 'NPC.a_casino_FDarkElf', 8.00, 23.00, 70, 'female', 'L2WeddingManager', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(50000, 30009, 'TOFIZ', 1, 'Buffer', 1, 'NPC.a_sanctuary_teacher_MHuman', 8.00, 23.00, 70, 'male', 'L2Buffer', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(50002, 30060, 'Icarus', 1, 'GM Shop Trader', 1, 'NPC.a_traderA_MHuman', 10.00, 24.00, 70, 'male', 'L2Merchant', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(50003, 30060, 'John', 1, 'Item Marketer', 1, 'NPC.a_traderA_MHuman', 10.00, 24.00, 70, 'male', 'L2ItemMarketer', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(50004, 30005, 'Charus', 1, 'Craft Manager', 1, 'NPC.a_warehouse_keeper_MDwarf', 8.00, 17.00, 70, 'male', 'L2CraftManager', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(50009, 30082, 'Newser', 1, 'Announcer', 1, 'NPC.a_traderA_FHuman', 8.00, 23.00, 70, 'male', 'L2Announcer', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(50011, 31854, 'Spice', 1, 'Sexy Changer', 1, 'NPC.a_maidA_FHuman', 8.00, 20.00, 70, 'female', 'L2LevelChanger', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(50012, 31854, 'Protector', 1, 'PVP/PK Manager', 1, 'NPC.a_maidA_FHuman', 8.00, 20.00, 70, 'female', 'L2Protector', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false'),
(50015, 31255, 'Pumpkin', 1, 'Class Master', 1, 'Monster.cat_the_cat', 9.00, 16.00, 70, 'male', 'L2ClassMaster', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 35, 10, 5879, 590, 1314, 470, 780, 382, 278, 0, 253, 0, 0, 0, 80, 120, NULL, 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter', 'false');