# API-GraduateSchool
Java/Spring API Layer for Graduate School

<h2>Runs on</h2>
- Java 8
- Apache Tomcat 7.0.62

<h2>Running API on Apache Tomcat</h2>
- Add the following Tomcat argument to pick the right properties file<br/>
-- For Dev:  ```-DpropertiesLoc=/properties/dev```<br/>
-- For Prod: ```-DpropertiesLoc=/properties/prod```<br/>
- To run in a standalone Tomcat (Linux) add a file “”setenv.sh” to the /conf folder with:</br>
```
JRE_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_20.jdk/Contents/Home/jre
CATALINA_OPTS="-DpropertiesLoc=properties/dev"
```
NOTE: JRE_HOME is optional but if you are running multiple Java version this is a way to handle it.

<h2>Maven command to build<h2>
```
> mvn eclipse:clean clean -U install eclipse:eclipse -Dwtpversion=2.0
```
