# --- 런타임 이미지 (미리 빌드된 JAR 실행용) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드된 JAR 파일 COPY (경로 수정)
COPY ./build/libs/feelem-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
