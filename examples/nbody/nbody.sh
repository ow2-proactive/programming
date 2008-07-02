#!/bin/sh

echo
echo --- N-body with ProActive ---------------------------------

#if [ -z "$PROACTIVE" ]
#then
#workingDir=`dirname $0`
#PROACTIVE=$workingDir/../../.
#CLASSPATH=.
#fi
#. $PROACTIVE/examples/env.sh

workingDir=`dirname $0`
. $workingDir/../env.sh

if [ "$1" = "-displayft" -o "$1" = "-3dft" ]
then
	export XMLDESCRIPTOR=$workingDir/GCMA_FaultTolerance.xml
else
	export XMLDESCRIPTOR=$workingDir/GCMA.xml
fi


$JAVACMD org.objectweb.proactive.examples.nbody.common.Start $XMLDESCRIPTOR "$@"

echo
echo ---------------------------------------------------------
