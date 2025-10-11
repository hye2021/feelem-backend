FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# build/libs 내부 jar 파일을 자동으로 복사 (이름 상관없이)
ARG JAR_FILE=build/libs/feelem-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
