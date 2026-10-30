FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY common/pom.xml common/pom.xml
COPY log-producer/pom.xml log-producer/pom.xml
COPY log-aggregator/pom.xml log-aggregator/pom.xml
COPY log-query-api/pom.xml log-query-api/pom.xml

COPY common/src common/src
COPY log-producer/src log-producer/src
COPY log-aggregator/src log-aggregator/src
COPY log-query-api/src log-query-api/src

RUN apk add --no-cache maven && \
    mvn -pl common,log-producer -am clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/log-producer/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
