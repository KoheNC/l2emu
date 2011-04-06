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
	for ls in $(ls ..sql/login_related/*.sql);do
		$MYG < $ls
	echo "GameServer database"
	$MYG < full_install.sql  &> /dev/null &> /dev/null
	echo "Server Related Tables"
	for serverrelated in $(ls ..sql/server_related/*.sql);do
		$MYG < $serverrelated
	echo "Castle Related Tables"
	for castlerelated in $(ls ..sql/server_related/castles/*.sql);do
		$MYG < $castlerelated
	echo "Drop related Tables"
	for droprelated in $(ls ..sql/server_related/drops/*.sql);do
		$MYG < $droprelated
	echo "Fortress Related Tables"
	for fortrelated in $(ls ..sql/server_related/fortresses/*.sql);do
		$MYG < $fortrelated
	echo "Item Related Tables"
	for itemrelated in $(ls ..sql/server_related/items/*.sql);do
		$MYG < $itemrelated
	echo "NPC Related Tables"
	for npcrelated in $(ls ..sql/server_related/npcs/*.sql);do
		$MYG < $npcrelated
	echo "Shop Related Tables"
	for shoprelated in $(ls ..sql/server_related/shops/*.sql);do
		$MYG < $shoprelated
	echo "Skill Related Tables"
	for skillrelated in $(ls ..sql/server_related/skills/*.sql);do
		$MYG < $skillrelated
	echo "Spawn Related Tables"
	for spawnrelated in $(ls ..sql/server_related/spawns/*.sql);do
		$MYG < $spawnrelated
	echo "User Related Tables"
	for userrelated in $(ls ..sql/user_related/*.sql);do
		$MYG < $userrelated
	echo "7Sings Related Tables"
	for 7srelated in $(ls ..sql/user_related/7signs/*.sql);do
		$MYG < $7srelated
	echo "Character Related Tables"
	for charrelated in $(ls ..sql/user_related/character_data/*.sql);do
		$MYG < $charrelated
	echo "Clan Related Tables"
	for clanrelated in $(ls ..sql/user_related/clan_data/*.sql);do
		$MYG < $clanrelated
	echo "Community Board Related Tables"
	for cbrelated in $(ls ..sql/user_related/community_board/*.sql);do
		$MYG < $cbrelated
	echo "Olympiad Related Tables"
	for olyrelated in $(ls ..sql/user_related/olympiad_data/*.sql);do
		$MYG < $olyrelated
	echo "Territory Related Tables"
	for territoryrelated in $(ls ..sql/user_related/territory_war/*.sql);do
		$MYG < $territoryrelated
	echo "Transaction Related Tables"
	for traderelated in $(ls ..sql/user_related/transactions/*.sql);do
		$MYG < $traderelated
	echo "Custom Tables"
	for custom in $(ls ..sql/custom/*.sql);do
		$MYG < $custom
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
	echo "Server Related Tables"
	for serverrelated in $(ls ..sql/server_related/*.sql);do
		$MYG < $serverrelated
	echo "Castle Related Tables"
	for castlerelated in $(ls ..sql/server_related/castles/*.sql);do
		$MYG < $castlerelated
	echo "Drop related Tables"
	for droprelated in $(ls ..sql/server_related/drops/*.sql);do
		$MYG < $droprelated
	echo "Fortress Related Tables"
	for fortrelated in $(ls ..sql/server_related/fortresses/*.sql);do
		$MYG < $fortrelated
	echo "Item Related Tables"
	for itemrelated in $(ls ..sql/server_related/items/*.sql);do
		$MYG < $itemrelated
	echo "NPC Related Tables"
	for npcrelated in $(ls ..sql/server_related/npcs/*.sql);do
		$MYG < $npcrelated
	echo "Shop Related Tables"
	for shoprelated in $(ls ..sql/server_related/shops/*.sql);do
		$MYG < $shoprelated
	echo "Skill Related Tables"
	for skillrelated in $(ls ..sql/server_related/skills/*.sql);do
		$MYG < $skillrelated
	echo "Spawn Related Tables"
	for spawnrelated in $(ls ..sql/server_related/spawns/*.sql);do
		$MYG < $spawnrelated
	echo "User Related Tables"
	for userrelated in $(ls ..sql/user_related/*.sql);do
		$MYG < $userrelated
	echo "7Sings Related Tables"
	for 7srelated in $(ls ..sql/user_related/7signs/*.sql);do
		$MYG < $7srelated
	echo "Character Related Tables"
	for charrelated in $(ls ..sql/user_related/character_data/*.sql);do
		$MYG < $charrelated
	echo "Clan Related Tables"
	for clanrelated in $(ls ..sql/user_related/clan_data/*.sql);do
		$MYG < $clanrelated
	echo "Community Board Related Tables"
	for cbrelated in $(ls ..sql/user_related/community_board/*.sql);do
		$MYG < $cbrelated
	echo "Olympiad Related Tables"
	for olyrelated in $(ls ..sql/user_related/olympiad_data/*.sql);do
		$MYG < $olyrelated
	echo "Territory Related Tables"
	for territoryrelated in $(ls ..sql/user_related/territory_war/*.sql);do
		$MYG < $territoryrelated
	echo "Transaction Related Tables"
	for traderelated in $(ls ..sql/user_related/transactions/*.sql);do
		$MYG < $traderelated
	echo "Custom Tables"
	for custom in $(ls ..sql/custom/*.sql);do
		$MYG < $custom
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