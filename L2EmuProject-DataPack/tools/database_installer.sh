#!/bin/bash

############################################
##           WARNING!  WARNING!           ##
##                                        ##
## Don't edit this script on Windows OS   ##
## Or use a software which allows you to  ##
## write in UNIX type                     ##
############################################
## Written by Respawner                   ##
## License: GNU GPL                       ##
## Based on L2JDP script                  ##
############################################

# Catch kill signals
trap finish 1 2 15

# Configure the database access
configure()
{
	echo "#################################################"
	echo "#               Configuration area              #"
	echo "#         Please answer to the questions        #"
	echo "#################################################"
	MYSQLDUMPPATH=`which mysqldump 2>/dev/null`
	MYSQLPATH=`which mysql 2>/dev/null`
	if [ $? -ne 0 ]; then
		echo "Unable to find MySQL binaries on your PATH"
		while :
		do
			echo -ne "\nPlease enter MySQL binaries directory (no trailing slash): "
			read MYSQLBINPATH
			if [ -e "$MYSQLBINPATH" ] && [ -d "$MYSQLBINPATH" ] && [ -e "$MYSQLBINPATH/mysqldump" ] && [ -e "$MYSQLBINPATH/mysql" ]; then
				MYSQLDUMPPATH="$MYSQLBINPATH/mysqldump"
				MYSQLPATH="$MYSQLBINPATH/mysql"
				break
			else
				echo "Invalid data. Please verify and try again."
				exit 1
			fi
		done
	fi

	# LoginServer
	echo -ne "\nPlease enter MySQL LoginServer hostname (default localhost): "
	read LSDBHOST
	if [ -z "$LSDBHOST" ]; then
		LSDBHOST="localhost"
	fi
	echo -ne "\nPlease enter MySQL Login Server database name (default L2Emu_DB): "
	read LSDB
	if [ -z "$LSDB" ]; then
		LSDB="L2Emu_DB"
	fi
	echo -ne "\nPlease enter MySQL Login Server user (default root): "
	read LSUSER
	if [ -z "$LSUSER" ]; then
		LSUSER="root"
	fi
	echo -ne "\nPlease enter MySQL Login Server $LSUSER's password (won't be displayed) :"
	stty -echo
	read LSPASS
	stty echo
	echo ""
	if [ -z "$LSPASS" ]; then
		echo "Please avoid empty password else you will have a security problem."
	fi

	# GameServer
	echo -ne "\nPlease enter MySQL Game Server hostname (default $LSDBHOST): "
	read GSDBHOST
	if [ -z "$GSDBHOST" ]; then
		GSDBHOST="$LSDBHOST"
	fi
	echo -ne "\nPlease enter MySQL Game Server database name (default $LSDB): "
	read GSDB
	if [ -z "$GSDB" ]; then
		GSDB="$LSDB"
	fi
	echo -ne "\nPlease enter MySQL Game Server user (default $LSUSER): "
	read GSUSER
	if [ -z "$GSUSER" ]; then
		GSUSER="$LSUSER"
	fi
	echo -ne "\nPlease enter MySQL Game Server $GSUSER's password (won't be displayed): "
	stty -echo
	read GSPASS
	stty echo
	echo ""
	if [ -z "$GSPASS" ]; then
		echo "Please avoid empty password else you will have a security problem."
	fi
}

# Actions which can be performed
action_type()
{
	echo "#################################################"
	echo "#           Database Installer Script           #"
	echo "#################################################"
	echo ""
	echo "What do you want to do?"
	echo "Database backup           [b] (make a backup of the existing tables)"
	echo "Insert backups            [r] (Erase all the tables and insert the backups)"
	echo "Full installation         [f] (for first installation, this will erase all the existing tables)"
	echo "Update non critical data  [u] (Only erase and reinsert tables without players' data)"
	echo "Insert one table          [t] (Only insert one table in your database)"
	echo "Quit this script          [q]"
	echo -ne "Choice: "
	read ACTION_CHOICE
	case "$ACTION_CHOICE" in
		"b"|"B") backup_db; finish;;
		"r"|"R") insert_backup; finish;;
		"f"|"F") full_install; finish;;
		"u"|"U") update_db noncritical; finish;;
		"t"|"T") table_insert;;
		"q"|"Q") finish;;
		*)       action_type;;
	esac
}

