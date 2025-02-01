FROM openjdk:17
WORKDIR /app

# JAR を `target/` からコピー
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

CMD ["java", "-jar", "app.jar"]
