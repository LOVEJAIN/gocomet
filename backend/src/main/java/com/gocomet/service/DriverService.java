package com.gocomet.service;

import com.gocomet.model.Driver;
import com.gocomet.repository.DriverRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public DriverService(DriverRepository driverRepository, RedisTemplate<String, Object> redisTemplate) {
        this.driverRepository = driverRepository;
        this.redisTemplate = redisTemplate;
    }

    private String locationKey(Long driverId) { return "driver:location:" + driverId; }

    @Transactional
    public Driver updateLocation(Long driverId, double lat, double lng) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        driver.setLatitude(lat);
        driver.setLongitude(lng);
        driver.setLocationUpdatedAt(LocalDateTime.now());
        redisTemplate.opsForHash().put(locationKey(driverId), "lat", lat);
        redisTemplate.opsForHash().put(locationKey(driverId), "lng", lng);
        redisTemplate.expire(locationKey(driverId), 10, TimeUnit.SECONDS);
        return driverRepository.save(driver);
    }

    public List<Driver> findNearbyDrivers(double lat, double lng, double radiusKm) {
        return driverRepository.findNearbyAvailableDrivers(lat, lng, radiusKm, 10);
    }

    public Driver getDriver(Long id) {
        return driverRepository.findById(id).orElseThrow(() -> new RuntimeException("Driver not found"));
    }

    @Transactional
    public Driver setStatus(Long driverId, Driver.DriverStatus status) {
        Driver driver = getDriver(driverId);
        driver.setStatus(status);
        return driverRepository.save(driver);
    }

    public Driver createDriver(Driver driver) { return driverRepository.save(driver); }
    public List<Driver> getAllDrivers() { return driverRepository.findAll(); }
}
