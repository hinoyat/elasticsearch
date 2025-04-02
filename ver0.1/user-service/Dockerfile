FROM openjdk:21
WORKDIR /app
COPY target/*.jar user-service.jar
EXPOSE 8080
# 환경변수 설정으로 Docker 내부 통신 지원
ENV SPRING_DATASOURCE_URL=jdbc:mysql://user-db:3306/user_db?useSSL=false&useUnicode=true&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=1234
ENV SPRING_DATA_REDIS_HOST=redis
ENTRYPOINT ["java", "-jar", "user-service.jar"]