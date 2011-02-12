@echo off
SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

@java -Djava.util.logging.config.file=console.cfg net.l2emuproject.gsregistering.GameServerRegister

SET CLASSPATH=%OLDCLASSPATH%
@pause