FROM openjdk:17-alpine

WORKDIR /app

# Maven をインストール
RUN apk add --no-cache maven

# ソースコードをコピー
COPY . /app

# JAR をビルド (バッチモードでログを抑える)
RUN mvn clean package -DskipTests -B

# JAR ファイルをコピー (正しいパスに修正)
COPY target/*.jar app.jar

# アプリケーションを実行
CMD ["java", "-jar", "app.jar"]
