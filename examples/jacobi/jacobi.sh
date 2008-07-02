#!/bin/sh

echo
echo --- Jacobi : nodes initialization -----------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh
export XMLDESCRIPTOR=$workingDir/GCMA.xml
$JAVACMD org.objectweb.proactive.examples.jacobi.Jacobi $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
