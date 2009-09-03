#!/bin/sh

echo
echo --- Terasort example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/../env.sh
PROACTIVE=$workingDir/../..

if [ "$#" -eq "0" ] ; then
	XMLDESCRIPTOR=${workingDir}/GCMA.xml
else
	XMLDESCRIPTOR="$0"
	shift 
fi

$JAVACMD  -Dgcmdfile=${GCMD} -Dos=unix org.objectweb.proactive.examples.terasort.TeraSort $workingDir/GCMA.xml "$@"

echo
echo ------------------------------------------------------------