# Make a backup of the LS and GS database
backup_db()
{
	echo "#################################################"
	echo "#                Database Backup                #"
	echo "#################################################"
	echo ""
	echo "LoginServer backup"
	$MYSQLDUMPPATH --add-drop-table -h $LSDBHOST -u $LSUSER --password=$LSPASS $LSDB > loginserver_backup.sql
	echo "GameServer backup"
	$MYSQLDUMPPATH --add-drop-table -h $GSDBHOST -u $GSUSER --password=$GSPASS $GSDB > gameserver_backup.sql
}

# Insert backups
insert_backup()
{
	echo "#################################################"
	echo "#                Database Backup                #"
	echo "#################################################"
	echo ""
	echo "What backups do you want to insert?"
	echo "Enter the full path of your backups."
	echo "LoginServer backup: "
	read LS_BACKUP
	echo "GameServer backup: "
	read GS_BACKUP
	echo "Deleting old tables"
	$MYL < login_install.sql &> /dev/null
	$MYG < full_install.sql &> /dev/null
	echo "Inserting Backups"
	$MYL < ../sql/$LS_BACKUP &> /dev/null
	$MYG < ../sql/$GS_BACKUP &> /dev/null
	echo "Backup restore completed"
}

# Full installation (erase and insert all tables)
full_install()
{
	echo "#################################################"
	echo "#          Full Database Installation           #"
	echo "#################################################"
	echo ""
	echo "LoginServer database"
	$MYL < login_install.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/login_related/account_data.sql  &> /dev/null
	$MYG < ../sql/login_related/accounts.sql  &> /dev/null
	$MYG < ../sql/login_related/gameservers.sql  &> /dev/null
	echo "GameServer database"
	$MYG < full_install.sql  &> /dev/null &> /dev/null
	echo "Server Related Tables"
	$MYG < ../sql/server_related/auto_chat.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/auto_chat_text.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/changelog.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/char_creation_items.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/char_templates.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/class_list.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/fish.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/lvlupgain.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/version.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/walker_routes.sql  &> /dev/null &> /dev/null
	echo "Castle Related Tables"
	$MYG < ../sql/server_related/castles/castle_door.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/castles/castle_manor_procure.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/castles/castle_manor_production.sql  &> /dev/null &> /dev/null
	echo "Drop related Tables"
	$MYG < ../sql/server_related/drops/droplist.sql  &> /dev/null &> /dev/null
	echo "Fortress Related Tables"
	$MYG < ../sql/server_related/fortresses/fort_staticobjects.sql  &> /dev/null &> /dev/null
	echo "Item Related Tables"
	$MYG < ../sql/server_related/items/armor.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/armorsets.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/etcitem.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/henna.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/henna_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/market_icons.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/weapon.sql  &> /dev/null &> /dev/null
	echo "NPC Related Tables"
	$MYG < ../sql/server_related/npcs/minions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/npc.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/npc_char_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/npc_elementals.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/npcskills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/pets_stats.sql  &> /dev/null &> /dev/null
	echo "Shop Related Tables"
	$MYG < ../sql/server_related/shops/merchant_buylists.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/shops/merchant_shopids.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/shops/merchants.sql  &> /dev/null &> /dev/null
	echo "Skill Related Tables"
	$MYG < ../sql/server_related/skills/buff_templates.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/certification_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/enchant_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/fishing_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/pets_skills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/pledge_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/skill_learn.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/skill_residential.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/skill_spellbooks.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/special_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/transform_skill_trees.sql  &> /dev/null &> /dev/null
	echo "Spawn Related Tables"
	$MYG < ../sql/server_related/spawns/castle_siege_guards.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/clanhall_siege_guards.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/fort_siege_guards.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/fort_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/four_sepulchers_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/hellbound_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/lastimperialtomb_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/random_spawn.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/random_spawn_loc.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/spawnlist_fantasy_island.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/territory_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/vanhalter_spawnlist.sql  &> /dev/null &> /dev/null
	echo "Teleport Related Tables"
	$MYG < ../sql/server_related/teleports/teleport.sql  &> /dev/null &> /dev/null
	echo "User Related Tables"
	$MYG < ../sql/user_related/auto_announcements.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/class_balance.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/couples.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/games.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/global_tasks.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/obj_restrictions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/persistent_properties.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/quest_global_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/record.sql  &> /dev/null &> /dev/null
	echo "7Sings Related Tables"
	$MYG < ../sql/user_related/7signs/seven_signs.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/7signs/seven_signs_festival.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/7signs/seven_signs_status.sql  &> /dev/null &> /dev/null
	echo "Boss Related Tables"
	$MYG < ../sql/user_related/bosses/grandboss_intervallist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/bosses/grandboss_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/bosses/raidboss_spawnlist.sql  &> /dev/null &> /dev/null
	echo "Character Related Tables"
	$MYG < ../sql/user_related/character_data/character_birthdays.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_blocks.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_effects.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_friends.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_hennas.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_instance_time.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_macroses.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_mail.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_name_title_colors.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_quest_global_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_quests.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_raid_points.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_recipebook.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_recommend_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_recommends.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_shortcuts.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_skill_reuses.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_skills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_subclass_certification.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_subclasses.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_tpbookmark.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_ui_actions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_ui_categories.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/characters.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/characters_custom_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/cursed_weapons.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/gm_audit.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/item_attributes.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/items.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/itemsonground.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/messages.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/petitions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/pets.sql  &> /dev/null &> /dev/null
	echo "Clan Related Tables"
	$MYG < ../sql/user_related/clan_data/airships.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle_doorupgrade.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle_functions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle_hired_guards.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle_zoneupgrade.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_notices.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_privs.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_skills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_subpledges.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_wars.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clanhall.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clanhall_functions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clanhall_sieges.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/fort.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/fort_doorupgrade.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/fort_functions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/fortsiege_clans.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/siege_clans.sql  &> /dev/null &> /dev/null
	echo "Community Board Related Tables"
	$MYG < ../sql/user_related/community_board/forums.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/community_board/posts.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/community_board/topic.sql  &> /dev/null &> /dev/null
	echo "Olympiad Related Tables"
	$MYG < ../sql/user_related/olympiad_data/heroes.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/olympiad_data/olympiad_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/olympiad_data/olympiad_nobles.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/olympiad_data/olympiad_nobles_eom.sql  &> /dev/null &> /dev/null
	echo "Territory Related Tables"
	$MYG < ../sql/user_related/territory_war/territories.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/territory_war/territory_registrations.sql  &> /dev/null &> /dev/null
	echo "Transaction Related Tables"
	$MYG < ../sql/user_related/transactions/auction.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/auction_bid.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/auction_lots.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/item_auction.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/item_auction_bid.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/item_market.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/market_seller.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/offline_traders.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/offline_traders_items.sql  &> /dev/null &> /dev/null
	echo "Custom Tables"
	$MYG < ../sql/custom/custom_armor.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_droplist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_etcitem.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_merchant_buylists.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_merchant_shopids.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_npc.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_npcskills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_weapon.sql  &> /dev/null &> /dev/null
}

