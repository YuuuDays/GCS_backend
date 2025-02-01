# Maven のビルド環境を Eclipse Temurin に変更
FROM maven:3.8.8-eclipse-temurin-17 AS build

WORKDIR /app

# すべてのソースコードをコピー
COPY . .

# JAR をビルド
RUN mvn clean package -DskipTests -B

# 実行用の軽量イメージ
FROM eclipse-temurin:17

WORKDIR /app

# `target` の中身を確認する
RUN ls -la /app/target

# `target/` の JAR を確実にコピー
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
