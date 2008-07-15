#!/bin/sh

echo
echo --- Master/Worker Basic Prime example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/../env.sh
PROACTIVE=$workingDir/../..


XMLDESCRIPTOR=${workingDir}/GCMA.xml

found=0
for i in $@; do
    if [ $i = '-d' ]; then found=1; fi
done

if [ $found -eq 1 ]; then
    $JAVACMD  org.objectweb.proactive.examples.masterworker.nqueens.NQueensExample  $@
else
    $JAVACMD  org.objectweb.proactive.examples.masterworker.nqueens.NQueensExample -d $XMLDESCRIPTOR $@
fi

echo
echo ------------------------------------------------------------
