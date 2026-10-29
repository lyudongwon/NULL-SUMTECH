# 백엔드(Spring Boot). 빌드는 여기서 끝내고, 컨테이너 시작 명령은 서버 실행만 한다.
# Flyway가 시작할 때 마이그레이션과 시드를 실행한다.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies > /dev/null
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
EXPOSE 8080
CMD ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
