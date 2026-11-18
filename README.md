# Blog backend

Бэкенд для приложения-блога.

## Стек
- Java 21
- Spring Framework 7 (Spring MVC, Spring Data JDBC)
- PostgreSQL
- Lombok
- Jackson 3
- Maven 3
- JUnit 5

## Сборка и запуск
```bash 
docker compose down -v
mvn clean package
docker compose up --build --wait -d
docker logs -f my-blog-back-container
```
```bash
docker compose down
```

```bash
docker exec -it blog-db psql -U blog_user -d blog
```