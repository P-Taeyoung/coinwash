# 1단계: 빌드 스테이지
FROM gradle:8-jdk17 as builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew clean build -x test --no-daemon

# 2단계: 실행 스테이지 - Amazon Corretto
FROM amazoncorretto:17

WORKDIR /app

COPY --from=builder /app/build/libs/coinwash-*.jar coinwash-app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:}", \
    "-jar", "coinwash-app.jar"]