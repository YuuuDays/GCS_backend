# 修正後の Dockerfile
FROM maven:3.8.7-openjdk-17 AS build

WORKDIR /app

# すべてのソースコードをコピー
COPY . .

# JAR をビルド
RUN mvn clean package -DskipTests -B

# 実行用の軽量イメージ
FROM openjdk:17

WORKDIR /app

# `target` の中身を確認する
RUN ls -la /app/target

# `target/` の JAR を確実にコピー
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
