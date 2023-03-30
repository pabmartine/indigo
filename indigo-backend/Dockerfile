FROM openjdk:11.0.11-jre-slim
EXPOSE 8080
COPY ./indigo-backend.jar /app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Djdk.tls.client.protocols=TLSv1.3,TLSv1.2", "-Dhttps.protocols=TLSv1.3,TLSv1.2", "-jar", "/app.jar"]