FROM openjdk:8
EXPOSE 8080
COPY ./indigo-backend.jar /app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod","-jar","/app.jar"]