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

RUN mkdir ${WORKDIR}/xtr/services
RUN cp ${WORKDIR}/${PROJECT_DIR}/target/${PROJECT_DIR}*/* ${WORKDIR}/xtr/services/

WORKDIR ${WORKDIR}/xtr
ENTRYPOINT ["mvn","spring-boot:run"]
