#!/bin/bash

if [[ "$#" -ne 2 ]]; then
    echo Usage: $0 WSDL_DIR OUTPUT_DIR
    exit 0
fi

CUR_DIR=$(pwd)
VERSION="4.2.11"

for d in $1/* ; do
    if [[ -d "$d" && ! -L "$d" ]]; then
        echo "Processing $d"

        PACKAGE=${d##*/}
        PACKAGE=${PACKAGE//-/_}
        OUTPUT="/tmp/xtr$(date +%s)"

        mkdir -p ${OUTPUT}/src/main/
        
 	cp -r ../xtr/src/main/resources ${OUTPUT}/src/main/	

        cd ${OUTPUT}

cat << EOF | tee pom.xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"><modelVersion>4.0.0</modelVersion><packaging>jar</packaging><parent><groupId>com.nortal.jroad</groupId><artifactId>xtee-root</artifactId><version>${VERSION}</version></parent><artifactId>xtee-requests-$PACKAGE</artifactId><dependencies><dependency><groupId>com.nortal.jroad</groupId><artifactId>xtee-typegen</artifactId><version>\${project.version}</version><scope>compile</scope></dependency><dependency><groupId>ee.ria.xtr_2_0</groupId><artifactId>xgen</artifactId><version>1.0.0-SNAPSHOT</version></dependency></dependencies><build>
<plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.8.1</version><configuration><release>11</release></configuration><dependencies><dependency><groupId>org.ow2.asm</groupId><artifactId>asm</artifactId><version>6.2</version></dependency></dependencies></plugin><plugin><groupId>org.codehaus.mojo</groupId><artifactId>exec-maven-plugin</artifactId><version>1.1.1</version><executions><execution><id>x-gen-classes</id><phase>generate-sources</phase><goals><goal>java</goal></goals><inherited>false</inherited><configuration><mainClass>com.nortal.jroad.typegen.TypeGen</mainClass><arguments><argument>wsdldir=$d</argument><argument>sourcedir=\${basedir}/src/main/java</argument><argument>xsbdir=\${basedir}/src/main/resources</argument><argument>basepackage=ee.ria.xtr_2_0.client.types.${PACKAGE}</argument><argument>dbclassespackage=ee.ria.xtr_2_0.client.database.${PACKAGE}</argument></arguments></configuration></execution><execution><id>x-gen-conf</id><phase>process-sources</phase><goals><goal>java</goal></goals><inherited>false</inherited><configuration><mainClass>ee.ria.xtr_2_0.xgen.metadata.MetadataReader</mainClass><arguments><argument>\${basedir}/src/main/resources</argument></arguments></configuration></execution></executions></plugin></plugins></build></project>
EOF

        mvn -e -X package || { rm -rf ${OUTPUT}; echo "Generating configuration failed"; exit 1; }

        echo "Moving jar to $2"

        mv target/*${PACKAGE}-${VERSION}.jar $2

        echo "Moving configuration files to $2"

        mv *.yaml $2

        cd ${CUR_DIR}

        echo "Removing tmp directory"

#        rm -rf ${OUTPUT}
    fi
done
