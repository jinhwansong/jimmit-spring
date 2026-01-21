# 빌드 스테이지
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 캐시를 활용하기 위해 의존성 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle clean build -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출 (Render는 PORT 환경변수를 사용)
EXPOSE 8083

# JVM 옵션 설정 (메모리 최적화)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 애플리케이션 실행 (Render의 PORT 환경변수 사용, 없으면 8083 기본값)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8083}"]
