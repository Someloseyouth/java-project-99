# Task Manager

Учебный проект — веб‑приложение для управления задачами.  
Сервис позволяет создавать задачи, назначать исполнителей, менять статусы и добавлять метки для удобной фильтрации.

[![Actions Status](https://github.com/Someloseyouth/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/Someloseyouth/java-project-99/actions)
[![Actions Status](https://github.com/Someloseyouth/java-project-99/actions/workflows/ci.yml/badge.svg)](https://github.com/Someloseyouth/java-project-99/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Someloseyouth_java-project-99&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Someloseyouth_java-project-99)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Someloseyouth_java-project-99&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Someloseyouth_java-project-99)

## Демо

Приложение задеплоено на Hugging Face Spaces:

👉 [https://someloseyouth-java-project-99.hf.space](https://someloseyouth-java-project-99.hf.space)

Интерактивная документация API (Swagger UI) доступна по адресу:

👉 [https://someloseyouth-java-project-99.hf.space/swagger-ui.html](https://someloseyouth-java-project-99.hf.space/swagger-ui.html)

## Функциональность

Основные возможности:

- Регистрация и аутентификация пользователя (JWT).
- Управление пользователями:
    - список пользователей;
    - просмотр профиля;
    - изменение и удаление своего профиля.
- Статусы задач:
    - предустановленные статусы (Draft, To review, To be fixed, To publish, Published);
    - CRUD для статусов (создание, редактирование, удаление).
- Метки (labels):
    - CRUD для меток;
    - связь задач и меток many‑to‑many;
    - запрет удаления метки, если она используется в задачах;
    - дефолтные метки `feature` и `bug` при старте приложения.
- Задачи:
    - создание задач с указанием:
        - заголовка и описания,
        - статуса,
        - исполнителя,
        - набора меток;
    - изменение задач (в т.ч. статуса, исполнителя и меток);
    - удаление задач;
    - просмотр списка задач и отдельной задачи.
- Фильтрация задач:
    - по части названия (`titleCont`);
    - по идентификатору исполнителя (`assigneeId`);
    - по слагу статуса (`status`);
    - по идентификатору метки (`labelId`).

Пример запроса с фильтрацией:

```http
GET /api/tasks?titleCont=create&assigneeId=1&status=to_be_fixed&labelId=1
```

## Технологический стек

- **Язык:** Java 21
- **Фреймворк:** Spring Boot
- **Сборка:** Gradle (Kotlin DSL)
- **База данных:** H2 (встроенная) / PostgreSQL (в проде при необходимости)
- **Безопасность:** Spring Security, JWT (oauth2 resource server)
- **JPA/ORM:** Spring Data JPA, Hibernate
- **Маппинг DTO:** MapStruct
- **Валидация:** Jakarta Bean Validation
- **Тестирование:** JUnit 5, Spring Boot Test, MockMvc, Instancio
- **Документация API:** springdoc‑openapi (Swagger UI)
- **Контейнеризация:** Docker

## Архитектура

Приложение построено по типичному слою:

- `controller` — REST‑контроллеры (`/api/users`, `/api/tasks`, `/api/labels`, `/api/task_statuses`).
- `service` — бизнес‑логика (работа с репозиториями, проверка связей, валидация).
- `repository` — Spring Data JPA репозитории.
- `dto` — объекты передачи данных (Create/Update/DTO).
- `mapper` — MapStruct‑мапперы между сущностями и DTO.
- `model` — JPA‑сущности (`User`, `Task`, `TaskStatus`, `Label`).
- `component` — инициализация данных (`DataInitializer`).

При старте (кроме профиля `test`) автоматически создаются:

- пользователь `hexlet@example.com` с паролем `qwerty`;
- базовые статусы задач;
- базовые метки `feature` и `bug`.

## Запуск локально

### Предварительные требования

- Java 21+
- Gradle (используется wrapper, можно не ставить отдельно)
- Docker (опционально, для контейнерного запуска)

### 1. Клонирование репозитория

```bash
git clone https://github.com/Someloseyouth/java-project-99.git
cd java-project-99
```

### 2. Запуск тестов

```bash
./gradlew test
```

### 3. Запуск приложения

По умолчанию приложение слушает порт `7860` (см. `application.yml`):

```bash
./gradlew bootRun
```

После запуска:

- UI: `http://localhost:7860`
- Swagger UI: `http://localhost:7860/swagger-ui.html`
- Пример API: `http://localhost:7860/api/tasks`

## Запуск в Docker

В репозитории есть `Dockerfile`, поэтому собрать и запустить контейнер можно так:

```bash
# сборка образа
docker build -t java-project-99 .

# запуск контейнера
docker run --rm -p 7860:7860 java-project-99
```

После этого приложение будет доступно по адресу:

- `http://localhost:7860`
- `http://localhost:7860/swagger-ui.html`

## Аутентификация

Приложение использует JWT‑авторизацию:

- Публичный ключ подгружается из `classpath:certs/public.pem`.
- Эндпоинт `/api/login` открытый, остальные `/api/**` — требуют авторизации.

Для удобства разработки предусмотрен дефолтный пользователь:

- email: `hexlet@example.com`
- password: `qwerty`

После получения токена его нужно передавать в заголовке:

```http
Authorization: Bearer <jwt-token>
```

## Тесты

Основные интеграционные тесты расположены в пакете:

- `src/test/java/hexlet/code/controller/api`

Покрываются:

- CRUD операций по пользователям, задачам, статусам, меткам;
- права доступа (действия только с JWT, попытки без токена и чужой пользователем);
- фильтрация задач по параметрам.

Для генерации тестовых данных используется библиотека **Instancio**.