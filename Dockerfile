FROM gradle:8-jdk21 AS builder
WORKDIR /app

# 의존성 파일만 먼저 복사
COPY build.gradle .
COPY settings.gradle .

RUN gradle dependencies

# 소스 코드 복사
COPY src ./src
RUN gradle build -x test

# ===== 실행 스테이지 =====
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

copy --from=builder /app/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]