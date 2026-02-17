# Build stage: compile both services
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY . .
RUN ./gradlew :command-service:bootJar :query-service:bootJar --no-daemon -x test

# Run stage: one image with both jars; compose overrides CMD per service
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/command-service/build/libs/command-service-*.jar ./command-service.jar
COPY --from=build /build/query-service/build/libs/query-service-*.jar ./query-service.jar
ENTRYPOINT ["java", "-jar"]
CMD ["command-service.jar"]
