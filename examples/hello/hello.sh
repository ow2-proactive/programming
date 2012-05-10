#!/bin/sh

echo
echo --- Hello World example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/../env.sh
PROACTIVE=$workingDir/../..
echo -n "Do you want to use a Local or remote descriptor file ? Simplest is local [L/R] "
read ans

XMLDESCRIPTOR=${workingDir}/helloApplication.xml
if [ $ans = "R" -o $ans = "r" ]
then
  GCMD=helloDeploymentRemote.xml
else
  GCMD=helloDeploymentLocal.xml
fi
$JAVACMD -Dgcmdfile=${GCMD} -Dos=unix org.objectweb.proactive.examples.hello.Hello $XMLDESCRIPTOR


echo
echo ------------------------------------------------------------
