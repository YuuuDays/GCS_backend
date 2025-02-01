# 修正後の Dockerfile
FROM maven:3.8.8-eclipse-temurin-17 AS build

WORKDIR /app

# すべてのソースコードをコピー
COPY . .

# JAR をビルド
RUN mvn clean package -DskipTests -B

# どこに `target/` があるか確認する
RUN ls -la /app

# 実行用の軽量イメージ
FROM eclipse-temurin:17

WORKDIR /app

# `target` の中身を確認する（これがエラーになる可能性がある）
RUN ls -la /app/target  # もしエラーになるなら削除

# `target/` の JAR を確実にコピー
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
