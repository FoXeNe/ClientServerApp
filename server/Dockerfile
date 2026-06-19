FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew :server:shadowJar --no-daemon
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/server/build/libs/*-all.jar ./server.jar
EXPOSE 45205
CMD ["java", "-jar", "server.jar"]
