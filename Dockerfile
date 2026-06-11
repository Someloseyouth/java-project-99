# Build stage
FROM gradle:8.14-jdk21 AS build
WORKDIR /app

# Устанавливаем Node.js
RUN apt-get update && apt-get install -y nodejs npm

COPY . .

# Устанавливаем и собираем фронтенд
RUN npm i @hexlet/java-task-manager-frontend
RUN npx build-frontend

# Добовляем Sentry token
ARG SENTRY_AUTH_TOKEN
ENV SENTRY_AUTH_TOKEN=${SENTRY_AUTH_TOKEN}

# Собираем Java приложение
RUN gradle build -x test

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 7860
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production}", "-jar", "app.jar"]