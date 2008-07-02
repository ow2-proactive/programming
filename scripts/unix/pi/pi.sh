#!/bin/sh

workingDir=.

PROACTIVE=$workingDir/../../../.
. $PROACTIVE/scripts/unix/env.sh


$JAVACMD -Xmx256000000 -classpath $JAVA_HOME/lib/tools.jar:$PROACTIVE/compile/lib/ant.jar:$PROACTIVE/compile/lib/ant-launcher.jar:$PROACTIVE/lib/ws/xml-apis.jar:$PROACTIVE/lib/xercesImpl.jar org.apache.tools.ant.Main -buildfile $PROACTIVE/src/Examples/org/objectweb/proactive/examples/pi/scripts/build.xml "$@"


