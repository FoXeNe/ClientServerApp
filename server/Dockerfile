FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/server-all.jar ./server.jar
EXPOSE 9090
CMD ["java", "-jar", "server.jar"]
