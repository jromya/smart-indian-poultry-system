# Stage 1: Build Spring Boot Fat JAR with Java 21 & Maven 3.9
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight Production Java 21 Runtime (with Indian Unicode Fonts for OpenPDF)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    fonts-noto-core \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /build/target/smart-poultry-backend-1.0.0.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseSerialGC -Xms128m -Xmx320m -XX:MaxMetaspaceSize=120m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
