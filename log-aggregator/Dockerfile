FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY ../pom.xml /app/parent-pom.xml
COPY ../common /app/common
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B -f /app/parent-pom.xml -pl common,log-aggregator

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/log-aggregator/target/*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
