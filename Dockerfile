FROM openjdk:17

WORKDIR /app

# Maven を使って JAR をビルド
COPY . /app
RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests

# JAR をコピー
COPY target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
