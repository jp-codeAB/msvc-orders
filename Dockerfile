FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY src src

RUN gradle clean build -x test --no-daemon

FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8085

ENV SPRING_PROFILES_ACTIVE=dev \
    CONFIG_SERVER_URL=http://config-server:8888 \
    EUREKA_URI=http://eureka-server:8761/eureka/ \
    RABBITMQ_HOST=rabbitmq \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USER=guest \
    RABBITMQ_PASS=guest \
    DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=orderdb \
    DB_USER=postgres \
    DB_PASSWORD=100juanU

ENTRYPOINT ["java", "-jar", "app.jar"]
