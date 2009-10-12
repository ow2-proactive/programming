#!/bin/sh
echo
echo --- Expose Dispatcher as a web service  -------------------------------------------

# If no argument is provided, the active object will be deployed
# localhost:8080 by default.
#
#if [ $# -lt 1 ]; then
#    echo "
#       Expose an ActiveObject as a web service
#         c3ddispatcherWS.sh <the url where to deploy the object>
#           ex : c3ddispatcherWS.sh trinidad:8080
#
#    "
#    exit 1
#fi

workingDir=`dirname $0`
. ${workingDir}/../env.sh

export XMLDESCRIPTOR=$workingDir/GCMA_Renderer.xml

JETTY_PORT=-Dproactive.http.port=8080

$JAVACMD $JETTY_PORT org.objectweb.proactive.examples.webservices.c3dWS.C3DDispatcher $XMLDESCRIPTOR "$@"
echo
echo ---------------------------------------------------------
