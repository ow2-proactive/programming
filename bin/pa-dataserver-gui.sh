#!/usr/bin/env bash
# PA dataserver command script
#

workingDir=$(dirname $0)

. $workingDir/env.sh

CLASS="org.objectweb.proactive.extensions.vfsprovider.gui.ServerBrowser"

$JAVACMD "$CLASS" $@
