# --- 런타임 이미지 (Java 21 JRE) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Gradle 빌드 후 생성된 JAR를 컨테이너에 복사
# 빌드된 JAR 파일명 확인: feelem-0.0.1-SNAPSHOT.jar
COPY build/libs/feelem-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
