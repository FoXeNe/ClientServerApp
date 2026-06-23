FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew :client:shadowJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/client/build/libs/*-all.jar ./client.jar
CMD ["java", "-jar", "client.jar"]
