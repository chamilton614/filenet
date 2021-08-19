#!/bin/sh

# mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>
# mvn install:install-file -Dfile=<path-to-file> -DpomFile=<path-to-pomfile>

# Set the FileNet P8 Jars Root Path
# e.g. set FileNetP8JarsRoot=/home/chamilton/development/FileNet-P8-Jars
FileNetP8JarsRoot=FileNet-P8-Jars

# Set the FileNet P8 Version
FileNetP8Version=5.2.1.2

# =================================================
# Add FileNet P8 Jars to Local Maven Repository
# =================================================

# Add to the local maven repository
GroupId=com.filenet.p8.pe
JarParentDir=PEEventExportAPI
JarName=eeapi
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCEWSclient
JarName=isannotvalidator
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCEWSclient
JarName=Jace
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCompatibilityLayer64bit
JarName=javaapi
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCompatibilityLayer64bit
JarName=listener
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCEWSclient
JarName=log4j-1.2.14
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCEWSclient
JarName=p8cel10n
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCompatibilityLayer64bit
JarName=p8cjares
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.pe
JarParentDir=PEJavaAPI
JarName=pe
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.pe
JarParentDir=PEJavaAPI
JarName=pe3pt
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.pe
JarParentDir=PEJavaAPI
JarName=peResources
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.pe
JarParentDir=PERESTAPI
JarName=peREST
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCEWSclient
JarName=stax-api
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCEWSclient
JarName=xlxpScanner
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar

# Add to the local maven repository
GroupId=com.filenet.p8.ce
JarParentDir=JavaCEWSclient
JarName=xlxpScannerUtils
./mvnw install:install-file -Dfile=${FileNetP8JarsRoot}/${FileNetP8Version}/${JarParentDir}/${JarName}.jar -DgroupId=${GroupId} -DartifactId=${JarName} -Dversion=${FileNetP8Version} -Dpackaging=jar















