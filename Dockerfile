# 1. 베이스 이미지 - OpenJDK 17 사용
FROM openjdk:17-jdk-slim

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. JAR 파일을 컨테이너로 복사
# Gradle 빌드 결과물 복사
COPY build/libs/coinwash-*.jar coinwash-app.jar

# 5. 포트 노출 (Spring Boot 기본 포트)
EXPOSE 8080

# 6. 애플리케이션 실행 명령어
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=prod", \
    "-Dspring.config.activate.on-profile=prod", \
    "-jar", "coinwash-app.jar"]