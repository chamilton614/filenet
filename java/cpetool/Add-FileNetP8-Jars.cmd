@echo off
title Add FileNet P8 Jars to Local Maven
cls

REM mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>
REM mvn install:install-file -Dfile=<path-to-file> -DpomFile=<path-to-pomfile>

REM Set the FileNet P8 Jars Root Path
REM e.g. set FileNetP8JarsRoot=/home/chamilton/development/FileNet-P8-Jars
set FileNetP8JarsRoot=FileNet-P8-Jars

REM Set the FileNet P8 Version
set FileNetP8Version=5.2.1.2

REM =================================================
REM Add FileNet P8 Jars to Local Maven Repository
REM =================================================

REM Add to the local maven repository
set GroupId=com.filenet.p8.pe
set JarParentDir=PEEventExportAPI
set JarName=eeapi
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCEWSclient
set JarName=isannotvalidator
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCEWSclient
set JarName=Jace
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCompatibilityLayer64bit
set JarName=javaapi
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCompatibilityLayer64bit
set JarName=listener
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCEWSclient
set JarName=log4j-1.2.14
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCEWSclient
set JarName=p8cel10n
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCompatibilityLayer64bit
set JarName=p8cjares
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.pe
set JarParentDir=PEJavaAPI
set JarName=pe
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.pe
set JarParentDir=PEJavaAPI
set JarName=pe3pt.jar
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.pe
set JarParentDir=PEJavaAPI
set JarName=peResources
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.pe
set JarParentDir=PERESTAPI
set JarName=peREST
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCEWSclient
set JarName=stax-api
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCEWSclient
set JarName=xlxpScanner
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar

REM Add to the local maven repository
set GroupId=com.filenet.p8.ce
set JarParentDir=JavaCEWSclient
set JarName=xlxpScannerUtils
mvnw install:install-file -Dfile=%FileNetP8JarsRoot%/%FileNetP8Version%/%JarParentDir%/%JarName%.jar -DgroupId=%GroupId% -DartifactId=%JarName% -Dversion=%FileNetP8Version% -Dpackaging=jar


