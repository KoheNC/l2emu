DROP TABLE IF EXISTS `buff_templates`;
CREATE TABLE `buff_templates` (
  `id` int(10) unsigned NOT NULL,
  `name` varchar(35) NOT NULL DEFAULT '',
  `skill_id` smallint(5) unsigned NOT NULL,
  `skill_name` varchar(35) DEFAULT NULL,
  `skill_level` smallint(3) unsigned NOT NULL DEFAULT '1',
  `skill_force` tinyint(1) NOT NULL DEFAULT '1',
  `skill_order` tinyint(3) unsigned NOT NULL,
  `char_min_level` tinyint(2) unsigned NOT NULL DEFAULT '1',
  `char_max_level` tinyint(2) unsigned NOT NULL DEFAULT '85',
  `char_race` tinyint(2) unsigned NOT NULL DEFAULT '0',
  `char_class` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`name`,`skill_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- id - template id, not zero value
-- name - template name, for easer access thru html links
-- Note: 'SupportMagic' is reserved name for Newbie Helper buff template
--
-- skill_id, skill_level - skill for buff
-- skill_force - force skill cast, eve if same skill effect present
-- skill_order - order of skill in template, not zero value
-- skill_name - name of skill
--
-- player condtitions for buff: 
-- 
-- char_min_level 
-- char_max_level - minimum and maximum level of character
--
-- char_race - race mask of character
-- 16 - human
-- 8  - elf
-- 4  - dark elf
-- 2  - orc
-- 1  - dwarf
-- example: if set 16+8+1, this mean 25, 
-- then buff can land only on humans,elfs and dwarves
-- set 0 or 31 for all races
--
-- char_class
-- 2 - magus
-- 1 - fighter
-- 0 or 3 - all classes

INSERT INTO `buff_templates` VALUES
-- Verified on Gracia Final
('1', 'SupportMagic', '5637', 'Magic Barrier', '1', '1', '3', '6', '75', '0', '0'),
('1', 'SupportMagic', '4338', 'Life Cubic', '1', '1', '4', '16', '34', '0', '0'),
('1', 'SupportMagic', '4324', 'Bless the Body', '1', '1', '5', '6', '75', '0', '1'),
('1', 'SupportMagic', '4325', 'Vampiric Rage', '1', '1', '6', '6', '75', '0', '1'),
('1', 'SupportMagic', '4326', 'Regeneration', '1', '1', '7', '6', '75', '0', '1'),
('1', 'SupportMagic', '4327', 'Haste', '1', '1', '8', '6', '39', '0', '1'),
('1', 'SupportMagic', '5632', 'Haste', '1', '1', '9', '40', '75', '0', '1'),
('1', 'SupportMagic', '4328', 'Bless the Soul', '1', '1', '10', '6', '75', '0', '2'),
('1', 'SupportMagic', '4329', 'Acumen', '1', '1', '11', '6', '75', '0', '2'),
('1', 'SupportMagic', '4330', 'Concentration', '1', '1', '12', '6', '75', '0', '2'),
('1', 'SupportMagic', '4331', 'Empower', '1', '1', '13', '6', '75', '0', '2'),
('2', 'SupportMagicServitor', '4322', 'Wind Walk', '1', '1', '1', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4323', 'Shield', '1', '1', '2', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '5637', 'Magic Barrier', '1', '1', '3', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4324', 'Bless the Body', '1', '1', '4', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4325', 'Vampiric Rage', '1', '1', '5', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4326', 'Regeneration', '1', '1', '6', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4328', 'Bless the Soul', '1', '1', '7', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4329', 'Acumen', '1', '1', '8', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4330', 'Concentration', '1', '1', '9', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4331', 'Name', '1', '1', '10', '6', '75', '0', '0'),
('2', 'SupportMagicServitor', '4327', 'Haste', '1', '1', '11', '6', '39', '0', '0'),
('2', 'SupportMagicServitor', '5632', 'Haste', '1', '1', '12', '40', '75', '0', '0');