#!/bin/sh

echo
echo --- TimIt --------------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

cd ${workingDir}

$JAVACMD org.objectweb.proactive.extensions.timitspmd.TimIt -c config.xml

echo
echo ------------------------------------------------------------
