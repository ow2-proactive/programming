#!/bin/sh

echo
echo --- Hello World Web Service ---------------------------------------------

workingDir=`dirname $0`
. ${workingDir}/../env.sh

HTTP_OPT=""
if [ $# -eq 0 ] ; then
	echo "Webservices will be deployed in the embedded Jetty instance on port 8080"
	HTTP_OPT="-Dproactive.http.port=8080"	
else
	echo "Webservices will be deployed in an external servlet container at $@ "
fi

echo
echo

$JAVACMD $HTTP_OPT org.objectweb.proactive.examples.webservices.helloWorld.HelloWorld "$@"

echo
echo ------------------------------------------------------------
