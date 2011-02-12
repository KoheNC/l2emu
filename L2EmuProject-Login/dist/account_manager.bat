@echo off
SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

@java -Djava.util.logging.config.file=console.cfg net.l2emuproject.accountmanager.AccountManager

SET CLASSPATH=%OLDCLASSPATH%
@pause