# Database update
update_db()
{
	echo "###########################"
	echo "#     Database Update     #"
	echo "###########################"
	echo ""
	echo "Skipping LoginServer database"
	echo "Upgrading GameServer database"
	$MYG < ../sql/server_related/auto_chat.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/auto_chat_text.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/changelog.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/char_creation_items.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/char_templates.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/class_list.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/fish.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/lvlupgain.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/version.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/walker_routes.sql  &> /dev/null &> /dev/null
	echo "Castle Related Tables"
	$MYG < ../sql/server_related/castles/castle_door.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/castles/castle_manor_procure.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/castles/castle_manor_production.sql  &> /dev/null &> /dev/null
	echo "Drop related Tables"
	$MYG < ../sql/server_related/drops/droplist.sql  &> /dev/null &> /dev/null
	echo "Fortress Related Tables"
	$MYG < ../sql/server_related/fortresses/fort_staticobjects.sql  &> /dev/null &> /dev/null
	echo "Item Related Tables"
	$MYG < ../sql/server_related/items/armor.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/armorsets.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/etcitem.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/henna.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/henna_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/market_icons.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/items/weapon.sql  &> /dev/null &> /dev/null
	echo "NPC Related Tables"
	$MYG < ../sql/server_related/npcs/minions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/npc.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/npc_char_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/npc_elementals.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/npcskills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/npcs/pets_stats.sql  &> /dev/null &> /dev/null
	echo "Shop Related Tables"
	$MYG < ../sql/server_related/shops/merchant_buylists.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/shops/merchant_shopids.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/shops/merchants.sql  &> /dev/null &> /dev/null
	echo "Skill Related Tables"
	$MYG < ../sql/server_related/skills/buff_templates.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/certification_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/enchant_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/fishing_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/pets_skills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/pledge_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/skill_learn.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/skill_residential.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/skill_spellbooks.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/special_skill_trees.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/skills/transform_skill_trees.sql  &> /dev/null &> /dev/null
	echo "Spawn Related Tables"
	$MYG < ../sql/server_related/spawns/castle_siege_guards.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/clanhall_siege_guards.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/fort_siege_guards.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/fort_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/four_sepulchers_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/hb_naia_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/lastimperialtomb_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/random_spawn.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/random_spawn_loc.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/spawnlist_fantasy_island.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/territory_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/server_related/spawns/vanhalter_spawnlist.sql  &> /dev/null &> /dev/null
	echo "User Related Tables"
	$MYG < ../sql/user_related/auto_announcements.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/class_balance.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/couples.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/games.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/global_tasks.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/obj_restrictions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/persistent_properties.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/quest_global_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/record.sql  &> /dev/null &> /dev/null
	echo "7Sings Related Tables"
	$MYG < ../sql/user_related/7signs/seven_signs.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/7signs/seven_signs_festival.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/7signs/seven_signs_status.sql  &> /dev/null &> /dev/null
	echo "Boss Related Tables"
	$MYG < ../sql/user_related/bosses/grandboss_intervallist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/bosses/grandboss_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/bosses/raidboss_spawnlist.sql  &> /dev/null &> /dev/null
	echo "Character Related Tables"
	$MYG < ../sql/user_related/character_data/character_birthdays.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_blocks.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_effects.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_friends.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_hennas.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_instance_time.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_macroses.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_mail.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_name_title_colors.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_quest_global_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_quests.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_raid_points.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_recipebook.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_recommend_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_recommends.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_shortcuts.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_skill_reuses.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_skills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_subclass_certification.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_subclasses.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_tpbookmark.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_ui_actions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/character_ui_categories.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/characters.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/characters_custom_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/cursed_weapons.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/gm_audit.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/item_attributes.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/items.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/itemsonground.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/messages.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/petitions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/character_data/pets.sql  &> /dev/null &> /dev/null
	echo "Clan Related Tables"
	$MYG < ../sql/user_related/clan_data/airships.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle_doorupgrade.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle_functions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle_hired_guards.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/castle_zoneupgrade.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_notices.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_privs.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_skills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_subpledges.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clan_wars.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clanhall.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clanhall_functions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/clanhall_sieges.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/fort.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/fort_doorupgrade.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/fort_functions.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/fortsiege_clans.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/clan_data/siege_clans.sql  &> /dev/null &> /dev/null
	echo "Community Board Related Tables"
	$MYG < ../sql/user_related/community_board/forums.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/community_board/posts.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/community_board/topic.sql  &> /dev/null &> /dev/null
	echo "Olympiad Related Tables"
	$MYG < ../sql/user_related/olympiad_data/heroes.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/olympiad_data/olympiad_data.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/olympiad_data/olympiad_nobles.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/olympiad_data/olympiad_nobles_eom.sql  &> /dev/null &> /dev/null
	echo "Territory Related Tables"
	$MYG < ../sql/user_related/territory_war/territories.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/territory_war/territory_registrations.sql  &> /dev/null &> /dev/null
	echo "Transaction Related Tables"
	$MYG < ../sql/user_related/transactions/auction.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/auction_bid.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/auction_lots.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/item_auction.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/item_auction_bid.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/item_market.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/market_seller.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/offline_traders.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/user_related/transactions/offline_traders_items.sql  &> /dev/null &> /dev/null
	echo "Custom Tables"
	$MYG < ../sql/custom/custom_armor.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_droplist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_etcitem.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_merchant_buylists.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_merchant_shopids.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_npc.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_npcskills.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_spawnlist.sql  &> /dev/null &> /dev/null
	$MYG < ../sql/custom/custom_weapon.sql  &> /dev/null &> /dev/null
}

# Insert only one table the user want
table_insert()
{
	echo "#################################################"
	echo "#                 Table insertion               #"
	echo "#################################################"
	echo ""
	echo -ne "What table do you want to insert? (don't add .sql extension) "
	read TABLE
	echo "Insertion of file $TABLE"
	$MYG < ../sql/$TABLE.sql &> /dev/null
	echo "Insertion completed"
	action_type
}

# End of the script
finish()
{
	echo ""
	echo "Script execution finished."
	exit 0
}

# Clear console
clear

# Call configure function
configure

# Open MySQL connections
MYL="$MYSQLPATH -h $LSDBHOST -u $LSUSER --password=$LSPASS -D $LSDB"
MYG="$MYSQLPATH -h $GSDBHOST -u $GSUSER --password=$GSPASS -D $GSDB"

# Ask action to do
action_type