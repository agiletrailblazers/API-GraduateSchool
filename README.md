# API-GraduateSchool
This is the source repository for the REST API Application layer used by the Graduate School Website.

## Core Application Technologies
* Java 8.x.x
* Spring 4.x.x
* JDBC/SQL
* Apache Tomcat 8.x.x
* Apache Maven 3.x.x

## Core Testing Technologies
* [JUnit](http://junit.org/) (Unit Testing Framework)
* [Mockito](http://mockito.org/) (Unit Testing / Mocking Framework)
* [PowerMock](https://github.com/jayway/powermock) (Unit Testing / Mocking Framework)
* [Postman](https://www.getpostman.com/) (REST API Integration Test Framework)
* [Newman](https://www.npmjs.com/package/newman) (Command-line utility for Postman)

## Installation
1. Download and install Maven
2. Clone the remote API-GraduateSchool repository to your local development directory
3. Change directories to the root application directory: `cd {your-dev-dir}/API-GraduateSchool`
4. Install the required Oracle JDBC drivers using the following Maven command
    `mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0.5 -Dpackaging=jar -Dfile=src/lib/ojdbc14.jar -DgeneratePom=true`

## Apache Tomcat Configuration
1. Required configuration for CyberSource, the 3rd party payment system used by the new registration functionality.
    1. Create a directory (it can be anywhere), or use an existing if you would prefer, and download the CyberSource test certificate (evalgraduateschool.p12) from http://confluence.agiletrailblazers.com/display/GS/Cybersource+Simple+Order+API+Security+Certificates.
    2. Add the following Tomcat VM argument to specify the location of the CyberSource directory
    `-DcybersourceDir=fullPathToCyberSourceDir`
    3. Update the version of Java that is being used by Tomcat with the security jars that support the type of key used by CyberSource.  Replace [JAVA_HOME]/jre/lib/security/local_policy.jar and [JAVA_HOME]/jre/lib/security/US_export_policy.jar with the versions that can be downloaded from http://confluence.agiletrailblazers.com/display/GS/Cybersource+Simple+Order+API+Security+Certificates
2. Required configuration to specify which environment configuration should be used
    * Dev:  `-DpropertiesLoc=/properties/dev`
    * Test: `-DpropertiesLoc=/properties/test`

## Building the Application
The application is built using Maven.  The following command will build the application and execute all of the JUnit tests.
All unit tests must pass and the minimum unit test code coverage of 95% must be met in order for a build to be successful.
All unit tests must be passing before you submit a pull request to merge any changes. 

`mvn clean install`

## Installation and Execution of Postman Integration Tests
A Postman collection is included in the repository and must be maintained and enhanced as functionality is implemented and/or changed.
All Postman tests must be passing before you submit a pull request to merge any changes. 

### Using the Postman Client
1. Download and install the Postman client
2. Import the test collection from this file
    * {your-dev-dir}/API-GraduateSchool/src/doc/GraduateSchool-API.json.postman_collection
3. Import the supported environments from these files
    * {your-dev-dir}/API-GraduateSchool/src/doc/dev.postman_environment
    * {your-dev-dir}/API-GraduateSchool/src/doc/local.postman_environment
4. Select the appropriate environment and execute the collection of tests

### Using Newman from the command-line
1. Install Newman using the Node Package Manager (npm)
    * To install newman (for use with Node 4.0+)
`npm install -g newman@beta`
    * To install newman (for use with older versions of Node)
`npm install -g newman`
2. Execute the Postman collection of tests by running the following command
`cd src/doc`
`newman -c GraduateSchool-API.json.postman_collection -e local.postman_environment`
