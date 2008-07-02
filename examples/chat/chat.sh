#!/bin/sh

echo
echo --- Chat with ProActive ---------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD org.objectweb.proactive.examples.chat.Chat "$@"

echo
echo ---------------------------------------------------------
