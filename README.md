# ServerClient
Chat app between two users; using sockets and simple GUI.

App requires [maven](https://maven.apache.org/download.cgi) v3.6+ to run
and [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) v8.x+ installed.

## Installation
Just clone all project from repos using
```sh
git clone https://github.com/slvn1shh/ServerClient
```
To update project use
```$xslt
git checkout && git pull
```
## Run
### Windows guide
Prepare your system first. Execute:
```
setx JAVA_HOME "%path to JDK%\jdk-11.x.x"
setx M2_HOME "%path to maven%\maven-3.x.x"
setx PATH "%PATH%;%JAVA_HOME%\bin;%M2_HOME%\bin;"
```
where: 
- **%path to JDK%** - path to your installed JDK
- **%path to maven%** - path to your maven folder

Open command prompt in "buildserverandseveralclients" folder
- To build and run server part, use:
```
mvn compile && mvn exec:java -Dexec.mainClass="com.serverclientapp.ChatServer"
```
- To run client part, use:
```
mvn exec:java -Dexec.mainClass="com.serverclientapp.ChatClient"
```
