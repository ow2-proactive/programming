ProActive helloWebServices example README file.

This example demonstrates the exposition of an active object as a web
service and how to call it using the axis library.

This example requires to have an application server already running
where the proactive.war file has been deployed.

The proactive.war is not build by the default ant target. In order to
have this file created, one has to go under the compile/ directory
located at the root of the ProActive directory and launch the following
command : 
 # build[.bat] proActiveWar

The result of this command is the creation of a web archive file called
proactive.war inside the dist/ directory located at the root of the
proactive directory. 

This file has then to be deployed under an application server like
tomcat.

When this setup step is complete, the first script to run is the
helloWS.[bat|sh] that is going to deploy the example within the
application server. 

Then, the script helloWSCall.[bat|sh] performs the web service call
and displays the results.




