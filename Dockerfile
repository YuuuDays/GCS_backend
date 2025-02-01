FROM openjdk:17

WORKDIR /app

# Alpine Linux なら apk add で Maven をインストール
RUN apk add --no-cache maven

# ソースコードをコピーしてビルド
COPY . /app
RUN mvn clean package -DskipTests

# JAR をコピー
COPY target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
