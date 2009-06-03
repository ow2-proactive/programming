#!/bin/sh

echo
echo --- User Guide: Simple CMAgent ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD org.objectweb.proactive.examples.userguide.cmagent.simple.Main

echo
echo ------------------------------------------------------------
