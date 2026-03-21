# Build stage
FROM maven:3.9.13-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
COPY src ./src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar /app/removebg.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/removebg.jar"]
