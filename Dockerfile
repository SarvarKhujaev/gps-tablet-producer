FROM openjdk:17-jdk-alpine
COPY ./target/*.jar /app.jar
ENTRYPOINT ["java"]
CMD ["-jar", "/app.jar"]