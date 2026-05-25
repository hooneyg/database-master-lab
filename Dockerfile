# ╔══════════════════════════════════════════════════════════════════╗
# ║         🐳 Dockerfile (Multi-stage Build for Java 21)           ║
# ║                                                                  ║
# ║  [빌드 전략]                                                     ║
# ║  빌드 환경과 실행 환경을 분리하여 공격 표면을 줄이고 이미지 크기 최적화 ║
# ╚══════════════════════════════════════════════════════════════════╝

# ☕ Stage 1: Build (Java 21 LTS)
FROM eclipse-temurin:21-jdk-jammy AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 크로스 플랫폼 빌드를 위한 캐리지 리턴(\r) 제거 및 개행 문자(LF) 통일  
RUN sed -i 's/\r$//' gradlew
# RUN sed -i 's/\r$//' build.gradle

# gradlew 파일에 실행 권한 부여
RUN chmod +x gradlew


# 의존성 미리 다운로드 (캐싱 활용)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드 (테스트 포함)
COPY src src
# RUN ./gradlew build --no-daemon
RUN ./gradlew build -x test --no-daemon

# 🚀 Stage 2: Run
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 빌드 결과물(jar)만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 컨테이너 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]

# 포트 설정
EXPOSE 8080
