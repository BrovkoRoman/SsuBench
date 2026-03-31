# SsuBench

Сервис для размещения заданий с оплатой виртуальными баллами.

## Технологии

    Java 17, Spring Boot 4.0.3

    PostgreSQL (в Docker)

    Flyway (миграции)

    Docker, Docker Compose

## Быстрый старт

### 1. Клонировать репозиторий

    git clone https://github.com/BrovkoRoman/SsuBench.git

    cd SsuBench

### 2. Запуск PostgreSQL через Docker Compose

    docker-compose up -d

Это поднимет PostgreSQL на порту 5432 и PgAdmin на порту 5050

### 3. Запуск приложения в IntelliJ IDEA

Нужно запустить файл:

    src/main/java/com/brovko/SsuBench/SsuBenchApplication.java

При первом запуске Flyway автоматически выполнит миграции и создаст схему базы данных.

### 4. Проверка работоспособности

    curl localhost:8080/v3/api-docs.yaml

## Миграции базы данных

Миграции расположены в src/main/resources/db/migration.
Имена файлов: V<версия>__<описание>.sql.
При запуске приложения Flyway применяет новые миграции автоматически.

## API документация

Полная спецификация OpenAPI:

    localhost:8080/v3/api-docs.yaml

Интерактивная документация (Swagger UI):

    http://localhost:8080/swagger-ui.html

## Примеры curl (Windows, cmd)

### Аутентификация

    curl -X POST http://localhost:8080/user/login -H "Content-Type: application/json" -d "{\"login\": \"admin\", \"password\": \"admin\"}"

В ответе приходит JWT-токен. Далее используйте его в заголовке  

    Authorization: Bearer <token>

### Регистрация нового пользователя

    curl -X POST http://localhost:8080/user/register -H "Content-Type: application/json" -d "{\"login\": \"customer1\", \"password\": \"12345\", \"role\": \"CUSTOMER\"}"

Зарегистрироваться можно только с ролями CUSTOMER и EXECUTOR, для роли ADMIN используйте аутентификацию с логином "admin" и паролем "admin" (этот пользователь определен в миграциях).

### Создать задание

Вместо \<token> нужно подставить значение jwt, полученное при регистрации или входе в систему. Чтобы создать задание, нужно войти с ролью CUSTOMER.

    curl -X POST http://localhost:8080/task -H "Content-Type: application/json" -H "Authorization: Bearer <token>" -d "{\"text\": \"Task description\", \"rewardMoney\": \"100\"}"  
