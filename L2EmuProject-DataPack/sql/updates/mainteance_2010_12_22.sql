-- Tracert
ALTER TABLE `accounts` ADD  `pcIp` char(15) DEFAULT NULL;
ALTER TABLE `accounts` ADD  `hop1` char(15) DEFAULT NULL;
ALTER TABLE `accounts` ADD  `hop2` char(15) DEFAULT NULL;
ALTER TABLE `accounts` ADD  `hop3` char(15) DEFAULT NULL;
ALTER TABLE `accounts` ADD  `hop4` char(15) DEFAULT NULL;

-- Chamber of Delusion update by Stake
DELETE FROM character_quests WHERE name LIKE 'ChamberOfDelusion%';

UPDATE `character_quests` SET `name` = 'Kief' WHERE `name` = '9006_Kief';
UPDATE `character_quests` SET `name` = 'Hude' WHERE `name` = '9005_Hude';
UPDATE `character_quests` SET `name` = 'Bernarde' WHERE `name` = '9004_Bernarde';