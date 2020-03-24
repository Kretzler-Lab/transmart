#!/bin/bash

# ********************************************************************************
# This script checks for and reports incompatible version numbers in the 
# linux command lines that are needed for the tranSMART install and data loading
# ********************************************************************************

# # ------------------ source helper function -------------------
. ./versionCompare.sh

# ---------------------------
# Check the version of those command line element that need specific versions
# ---------------------------

echo "-------------------------------------"
echo "|  Checking for incompatible version of basic command-line tools;"
echo "|  If any problems are reported, then recheck the instructions, "
echo "|  and install or re-installing the missing items"
echo "-------------------------------------"

returnFlag=0
# check java version, 1.8 or higher
desiredjavaVersion="1.8"
javaVersion=$(java -version 2>&1 | awk -F '"' '{print $2}')
reportCheckOrHigher "java" $desiredjavaVersion $javaVersion

let "returnFlag=$returnFlag + $?"

# check php version, 7.2 or higher
desiredPhpVersion="7.2"
phpVersion=$(php --version | awk -F '^PHP ' '{print $2}' | awk -F 'ubuntu' '{print $1}')
reportCheckOrHigher "php" $desiredPhpVersion $phpVersion

let "returnFlag=$returnFlag + $?"

# check psql version, 9.2 or higher
desiredPsqlVersion="9.2"
version=$(psql --version)
psqlVersion=$( echo "$version" | awk -F '^psql .PostgreSQL. ' '{print $2}')
reportCheckOrHigher "psql" $desiredPsqlVersion $psqlVersion

let "returnFlag=$returnFlag + $?"

# check groovy version, 2.1 or higher
desiredGroovyVersion="2.1"
groovyVersion=$(groovy --version | awk -F '^Groovy Version: ' '{print $2}')
reportCheckOrHigher "groovy" $desiredGroovyVersion $groovyVersion

let "returnFlag=$returnFlag + $?"

# check grails version, exactly 2.3.11
desiredGrailsVersion="2.3.11"
grailsVersion=$(grails --version | awk -F '^Grails version: ' '{print $2}')
reportCheckExact "grails" $desiredGrailsVersion $grailsVersion

let "returnFlag=$returnFlag + $?"

exit $returnFlag
