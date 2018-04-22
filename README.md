[![Build Status](https://travis-ci.org/meltzg/EdHD.svg?branch=master)](https://travis-ci.org/meltzg/EdHD)

# EdHD
An application for creating and completing Hadoop MapReduce based assignemnts

## Requirements
* Google Chrome
* Postgres with password authentication
* Java JDK 8
* Hadoop 3.0.0
** Instructions for creating a single node cluster can be found [here](https://hadoop.apache.org/docs/r3.0.0/hadoop-project-dist/hadoop-common/SingleCluster.html)

### Notes on Hadoop requirements
* EdHD must be installed allongside a valid installation of Hadoop.  The Hadoop installation can be configured to point to a remote cluster, but EdHD shoudl be able to resolve all ot the nodes.  EdHD has only been tested running on the Master node.
* The Hadoop binaries must also be on the path.

## Running

1) Download or clone this repository
2) Ensure Hadoop binaries are on the path (execute `hadoop classpath`)
3) Make environment specific changes to [application.yml](./src/main/resources/application.yml)
    * You can optionally make your own application.yml external to this and use it at run time
4) Run `./gradlew clean build`.  This will create an executable `war` in build/libs
5) `cd build/libs`
6) `java -jar edhd-0.0.1-SNAPSHOT.war`
    * If using an external yml file, run `java -jar edhd-0.0.1-SNAPSHOT.war --spring.config.location=classpath:file:///path/to/your/application.yml`
7) In Chrome, navigate to localhost:8080 (or whatever port you configured the app to run on)
    
## Using EdHD

### User management
When you fist navigate to EdHD, you will see the sign in/ registration screen.

![sign in or sign up](./img/login-screen.PNG)

From here, you can login if you've already signed up, or you can register a as a new user.

#### Admin Privileges
When a user signs up, they do not have admin privileges.  Admin privileges are required to:
* Create/Edit/Delete assignments
* Download submissions
* Upload/delete files via the HDFS browser.

To add or remove admin privileges to or from your account:
1) Login
2) Click the user icon in the top right
3) Click User Settings

![User Menu](./img/user-menu.PNG)

4) Toggle `Is Administrator` and enter the EdHD administrative password configured in `application.yml`
5) Click `UPDATE ADMIN SETTINGS`

![User Settings](./img/user-settings.PNG)

### HDFS Browser
EdHD includes a built-in HDFS browser.  Once logged in, it can be accessed from the left side bar.

![HDFS Browser Layout](./img/hdfs-browser.PNG)

Explanation:
1) Displays the current location shown in the HDFS browser
2) Creates a new folder under the current location 
    * Requires admin privileges
3) Uploads a file under the current location in HDFS
    * Requires admin privileges
    * Limit of 2GB (can be changed by changing the value of `spring.http.multipart.max-file-size`)
4) Navigation helpers
    * Go up one level
    * Refresh the current view
5) HDFS File Actions
    * Recursively delete entry (Requires admin privileges)
    * Download file 
6) HDFS Navigation - click to navigate to location
    * If location is a directory, the HDFS browser will go there
    * if location is a file, a preview will be displayed in section 7
7) File preview displays the first `edhd.hadoop.hdfsFilePreview` lines of a file in HDFS

### Assignments
The assignment screen displays information about assignments registered with EdHD, allows for creating assignment submissions, and viewing submission status information.
 