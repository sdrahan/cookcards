# cookcards

## Stack

- Java 21, Spring Boot 3 (Monolith), Thymeleaf, Tailwind
- MySQL 8, Hibernate, Flyway
- Docker Compose

## Documentation
- Entry point for contributors/agents: `AGENTS.md`
- Project docs map: `docs/README.md`

---

## Getting Started (Local)

### Prerequisites
- Java 21
- MySQL 8
- Maven 3

### Start MySQL

**Option A: Docker**

```bash
docker run --name cookcards-mysql -p 3306:3306 \
  -e MYSQL_DATABASE=cookcards \
  -e MYSQL_USER=cookcards \
  -e MYSQL_PASSWORD=cookcards \
  -e MYSQL_ROOT_PASSWORD=root \
  -d mysql:8.0
```

**Option B: Local MySQL server**
```
CREATE DATABASE IF NOT EXISTS `cookcards`;
CREATE USER IF NOT EXISTS 'cookcards'@'%' IDENTIFIED BY 'cookcards';
GRANT ALL PRIVILEGES ON `cookcards`.* TO 'cookcards'@'%';
```

### Run with local profile
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

To add initial users and recipes use `init-db` profile:
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,init-db
```

### During development

When the app is running, it retrieves page templates from `target/classes/templates`
When you change template - it has to be copied into target dir. If styles inside the page are changed - they have to be recompiled by Tailwind.
You can do `mvn compile` every time. Alternatively you can have hot-update of templates:
```
cd frontend
npm run watch
```

This will have templates automatically tailwind-ed and copied from `src/main/resources/templates` to `target/classes/templates` on update.
