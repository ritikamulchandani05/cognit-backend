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

# su-exec lets the entrypoint start as root, fix the volume mount's
# ownership, then drop back to the unprivileged user to run the app.
RUN apk add --no-cache su-exec && \
    addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/cognit-backend-*.jar app.jar
# file.upload-dir defaults to a relative "uploads" folder under the
# working directory — it needs to exist and be writable by the
# non-root user before LocalFileStorageServiceImpl creates it at boot.
RUN mkdir -p /app/uploads && chown -R spring:spring /app
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080

# The entrypoint runs as root only long enough to chown the (root-owned)
# Railway volume at /app/uploads, then exec's the JVM as `spring`.
# JAVA_OPTS lets you pass extra flags (e.g. -XX:MaxRAMPercentage=75) via an
# env var without rebuilding the image.
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
