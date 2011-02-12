@echo off
color 17
title L2EmuProject-Game: Classical Console
:start
echo Starting L2EmuProject-Game: Classical style...
echo.

SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

REM -------------------------------------
REM Default parameters for a basic server.
java -server -Xmn128m -Xms512m -Xmx1024m net.l2emuproject.gameserver.L2GameServer
REM
REM For debug purpose (for devs), use this :
REM java -Xmx512m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=7456 net.l2emuproject.gameserver.L2GameServer 
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Administrator Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormally ...
echo.
:end
echo.
echo Server is terminated ...
echo.
pause
