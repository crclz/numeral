FROM adoptopenjdk:11-jre-openj9 AS base
WORKDIR /app
EXPOSE 80
EXPOSE 443

FROM base AS final
WORKDIR /app
COPY target/*.jar app.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod u+x wait-for-it.sh
ENTRYPOINT ["./wait-for-it.sh","mysql:3306", "-s", "-t", "60","--","java","-jar","app.jar"]