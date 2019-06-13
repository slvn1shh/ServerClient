@echo off

rem set JAVA_HOME=%cd%\tools\jdk-12.0.1
rem set M2_HOME=%cd%\tools\maven-3.6.1
rem set PATH="%PATH%;%JAVA_HOME%\bin;%M2_HOME%\bin;

mvn compile && mvn exec:java -Dexec.mainClass="com.serverclientapp.ChatClient"