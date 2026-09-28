# Library

Spring Boot застосунок (REST API + Thymeleaf UI) з PostgreSQL, Liquibase і Keycloak (OIDC для UI, JWT для API).

Користувачі Keycloak: `user/user` (роль USER), `admin/admin` (ролі USER, ADMIN).

## Docker

```
docker compose up --build
```
- Застосунок: http://localhost:8080
- Keycloak: http://localhost:8180 (адмінка `admin/admin`)