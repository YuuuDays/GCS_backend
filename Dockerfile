FROM openjdk:17-alpine

WORKDIR /app

RUN apk add --no-cache maven

COPY . /app
RUN mvn clean package -DskipTests -B

COPY target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
