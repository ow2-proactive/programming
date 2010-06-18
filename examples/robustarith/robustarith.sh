#!/bin/sh

echo
echo --- Robust Arithmetic -----------------------

if [ -z "$PROACTIVE" ]
then
	workingDir=`dirname $0`
	PROACTIVE=$workingDir/../../.
	CLASSPATH=.
fi

. $PROACTIVE/examples/env.sh

export XMLDESCRIPTOR=$workingDir/GCMA.xml
time $JAVACMD org.objectweb.proactive.examples.robustarith.Main $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
