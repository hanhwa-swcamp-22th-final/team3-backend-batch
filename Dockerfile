# ── Build stage ──
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ── Runtime stage ──
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/team3backendbatch-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.include=db,secret", "--server.port=8084"]
