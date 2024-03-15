FROM eclipse-temurin:11-jdk as build

ARG WORKDIR="/workspace/app"
ARG PROJECT_DIR="xtee-samples"

WORKDIR ${WORKDIR}

RUN apt update && apt upgrade -y
RUN apt install maven -y

COPY pom.xml .
COPY common common
COPY xgen xgen
COPY ${PROJECT_DIR} ${PROJECT_DIR}
COPY xtr xtr
COPY conf conf

RUN mvn -q clean install
RUN mvn -q package -Pservices


FROM tomcat:9 as run

ARG WORKDIR="/workspace/app"
ARG PROJECT_DIR="xtee-samples"

WORKDIR ${WORKDIR}

COPY pom.xml .
COPY common common
COPY xgen xgen
COPY ${PROJECT_DIR} ${PROJECT_DIR}
COPY xtr xtr
COPY conf conf

RUN mkdir -p ${WORKDIR}/xtr/services
COPY --from=build ${WORKDIR}/${PROJECT_DIR}/target/${PROJECT_DIR}*/* ${WORKDIR}/xtr/services/

COPY --from=build ${WORKDIR}/xtr/target/xtr-1.0.0-SNAPSHOT.war /usr/local/tomcat/webapps/xtr.war

WORKDIR ${WORKDIR}/xtr


RUN cp -r ${WORKDIR}/xtr/services /usr/local/tomcat/
RUN cp ${WORKDIR}/xtr/conf/context.xml /usr/local/tomcat/conf/

#RUN mvn -q spring-boot:repackage
#ENTRYPOINT ["mvn","spring-boot:run"]
EXPOSE 8080
#ENTRYPOINT ["/opt/java/openjdk/bin/java", "-Dloader.path=/workspace/app/xtr/services", "-jar", "target/xtr-1.0.0-SNAPSHOT.war"]
CMD ["catalina.sh", "run"]
