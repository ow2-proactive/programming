#!/bin/sh

echo
echo --- User Guide: Initialized CMAgent ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD org.objectweb.proactive.examples.userguide.cmagent.initialized.Main

echo
echo ------------------------------------------------------------
