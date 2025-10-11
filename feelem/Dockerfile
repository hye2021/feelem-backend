# --- 런타임 이미지 (미리 빌드된 JAR 파일 실행용) ---
# Java 21 JRE가 설치된 경량 Alpine 기반 이미지 사용
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# GitHub Actions를 통해 이미 EC2의 TARGET_DIR로 전송된 JAR 파일을 복사
# EC2의 /home/ubuntu/ppt-backend/feelem 디렉토리에 JAR 파일이 있다고 가정
# Dockerfile이 /home/ubuntu/ppt-backend/feelem 에 있을 것이므로, 그냥 JAR 파일만 COPY 하면 됨
# JAR 파일명은 feelem-0.0.1-SNAPSHOT.jar (PROJECT_PATH/build/libs/ 에서 가져온 이름과 동일해야 함)
# --- 런타임 이미지 (미리 빌드된 JAR 파일 실행용) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# GitHub Actions의 빌드 컨텍스트 (feelem/) 내에서 실제 JAR 파일 경로 지정
COPY build/libs/feelem-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
# ... (생략) ...

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]