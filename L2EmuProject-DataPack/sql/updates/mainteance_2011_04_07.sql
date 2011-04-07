UPDATE `spawnlist` SET `locx` = 119120, `locy` = 80576-, `locz` = -2694 WHERE `npc_templateid` = 32757; -- Priest Greymore new location

UPDATE `raidboss_spawnlist` SET `boss_id` = 25701 WHERE `boss_id` = 29096; -- Anays spawn

UPDATE `minions` SET `boss_id` = 25701 WHERE `boss_id` = 29096; -- Update Anays minions

UPDATE `npcaidata` SET `clan` = 'solina_clan', `clan_range` = 1000, `ai_type` = 'balanced' WHERE `npc_id` = 25701; -- Anays AI