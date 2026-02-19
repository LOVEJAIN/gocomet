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

### Run Backend with New Relic (Recommended)
We **do not commit real license keys**. Use an environment variable instead.

```bash
# 1) Copy agent files to a path without spaces (recommended on macOS)
mkdir -p /tmp/newrelic
cp backend/newrelic/newrelic.jar /tmp/newrelic/
cp backend/newrelic/newrelic.yml /tmp/newrelic/

# 2) Export your license key (keep it local)
export NEW_RELIC_LICENSE_KEY="YOUR_REAL_KEY"

# 3) Run backend with the Java agent
cd backend
mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-javaagent:/tmp/newrelic/newrelic.jar -Dnewrelic.config.file=/tmp/newrelic/newrelic.yml"
```

**IDE run configuration (IntelliJ/VS Code)**
- VM options:
  ```
  -javaagent:/tmp/newrelic/newrelic.jar -Dnewrelic.config.file=/tmp/newrelic/newrelic.yml
  ```
- Environment variables:
  ```
  NEW_RELIC_LICENSE_KEY=YOUR_REAL_KEY
  ```

### Run Frontend
```bash
cd frontend
# Option 1: VS Code Live Server
# Option 2: Python simple server
python3 -m http.server 3000 --directory "/Users/lovejain/Documents/New project/frontend"
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
| GET | /v1/drivers/{id} | Get driver by ID |
| POST | /v1/drivers/{id}/location | Update driver GPS |
| POST | /v1/drivers/{id}/status?status=AVAILABLE|BUSY|OFFLINE | Update driver status |
| POST | /v1/drivers/{id}/accept?rideId={rideId} | Driver accept ride |
| POST | /v1/payments | Process payment |
| GET | /v1/payments/ride/{rideId} | Get payment by ride |

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
