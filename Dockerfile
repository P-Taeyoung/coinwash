
FROM amazoncorretto:17

WORKDIR /app

COPY build/libs/coinwash-*.jar coinwash-app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:}", \
    "-jar", "coinwash-app.jar"]