FROM openjdk:8
EXPOSE 8081
COPY ./indigo-backend.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar", "--spring.config.name=application-docker"]