# GoComet Ride Hailing - SDE-2 Assignment

## Quick Start

### Prerequisites
- Java 17+, Maven 3.8+
- MySQL 8+
- Redis (local or Docker)

### MySQL Setup
If you don't already have MySQL installed (common on a fresh Mac), install and start it:

```bash
brew install mysql
brew services start mysql
```

Create the database:

```bash
mysql -uroot
```

```sql
CREATE DATABASE IF NOT EXISTS gocomet_db;
```

Connection settings are configurable via env vars (recommended) or by editing `backend/src/main/resources/application.properties`:

```bash
export MYSQL_URL='jdbc:mysql://localhost:3306/gocomet_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export MYSQL_USERNAME='root'
export MYSQL_PASSWORD='root'
```

### Redis Setup
```bash
# Option 1: Docker
docker run -d -p 6379:6379 redis:latest

# Option 2: Homebrew (Mac)
brew install redis && brew services start redis
```

You can override Redis settings via env vars:

```bash
export REDIS_HOST='localhost'
export REDIS_PORT='6379'
export REDIS_PASSWORD=''
```

### Run Backend
```bash
cd backend
mvn spring-boot:run
# Runs on http://localhost:8080
```

### Run Frontend
```bash
cd frontend
# Option 1: VS Code Live Server
# Option 2: Python simple server
python3 -m http.server 3000
# Open http://localhost:3000
```

---

## API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /v1/rides | Create ride request |
| GET | /v1/rides/{id} | Get ride by ID |
| GET | /v1/rides | Get all rides |
| POST | /v1/trips/{id}/start | Start trip |
| POST | /v1/trips/{id}/end | End trip + fare calc |
| POST | /v1/rides/{id}/cancel | Cancel ride |
| POST | /v1/drivers | Register driver |
| GET | /v1/drivers | Get all drivers |
| POST | /v1/drivers/{id}/location | Update driver GPS |
| POST | /v1/drivers/{id}/accept | Driver accept ride |
| POST | /v1/payments | Process payment |

---

## New Relic Setup
1. Sign up at https://newrelic.com (100GB free)
2. Download Java agent: https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/
3. Place `newrelic.jar` in `backend/`
4. Update `newrelic.yml` with your license key
5. Run: `mvn spring-boot:run -Djvmargs="-javaagent:newrelic.jar"`

---

## GitHub Push
```bash
git init
git add .
git commit -m "GoComet ride hailing initial commit"
git remote add origin https://github.com/YOUR_USERNAME/gocomet-daw.git
git push -u origin main
```
