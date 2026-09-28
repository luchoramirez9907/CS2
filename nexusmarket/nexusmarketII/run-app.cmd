@echo off
rem Runs NexusMarket with demo seed data and in-memory persistence on port 8090
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo,memory "-Dspring-boot.run.jvmArguments=-Dserver.port=8090"
