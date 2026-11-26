# Blog backend

Бэкенд для приложения-блога.

## Стек
- Java 21
- Spring Framework 6
- PostgreSQL
- Lombok
- Jackson 2
- Maven 3
- JUnit 5
- Liquibase
- Testcontainers
- HikariCP
- Docker

## Сборка и запуск

```bash
docker compose down
mvn clean package
docker compose up --build --wait -d
docker logs -f my-blog-back-container
```

## Вход в БД в докере
```bash
docker exec -it blog-db psql -U blog_user -d blog
```

## Тесты

```bash
mvn clean test
```

Интеграционные тесты (DAO и MVC) поднимают реальный PostgreSQL через Testcontainers, поэтому нужен запущенный Docker.
Юнит-тесты сервисов работают на Mockito и Docker не требуют.

## Осторожно! Сборка и запуск с удалением тома базы данных, только для удобства разработки!

```bash 
docker compose down -v
mvn clean package
docker compose up --build --wait -d
docker logs -f my-blog-back-container
```

## Деплой в сервлет-контейнер
`mvn clean package` собирает `target/ROOT.war`. Запуск в Tomcat описан в `Dockerfile` (`tomcat:11-jdk21`): 
WAR копируется в `webapps/ROOT.war`, приложение доступно по корню (`/`).

Для ручного деплоя в Tomcat 11 на JDK 21 положить `ROOT.war` в `webapps/` и задать переменные окружения:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER`

Схема БД накатывается автоматически через Liquibase при старте.

## Использование
Базовый URL: `http://localhost:8080`.

Пример:
```bash
curl -X POST http://localhost:8080/api/posts \
  -H 'Content-Type: application/json' \
  -d '{"title":"Пост","text":"Текст поста","tags":["tag_1"]}'
```