FROM openjdk:11-jdk as build

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

RUN mvn clean install
RUN mvn clean -U package
RUN mvn clean -U package -Pservices

RUN mkdir ./xtr/services
COPY ./${PROJECT_DIR}/target/${PROJECT_DIR}*/* ./xtr/services/

WORKDIR ${WORKDIR}/xtr
ENTRYPOINT ["mvn","spring-boot:run"]