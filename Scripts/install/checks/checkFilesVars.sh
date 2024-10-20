#!/bin/bash

# **********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSMART install and data loading
# **********************************************************************************

base="$TMINSTALL_BASE/transmart-data"
filepath="$base/vars"

echo "-------------------------------------"
echo "|  Checking for $filepath"
echo "-------------------------------------"

returnValue=0

if [ ! -e "$filepath" ]; then
	echo "The directory/folder at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildEnv' step of the install; repeat that step"
	returnValue=1
fi
echo "vars file found at $filepath"

exit $returnValue
