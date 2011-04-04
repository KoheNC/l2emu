@echo off

color 17
title L2Emu Project: Database Installer

REM ############################################
REM ## You can change here your own DB params ##
REM ############################################
REM MYSQL BIN PATH
set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 5.5\bin

REM LOGINSERVER
set lsuser=root
set lspass=
set lsdb=l2emu_db
set lshost=localhost

REM GAMESERVER
set gsuser=root
set gspass=
set gsdb=l2emu_db
set gshost=localhost
REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"

echo PLEASE EDIT THIS SCRIPT SO VALUES IN THE CONFIG SECTION MATCH YOUR DATABASE(S)
echo.
echo.
echo Making a backup of the original loginserver database.
%mysqldumpPath% --add-drop-table -h %lshost% -u %lsuser% --password=%lspass% %lsdb% > loginserver_backup.sql
echo.
echo                ** [ L2EMU PROJECT INTERACTIVE DATABASE INSTALLER ] **
echo                         ** [ LOGINSERVER DATABASE ] **
echo.
echo PLEASE SELECT YOUR INSTALL TYPE :
echo.
echo OPTIONS: 
echo.
echo          FULL INSTALL (F) FOR A COMPLETE LOGINSERVER INSTALL.
echo.         
echo          SKIP (S) TO SKIP LOGINSERVER DATABASE INSTALL.          
echo.
echo          QUIT (Q) TO EXIT PROGRAM.
echo.
:asklogin
set loginprompt=x
set /p loginprompt=LOGINSERVER DB install type: (f) full or (s) skip or (q) quit? 
if /i %loginprompt%==f goto logininstall
if /i %loginprompt%==s goto gsbackup
if /i %loginprompt%==q goto end
goto asklogin

:logininstall
echo Deleting all old Login Server datatables...
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login_install.sql
echo.
echo Installing Login Related datatables...
for %%S in (../sql/login_related/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/login_related/%%S
echo.
echo.
echo.
echo LOGINSERVER DATABASE INSTALL COMPLETED !
echo.
echo.
echo.

:gsbackup
echo.
echo Making a backup of the original GameServer database.
%mysqldumpPath% --add-drop-table -h %gshost% -u %gsuser% --password=%gspass% %gsdb% > gameserver_backup.sql

echo.
echo                ** [ L2EMU PROJECT INTERACTIVE DATABASE INSTALLER ] **
echo                         ** [ GAMESERVER DATABASE ] **
echo.
echo PLEASE SELECT YOUR INSTALL TYPE :
echo.
echo OPTIONS:
echo.
echo          FULL INSTALL (F) FOR A COMPLETE GAMESERVER INSTALL.
echo.           
echo          UPGRADE INSTALL (U) FOR A GAMESERVER UPGRADE INSTALL.
echo.         
echo          SKIP (S) TO SKIP GAMESERVER DATABASE INSTALL. 
echo.
echo          QUIT (Q) TO EXIT THIS PROGRAM.
echo.
echo. 
:asktype
set installtype=x
set /p installtype=GAMESERVER DB install type: (f) full install or (u) upgrade or (q) quit? 
if /i %installtype%==f goto fullinstall
if /i %installtype%==u goto upgradeinstall
if /i %installtype%==q goto end
goto asktype

:fullinstall
echo Deleting all old Game Server datatables...
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < full_install.sql

:upgradeinstall
echo.
echo Installing Server Related datatables...
for %%S in (../sql/server_related/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/%%S
for %%S in (../sql/server_related/castles/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/castles/%%S
for %%S in (../sql/server_related/drops/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/drops/%%S
for %%S in (../sql/server_related/fortresses/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/fortresses/%%S
for %%S in (../sql/server_related/items/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/items/%%S
for %%S in (../sql/server_related/npcs/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/npcs/%%S
for %%S in (../sql/server_related/shops/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/shops/%%S
for %%S in (../sql/server_related/skills/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/skills/%%S
for %%S in (../sql/server_related/spawns/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/server_related/spawns/%%S
echo.
echo.
echo Installing User Related datatables...
for %%S in (../sql/user_related/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/%%S
for %%S in (../sql/user_related/7signs/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/7signs/%%S
for %%S in (../sql/user_related/bosses/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/bosses/%%S
for %%S in (../sql/user_related/character_data/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/character_data/%%S
for %%S in (../sql/user_related/clan_data/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/clan_data/%%S
for %%S in (../sql/user_related/community_board/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/community_board/%%S
for %%S in (../sql/user_related/olympiad_data/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/olympiad_data/%%S
for %%S in (../sql/user_related/territory_war/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/territory_war/%%S
for %%S in (../sql/user_related/transactions/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/user_related/transactions/%%S
echo.
echo.
echo Installing Custom datatables...
for %%S in (../sql/custom/*.sql) do %mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom/%%S
echo.
:end
echo.
echo.
echo.
echo INTERACTIVE DATABASE INSTALLER COMPLETED !
echo.
echo.
echo CONTACTS : http://www.l2emuproject.net/
echo.
echo.
echo POWERED BY L2EMU PROJECT DEVELOPMENT TEAM. 
echo COPYRIGHT 2010-2011 - ALL RIGHTS RESERVED.
echo.
echo Patch by RAYAN and lord_rex. 
pause