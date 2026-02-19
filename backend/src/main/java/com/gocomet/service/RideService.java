package com.gocomet.service;

import com.gocomet.dto.CreateRideRequest;
import com.gocomet.model.Driver;
import com.gocomet.model.Ride;
import com.gocomet.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rideRepository;
    private final DriverService driverService;
    private final SurgeService surgeService;
    private final RedisTemplate<String, Object> redisTemplate;

    public RideService(RideRepository rideRepository, DriverService driverService,
                       SurgeService surgeService, RedisTemplate<String, Object> redisTemplate) {
        this.rideRepository = rideRepository;
        this.driverService = driverService;
        this.surgeService = surgeService;
        this.redisTemplate = redisTemplate;
    }

    private String rideKey(Long rideId) { return "ride:" + rideId; }

    @Transactional
    public Ride createRide(CreateRideRequest req) {
        if (req.getIdempotencyKey() != null) {
            Optional<Ride> existing = rideRepository.findByIdempotencyKey(req.getIdempotencyKey());
            if (existing.isPresent()) return existing.get();
        }
        Ride ride = new Ride();
        ride.setRiderId(req.getRiderId());
        ride.setPickupLat(req.getPickupLat());
        ride.setPickupLng(req.getPickupLng());
        ride.setDropLat(req.getDropLat());
        ride.setDropLng(req.getDropLng());
        ride.setPickupAddress(req.getPickupAddress());
        ride.setDropAddress(req.getDropAddress());
        ride.setTier(req.getTier());
        ride.setPaymentMethod(req.getPaymentMethod());
        ride.setIdempotencyKey(req.getIdempotencyKey() != null ? req.getIdempotencyKey() : UUID.randomUUID().toString());
        ride.setSurgeMultiplier(surgeService.getSurgeMultiplier());

        double distKm = surgeService.calculateDistance(
            req.getPickupLat(), req.getPickupLng(), req.getDropLat(), req.getDropLng());
        ride.setDistanceKm(distKm);
        ride.setFare(surgeService.calculateFare(distKm, req.getTier(), ride.getSurgeMultiplier()));

        Ride saved = rideRepository.save(ride);
        autoAssignDriver(saved);
        redisTemplate.opsForValue().set(rideKey(saved.getId()), saved.getStatus().name(), 60, TimeUnit.SECONDS);
        return saved;
    }

    @Transactional
    public void autoAssignDriver(Ride ride) {
        List<Driver> nearby = driverService.findNearbyDrivers(ride.getPickupLat(), ride.getPickupLng(), 5.0);
        if (!nearby.isEmpty()) {
            Driver driver = nearby.get(0);
            ride.setDriverId(driver.getId());
            ride.setStatus(Ride.RideStatus.DRIVER_ASSIGNED);
            rideRepository.save(ride);
            driverService.setStatus(driver.getId(), Driver.DriverStatus.BUSY);
            log.info("Driver {} assigned to ride {}", driver.getId(), ride.getId());
        } else {
            log.info("No nearby drivers for ride {}", ride.getId());
        }
    }

    public Ride getRide(Long id) {
        return rideRepository.findById(id).orElseThrow(() -> new RuntimeException("Ride not found: " + id));
    }

    @Transactional
    public Ride acceptRide(Long driverId, Long rideId) {
        Ride ride = getRide(rideId);
        if (ride.getStatus() != Ride.RideStatus.REQUESTED) throw new RuntimeException("Ride not in REQUESTED state");
        ride.setDriverId(driverId);
        ride.setStatus(Ride.RideStatus.DRIVER_ASSIGNED);
        driverService.setStatus(driverId, Driver.DriverStatus.BUSY);
        return rideRepository.save(ride);
    }

    @Transactional
    public Ride startTrip(Long tripId) {
        Ride ride = getRide(tripId);
        ride.setStatus(Ride.RideStatus.STARTED);
        ride.setStartedAt(LocalDateTime.now());
        return rideRepository.save(ride);
    }

    @Transactional
    public Ride endTrip(Long tripId) {
        Ride ride = getRide(tripId);
        if (ride.getStatus() == Ride.RideStatus.COMPLETED) return ride;
        ride.setStatus(Ride.RideStatus.COMPLETED);
        ride.setEndedAt(LocalDateTime.now());
        if (ride.getDriverId() != null) driverService.setStatus(ride.getDriverId(), Driver.DriverStatus.AVAILABLE);
        redisTemplate.delete(rideKey(tripId));
        return rideRepository.save(ride);
    }

    @Transactional
    public Ride cancelRide(Long rideId) {
        Ride ride = getRide(rideId);
        if (ride.getDriverId() != null) driverService.setStatus(ride.getDriverId(), Driver.DriverStatus.AVAILABLE);
        ride.setStatus(Ride.RideStatus.CANCELLED);
        redisTemplate.delete(rideKey(rideId));
        return rideRepository.save(ride);
    }

    public List<Ride> getAllRides() { return rideRepository.findAll(); }
}
