#!/bin/sh

echo
echo --- Monte/Carlo European Option example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/../env.sh
PROACTIVE=$workingDir/../..


XMLDESCRIPTOR=${workingDir}/GCMA.xml

found=0
for i in $@; do
    if [ $i = '-d' ]; then found=1; fi
done

if [ $found -eq 1 ]; then
    $JAVACMD  org.objectweb.proactive.extra.montecarlo.example.EuropeanOption  $@
else
    $JAVACMD  org.objectweb.proactive.extra.montecarlo.example.EuropeanOption -d $XMLDESCRIPTOR $@
fi

echo
echo ------------------------------------------------------------
