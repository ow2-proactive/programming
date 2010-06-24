#!/bin/sh

echo
echo --- Dataspace--------------------------------------------

workingDir=`dirname $0`
. $workingDir/../env.sh


GCMA=$workingDir/hello/helloApplication.xml
GCMD=helloDeploymentLocal.xml

$JAVACMD -Dgcmdfile=$GCMD org.objectweb.proactive.examples.dataspaces.hello.HelloExample $GCMA

echo
echo ---------------------------------------------------------
