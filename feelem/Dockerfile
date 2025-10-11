FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# build context가 feelem/ 이므로 아래 경로가 유효합니다.
# Actions 로그에서 확인된 JAR: feelem-0.0.1-SNAPSHOT.jar
COPY ./build/libs/feelem-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
