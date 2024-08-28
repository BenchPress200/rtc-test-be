FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/video-0.0.1-SNAPSHOT.jar video.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "video.jar"]