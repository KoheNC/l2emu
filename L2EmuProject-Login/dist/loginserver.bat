@echo off
color 17
title L2EmuProject-Login Classical Console
:start
echo Starting L2EmuProject-Login: Classical style...
echo.

SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

java -Dfile.encoding=UTF-8 -Xmx64m net.l2emuproject.loginserver.L2LoginServer

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
