FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# build/libs 내부 jar 파일을 자동으로 복사 (이름 상관없이)
COPY build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
