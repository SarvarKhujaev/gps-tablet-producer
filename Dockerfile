FROM openjdk:17-jdk-alpine
ENV TZ=Asia/Tashkent
RUN apk update && apk upgrade && apk add ca-certificates && update-ca-certificates && apk add --update tzdata
RUN rm -rf /var/cache/apk/*
COPY ./target/*.jar /app.jar
ENTRYPOINT ["java"]
CMD ["-jar", "/app.jar"]