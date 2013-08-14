
Annotation processing rely on the several API:
 - Mirror API (Java 5)
 - Compiler Tree API (Java 6)
 - javax.annotation.processing
 
 The first two APIs are only available trough the tools.jar file.
 
 Since it is not possible to add tools.jar in the classpath 
 in a portable way, we have to exclude the annotation package from the
 default built.
 
 If you want to re-enable it, following steps must be done:
 	- Remove org/objectweb/proactive/extensions/annotation/*/*/** from Sources -> Extensions -> Excluded
 	- Remove functionalTests/annotation/**/* from Sources -> Extensions -> Excluded
 	- Add tools.jar in your classpath
 	
Be warn to NOT commit these changes in the Subversion repository.

./compile/build compile.all WILL compile the annotation package.