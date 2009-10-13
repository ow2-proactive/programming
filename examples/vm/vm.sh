#!/bin/sh

echo
echo --- VM Example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/../env.sh
PROACTIVE=$workingDir/../..

XMLDESCRIPTOR=${workingDir}/vm-gcma.xml
GCMD=${workingDir}/vm-gcmd.xml


$JAVACMD  -Dgcmdfile=${GCMD} -Dos=unix org.objectweb.proactive.examples.vm.ComputeRandom $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
