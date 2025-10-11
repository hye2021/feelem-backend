# Dockerfile (ppt-backend/feelem/Dockerfile)

# --- 1단계: 빌더 이미지 (애플리케이션 빌드용) ---
FROM openjdk:17-alpine
WORKDIR /app # 컨테이너 내 작업 디렉토리 설정

# Gradle Wrapper와 설정 파일, 소스 코드를 복사
# 현재 디렉토리 (feelem) 기준 경로
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .
COPY src ./src

# JAR 파일 빌드
RUN chmod +x ./gradlew && ./gradlew bootJar

# --- 2단계: 런타임 이미지 (애플리케이션 실행용) ---
FROM openjdk:17-jre-alpine
WORKDIR /app

# 빌더 단계에서 생성된 JAR 파일을 복사
# 정확한 JAR 파일명 반영: feelem-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/build/libs/feelem-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]