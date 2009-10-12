ProActive helloWebServices example README file.

This example demonstrates the exposition of an active object or a GCM component as a web
service and how to call it using the axis and cxf libraries.

By default, this example will use a jetty which is launch at the beginning
on the port 8080 and on which services will be deployed. If you want
to use an other application server, just provide the script with its url
(e.g. http://localhost:8081/) as its first argument.

The proactive.war is not build by the default ant target. In order to
have this file created, one has to go under the compile/ directory
located at the root of the ProActive directory and launch the following
command : 
 # build[.bat] proActiveWar[Axis2|CXF]

The result of this command is the creation of a web archive file called
proactive.war inside the dist/ directory located at the root of the
proactive directory. 

If you want to use an application server different from the jetty server,
you have to copy this proactive.war file to the webapp/ directory of your
own server. Otherwise, the jetty will automatically create an axis2 servlet
and take needed files from the dist/ directory.

When this setup step is complete, the first script to run is the
helloWS.[bat|sh] that is going to deploy the example within the
application server. 

Then, the script helloWSCall.[bat|sh] performs the web service call
and displays the results.




