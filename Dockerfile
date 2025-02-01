# Maven が含まれた OpenJDK イメージを使用
FROM maven:3.8.7-openjdk-17 AS build

WORKDIR /app

# ソースコードをコピーしてビルド
COPY . /app
RUN mvn clean package -DskipTests

# 実行用の軽量イメージを作成
FROM openjdk:17

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
