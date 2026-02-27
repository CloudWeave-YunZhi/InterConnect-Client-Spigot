@echo off
set JAVA_HOME=C:\Program Files\Zulu\zulu-21
set PATH=C:\maven\apache-maven-3.9.6\bin;%PATH%
cd /d c:\Users\Administrator\Desktop\IC\InterConnect-Client-Spigot
mvn clean package -DskipTests
