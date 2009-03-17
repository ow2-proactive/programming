#!/bin/sh

echo
echo --- Scilab GUI ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/../env.sh
. $workingDir/scilab_env.sh

$JAVACMD -Djava.library.path=$LD_LIBRARY_PATH org.objectweb.proactive.examples.scilab.gui.MSFrame

echo
echo ------------------------------------------------------------
