#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed PROACTIVE
#

CLASSPATH=.

# User envrionment variable
if [ ! -z "$PROACTIVE_HOME" ] ; then
	PROACTIVE=$PROACTIVE_HOME
fi


# Internal ProActive scripts can override $PROACTIVE
if [ -z "$PROACTIVE" ]
then
	workingDir=`dirname $0`
       PROACTIVE=$(cd $workingDir/../.././ || (echo "Broken PROACTIVE installation" ; exit 1) && echo $PWD)
fi


# ----------------------------------------------------------------------------


JAVA_HOME=${JAVA_HOME-NULL};
if [ "$JAVA_HOME" = "NULL" ]
then
echo
echo "The enviroment variable JAVA_HOME must be set the current jdk distribution"
echo "installed on your computer."
echo "Use "
echo "    export JAVA_HOME=<the directory where is the JDK>"
exit 127
fi

# ----
# Set up the classpath using jar files
#
# Since ProActive 4.1.0 classes are no longer used
CLASSPATH=$CLASSPATH:$PROACTIVE/dist/lib/ProActive.jar
CLASSPATH=$CLASSPATH:$PROACTIVE/dist/lib/ProActive_examples.jar
CLASSPATH=$CLASSPATH:$PROACTIVE/dist/lib/ibis-1.4.jar:$PROACTIVE/dist/lib/ibis-connect-1.0.jar:$PROACTIVE/dist/lib/ibis-util-1.0.jar

#echo "CLASSPATH"=$CLASSPATH
export CLASSPATH


JAVACMD=$JAVA_HOME/bin/java"\
	-Djava.security.manager \
	-Djava.security.policy=$PROACTIVE/examples/proactive.java.policy \
	-Dlog4j.configuration=file:${PROACTIVE}/examples/proactive-log4j \
	-Dproactive.home=$PROACTIVE \
	-Dos=unix"

export PROACTIVE
echo $PROACTIVE
export JAVACMD
