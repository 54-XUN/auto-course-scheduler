@echo off
setlocal
set JAVA_HOME=E:\Tools\jdk-17
set MAVEN_HOME=E:\Tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
%JAVA_HOME%\bin\java.exe %*
