#애플리케이션 빌드 하는 부분
#개발에 필요한것들을 모두 설치하여 로컬 소스코드를 컨테이너로 복사
# 빌드 도구(Gradle)를 포함한 이미지를 사용하여 JAR 파일 생성
FROM gradle:7.6.0-jdk17 AS builder

WORKDIR /app

# 의존성 캐싱을 위해 Gradle 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Gradle 래퍼 스크립트 복사 및 권한 설정
COPY gradlew ./
RUN chmod +x ./gradlew

# 의존성 다운로드 - 소스 변경 시에도 이 단계는 캐시됨, 매번
RUN ./gradlew dependencies --no-daemon
# 실제 코드 빌드
COPY src ./src
# 빌드 실행
RUN ./gradlew clean build -x test --no-daemon

#Jar파일 이미지에 저장하는 부분
FROM eclipse-temurin:17-jdk-jammy

ENV PROJECT_NAME=duckhu
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

# Tess4j 를 위한 필수 패키지 설치 (OCR 라이브러리)
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    libtesseract-dev \
    && rm -rf /var/lib/apt/lists/*


WORKDIR /app

#builder 컨테이너에 들어가 builder 컨테이너 내부에 있는 파일을 app.jar로 가져오기
COPY --from=builder /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar app.jar

EXPOSE 80

#sh=shell -c=해당 명령어 실행 $JVM_OPTS= ENTRY실행될때까지 몰라.
#그냥 java로 하면 환경변수 그대로 문자열로 인식해서 JVM_OPTS는 값이 안들어가게됨.
ENTRYPOINT ["sh","-c","java -Duser.timezone=Asia/Seoul $JVM_OPTS -jar app.jar"]