#!/bin/bash

# ********************************************************************
# This script checks for and reports missing items in the context of R
# ********************************************************************

# # ------------------ helper function -------------------
. $TMSCRIPTS_BASE/checks/versionCompare.sh

base="$TMINSTALL_BASE/transmart-data"
pathForRBin="$base/R/root/bin"

echo "---------------------------------------------------------------"
echo "|  Checking for basics and packages required by R;"
echo "|    if anything is reported as missing, then recheck"
echo "|    the detailed instructions for installing the missing items"
echo "---------------------------------------------------------------"

echo "Checking for R bin on path"
pathToExecutable=$(which R)
if [ -x "$pathToExecutable" ] ; then
    echo "It's here: $pathToExecutable"
else
    echo "Warning: R not on path; setting PATH temporarily to perform checks."
    echo "Add to PATH: $pathForRBin"
    export PATH=$pathForRBin:$PATH
    pathToExecutable=$(which R)
    if [ -x "$pathToExecutable" ] ; then
        echo "It's here: $pathToExecutable"
    else
        echo "The R command is not reachable"
        echo "Check details of install step for installing R"
        echo "Checking can not continue."
        echo 1
    fi
fi

# check R version, exactly 4.1.2
desiredRVersion="4.1.2"
RVersion=$(R --version | awk -F '^R version ' '{print $2}')
reportCheckExact "R" $desiredRVersion $RVersion
returnFlag=$?
if [ "$returnFlag" -eq 1 ]; then
    echo "R version problems; aborting check of R and Rpackages"
    exit 1
fi

R --vanilla --slave < probeRserve.R > /dev/null
results=$?

if (( ! $results )); then
    echo "OK; Rserve and the other packages required appear to be available"
else
    echo "One or more required package(s) is missing from R;"
    echo "  see probeRserve.R for a method of checking the details."
fi 

exit $results
