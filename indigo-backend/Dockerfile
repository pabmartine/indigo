FROM openjdk:8
EXPOSE 8081
COPY ./indigo-backend.jar /app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod","-jar","/app.jar"]
