ProActive OSGi Example 

before being able to run that example, it is mandatory to create the
ProActive's OSGi bundle that has to be deployed within the OSGi
platform. 

In order to create these bundles, you have to go within the compile/
directory located at the root directory of the ProActive installation 
build target called 'OSGiBundles'.

This is done by going under the compile directory and launching :
 # build[.bat] OSGiBundles

The result of this command is the creation of a directory called
bundle under the dist/ located at the root of the ProActive
installation. This directory contains 3 jar files : 
  - OSGiManagement.jar
  - librariesBundle.jar 
  - proactiveConnectorBundle.jar 

When this is done, the script oscar.[bat|sh] can be run
