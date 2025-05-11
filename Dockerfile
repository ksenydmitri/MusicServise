FROM ubuntu:latest
LABEL authors="kseny_dmitri"

FROM openjdk:17-jdk-alpine
WORKDIR /MusicService
COPY target/music-service-1.0.0.jar /MusicService/music-service-1.0.0.jar
CMD ["java", "-jar", "/app/music-service.jar"]

ENTRYPOINT ["top", "-b"]