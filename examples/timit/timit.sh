#!/bin/sh

echo
echo --- TimIt --------------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

cd ${workingDir}

$JAVACMD org.objectweb.proactive.benchmarks.timit.TimIt -c config.xml

echo
echo ------------------------------------------------------------
