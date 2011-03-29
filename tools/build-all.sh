#!/bin/bash

##############################################
# Configure this, if you don't have 'svn' in the path!
PATH=${PATH}:/home/lord_rex/maven/apache-maven-2.2.1/bin

MAVEN_OPTS="-Xms64m -Xmx256m"

# Configure this, if you don't have 'mvn' in the path!
MAVEN="mvn"
##############################################

echo ""
cd ..
cd L2EmuProject-Commons
$MAVEN clean:clean install -Dmaven.test.skip=true
cd ..
cd L2EmuProject-Login
$MAVEN clean:clean assembly:assembly -Dmaven.test.skip=true
cd ..
cd L2EmuProject-Game
$MAVEN clean:clean install assembly:assembly -Dmaven.test.skip=true
cd ..
cd L2EmuProject-DataPack
$MAVEN clean:clean assembly:assembly -Dmaven.test.skip=true
cd ..
cd tools
echo ""
echo "Sources compiled, and dependencies installed to the local repository."