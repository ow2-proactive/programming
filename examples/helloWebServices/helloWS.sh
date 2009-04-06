#!/bin/sh

echo
echo --- Hello World Web Service ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

$JAVACMD -Dproactive.http.port=8080 org.objectweb.proactive.examples.webservices.helloWorld.HelloWorld "$@"


echo
echo ------------------------------------------------------------
else
	echo "Webservices will be deployed in an external servlet container at $@ "
fi


echo
echo ------------------------------------------------------------
