# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- Runtime stage ---
# jammy (not alpine) is used because it publishes multi-arch images
# (amd64 + arm64), which alpine's eclipse-temurin tag does not.
FROM eclipse-temurin:17-jre-jammy
RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=build /build/target/ops-control-plane.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
