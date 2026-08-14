# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy wrapper + pom first so dependency resolution is cached as its own
# layer and doesn't re-run just because source files changed.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/cognit-backend-*.jar app.jar
# file.upload-dir defaults to a relative "uploads" folder under the
# working directory — it needs to exist and be writable by the
# non-root user before LocalFileStorageServiceImpl creates it at boot.
RUN mkdir -p /app/uploads && chown -R spring:spring /app
USER spring

EXPOSE 8080

# JAVA_OPTS lets you pass extra flags (e.g. -Xmx400m on a memory-capped
# host) via an env var without rebuilding the image.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
