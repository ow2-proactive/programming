#!/usr/bin/env bash
# PA dataserver command script
#

workingDir=$(dirname $0)

. $workingDir/env.sh

if [ $# -gt 0 ]; then
	echo "$0 : arguments will be ignored"
fi


CLASS="org.objectweb.proactive.extensions.vfsprovider.gui.ServerBrowser"

$JAVACMD "$CLASS"
