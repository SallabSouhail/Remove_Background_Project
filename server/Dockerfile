
# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar /app/removebg.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/removebg.jar"]
