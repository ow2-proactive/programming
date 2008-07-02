#!/bin/sh

echo
echo --- JMX Show OS ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh



$JAVACMD org.objectweb.proactive.examples.jmx.ShowOS

echo
echo ------------------------------------------------------------
