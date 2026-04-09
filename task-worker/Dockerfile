# Build stage — compile and package the fat jar
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY common common
COPY task-worker task-worker

RUN ./gradlew :task-worker:bootJar --no-daemon -x test

# Runtime stage — minimal JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/task-worker/build/libs/task-worker-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
