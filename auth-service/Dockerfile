# --- ЭТАП 1: Сборка (Нужен JDK 26) ---
# Используем официальный образ Liberica JDK 26
FROM bellsoft/liberica-openjdk-alpine:26 AS build
WORKDIR /app

# Копируем файлы Gradle Wrapper из вашего проекта
COPY gradlew ./
COPY gradle ./gradle

RUN chmod +x gradlew

# Копируем конфигурацию и исходный код
COPY build.gradle settings.gradle ./
COPY src ./src

# Собираем production-ready .jar файл
RUN ./gradlew bootJar --no-daemon -x test

# --- ЭТАП 2: Запуск (Нужен минимальный JRE 26) ---
# Берем супер-легковесный образ рантайма JRE 26 от BellSoft
FROM bellsoft/liberica-openjre-alpine:26
WORKDIR /app

# Безопасность: добавляем системного пользователя без root-прав
RUN adduser -D appuser && chown -R appuser:appuser /app
USER appuser

# Забираем готовый .jar из этапа сборки
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]