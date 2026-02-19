# GoComet Ride Hailing — HLD & LLD

---

## HLD (High-Level Design)

### System Overview

```
[Rider App / Driver App]
         |
    [Frontend - HTML/JS]
         |
    [Spring Boot API - Port 8080]
       /    \
  [MySQL]  [Redis]
  (data)   (cache + location)
         |
  [New Relic - Monitoring]
```

### Core Components
1. **Ride Service** — manages lifecycle: REQUESTED → DRIVER_ASSIGNED → STARTED → COMPLETED
2. **Driver Service** — location tracking, availability, matching
3. **Surge Service** — dynamic pricing based on active demand
4. **Payment Service** — fare collection via simulated PSP

### Key Design Decisions
- **MySQL** for transactional data (rides, drivers, payments) — ACID guaranteed
- **Redis** for: driver location TTL cache (10s), surge multiplier cache (30s), ride status cache
- **Haversine formula** in SQL for nearby driver lookup (indexed on lat/lng)
- **Idempotency keys** on ride creation and payments to prevent duplicates
- **Auto-assignment**: on ride creation, system finds nearest available driver within 5km
- **Stateless backend** — all state in DB/Redis, horizontally scalable

---

## LLD (Low-Level Design)

### Database Schema

**drivers**
```
id (PK), name, phone (UNIQUE), vehicleType, status (ENUM), 
latitude, longitude, locationUpdatedAt, createdAt
INDEX: status, (latitude, longitude)
```

**rides**
```
id (PK), riderId, driverId, pickupLat, pickupLng, dropLat, dropLng,
pickupAddress, dropAddress, tier, paymentMethod, status (ENUM),
fare, surgeMultiplier, distanceKm, idempotencyKey (UNIQUE),
requestedAt, startedAt, endedAt
INDEX: status, riderId
```

**payments**
```
id (PK), rideId, riderId, amount, method, status (ENUM),
transactionRef, idempotencyKey (UNIQUE), createdAt
```

---

### State Machine

**Ride:**
```
REQUESTED → DRIVER_ASSIGNED (auto-assign or driver.accept)
          → CANCELLED

DRIVER_ASSIGNED → STARTED (trip.start)
               → CANCELLED

STARTED → COMPLETED (trip.end → fare calculated)
```

**Driver:**
```
AVAILABLE → BUSY (assigned to ride)
BUSY → AVAILABLE (ride completed/cancelled)
OFFLINE → AVAILABLE (manual)
```

---

### Surge Pricing Logic

```
activeRides > 500  → 2.5x
activeRides > 200  → 2.0x
activeRides > 100  → 1.5x
else               → 1.0x
```
Cached in Redis for 30 seconds to avoid DB hits on every ride request.

---

### Fare Calculation

```
ECONOMY  = 20 + (distKm × 12)
PREMIUM  = 30 + (distKm × 18)
SUV      = 50 + (distKm × 22)
Final fare = base × surgeMultiplier
```

---

### Nearby Driver Query (Haversine SQL)

```sql
SELECT *, (6371 * acos(cos(radians(:lat)) * cos(radians(latitude))
  * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat))
  * sin(radians(latitude)))) AS distance
FROM drivers
WHERE status = 'AVAILABLE' AND latitude IS NOT NULL
HAVING distance < 5.0
ORDER BY distance LIMIT 10;
```

---

### Redis Usage

| Key Pattern | Value | TTL |
|---|---|---|
| `driver:location:{id}` | {lat, lng} | 10s |
| `surge:multiplier` | Double | 30s |
| `ride:{id}` | status string | 60s |

---

### Concurrency & Atomicity
- `@Transactional` on all write operations
- Unique constraint on `idempotencyKey` prevents duplicate rides/payments
- Driver assignment wrapped in transaction: ride status + driver status updated atomically
- Redis TTL on driver location: if no update in 10s, driver treated as stale

---

### Performance Optimizations
1. **DB Indexes**: status (for filtering), (lat, lng) for spatial queries, riderId for user history
2. **Redis Cache**: surge multiplier avoids repeated COUNT queries, location cache avoids DB writes on every GPS ping
3. **Connection Pooling**: Spring Boot default HikariCP (max 10 connections)
4. **Stateless**: no session state → any instance handles any request

---

### Scalability (For 100k drivers, 10k rides/min, 200k location/sec)
- **Location updates**: Redis geo-hash or sorted sets would handle 200k/sec (current approach: DB write every update, suitable for assignment scale)
- **Horizontal scaling**: multiple Spring Boot instances behind load balancer
- **Read replicas**: ride status reads from MySQL read replica
- **Redis Cluster**: for high availability of cache

---

### New Relic Monitoring
- Tracks API response times per endpoint
- MySQL slow query detection (threshold: 500ms)
- Alerts: response time > 1s → page on-call
- Dashboard: active rides, driver availability, surge multiplier trend

