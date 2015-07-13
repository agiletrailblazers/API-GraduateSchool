# API-GraduateSchool
Java/Spring API Layer for Graduate School

<h2>Runs on</h2>
- Java 8
- Apache Tomcat 7.0.62

<h2>Maven command to build</h2>
```
> mvn eclipse:clean clean -U install eclipse:eclipse -Dwtpversion=2.0
```

<h2>Running API on Apache Tomcat</h2>
- Add the following Tomcat argument to pick the right properties file<br/>
-- For Dev:  ```-DpropertiesLoc=/properties/dev```<br/>
-- For Prod: ```-DpropertiesLoc=/properties/prod```<br/>
- To run in a standalone Tomcat (Linux) add a file "setenv.sh" to the /conf folder with:</br>
```
JRE_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_20.jdk/Contents/Home/jre
CATALINA_OPTS="-DpropertiesLoc=properties/dev"
```
NOTE: JRE_HOME is optional but if you are running multiple Java version this is a way to handle it.

<h2>API Documentation</h2>
TODO

<h2>Postman</h2>
The APIs can be run using Postman.  A Postman collection is included in the repo. 
- Download Postman Client: https://www.getpostman.com/
- Import Collection from:
```
API-GraduateSchool/src/doc/GraduateSchool-API.json.postman_collection
```
- Run any of the APIs (you must be running locally on port 8080)
