FROM adoptopenjdk:11-jre-openj9 AS base
WORKDIR /app
EXPOSE 80
EXPOSE 443

COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]