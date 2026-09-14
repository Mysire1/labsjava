# Library

Spring Boot застосунок (REST API + Thymeleaf UI) з PostgreSQL, Liquibase і Keycloak (OIDC для UI, JWT для API).

Користувачі Keycloak: `user/user` (роль USER), `admin/admin` (ролі USER, ADMIN).

## Варіант 1. Docker (все одразу)

```
docker compose up --build
```
- Застосунок: http://localhost:8080
- Keycloak: http://localhost:8180 (адмінка `admin/admin`)

Зупинити: `docker compose down`.

## Варіант 2. Локально без Docker

Потрібно: JDK 25, PostgreSQL на 5432.

1. База (один раз):
   ```
   psql -d postgres -c "CREATE USER library WITH PASSWORD 'library';" -c "CREATE DATABASE library OWNER library;"
   ```
2. Keycloak (один раз скачати https://github.com/keycloak/keycloak/releases/download/26.4.7/keycloak-26.4.7.zip і розпакувати, напр. в `~/keycloak-26.4.7`):
   ```
   mkdir -p ~/keycloak-26.4.7/data/import
   cp keycloak/library-realm.json ~/keycloak-26.4.7/data/import/
   KC_BOOTSTRAP_ADMIN_USERNAME=admin KC_BOOTSTRAP_ADMIN_PASSWORD=admin ~/keycloak-26.4.7/bin/kc.sh start-dev --http-port=8180 --import-realm
   ```
   Realm імпортується лише якщо його ще немає. Щоб скинути Keycloak до початкового стану — видалити `~/keycloak-26.4.7/data/h2`.
3. Застосунок (в іншому терміналі або Run `DemoApplication` в IntelliJ):
   ```
   ./gradlew bootRun
   ```
   Liquibase сам створить таблиці при старті.

### Запуск з IntelliJ (все одразу)

У списку конфігурацій зверху (з `.run/`):
- **All (Keycloak + App)** — Keycloak і застосунок разом
- **Keycloak** — лише Keycloak (`scripts/keycloak.sh`, чекає Keycloak у `~/keycloak-26.4.7`, інше місце — змінна `KC_HOME`)
- **App** — лише застосунок (`gradlew bootRun`)

UI (Thymeleaf) — частина застосунку, окремого фронта немає. Postgres має бути запущений.

## Як користуватись

### UI (OIDC)
1. Відкрити http://localhost:8080 → редирект на логін Keycloak.
2. Увійти як `user` або `admin` → повернення в застосунок, зверху видно користувача і ролі.
3. Додати книгу (форма знизу), Edit, Delete.
   - `user`: Edit → 403 (`@PreAuthorize` на `BookService.update`), Delete → 403 (правило в `SecurityFilterChain`).
   - `admin`: усе дозволено.
4. Logout → вихід із Keycloak.

### API (JWT) — Postman
1. Postman → Import → `postman/library.postman_collection.json`.
2. Запускати запити по порядку (токени і `bookId` зберігаються автоматично):

| # | Запит | Очікується |
|---|---|---|
| 1–2 | Get token (user / admin) | 200 |
| 3 | List books без токена | 401 |
| 4 | Create book (user) | 201 |
| 5–6 | List / Get by id (user) | 200 |
| 7 | Update (user) | 403 — `@PreAuthorize` |
| 8 | Update (admin) | 200 |
| 9 | Delete (user) | 403 — `SecurityFilterChain` |
| 10 | Delete (admin) | 204 |

Те саме через curl:
```
TOKEN=$(curl -s -X POST http://localhost:8180/realms/library/protocol/openid-connect/token \
  -d grant_type=password -d client_id=library-app -d client_secret=library-secret \
  -d username=admin -d password=admin | jq -r .access_token)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/books
```

### Тести
```
./gradlew test
```

## Де що в коді
- `book/` — Entity, DTO, MapStruct mapper, Repository, Service, REST і UI контролери
- `SecurityConfig` — два `SecurityFilterChain`: `/api/**` (JWT) і решта (OIDC login)
- `db/changelog/` — Liquibase міграції
- `keycloak/library-realm.json` — realm, клієнт, ролі, користувачі
- `Dockerfile`, `docker-compose.yml` — контейнеризація
