FROM openjdk:17-bullseye

WORKDIR /app

# Debian ベースなので apt-get が使える
RUN apt-get update && apt-get install -y maven

# ソースコードをコピーしてビルド
COPY . /app
RUN mvn clean package -DskipTests

# JAR をコピー
COPY target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
