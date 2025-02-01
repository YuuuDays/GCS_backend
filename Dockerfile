FROM maven:3.8.8-eclipse-temurin-17 AS build

WORKDIR /app

# すべてのソースコードをコピー
COPY . .

# JAR をビルド（ログを標準出力にリダイレクト）
RUN mvn clean package -DskipTests -B || cat /app/target/build.log

# `app/` の中身を確認する（target があるかチェック）
RUN ls -la /app

# 実行用の軽量イメージ
FROM eclipse-temurin:17

WORKDIR /app

# `target` の中身を確認する（もしエラーになるなら削除）
RUN ls -la /app/target || echo "WARNING: /app/target does not exist!"

# `target/` の JAR を確実にコピー
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
