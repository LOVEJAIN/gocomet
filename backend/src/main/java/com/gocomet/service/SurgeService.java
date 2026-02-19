package com.gocomet.service;

import com.gocomet.repository.RideRepository;
import com.gocomet.model.Ride;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class SurgeService {

    private final RideRepository rideRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SURGE_KEY = "surge:multiplier";

    public SurgeService(RideRepository rideRepository, RedisTemplate<String, Object> redisTemplate) {
        this.rideRepository = rideRepository;
        this.redisTemplate = redisTemplate;
    }

    public double getSurgeMultiplier() {
        Object cached = redisTemplate.opsForValue().get(SURGE_KEY);
        if (cached != null) return Double.parseDouble(cached.toString());

        long activeRides = rideRepository.countByStatus(Ride.RideStatus.REQUESTED);
        double multiplier;
        if (activeRides > 500) multiplier = 2.5;
        else if (activeRides > 200) multiplier = 2.0;
        else if (activeRides > 100) multiplier = 1.5;
        else multiplier = 1.0;

        redisTemplate.opsForValue().set(SURGE_KEY, multiplier, 30, TimeUnit.SECONDS);
        return multiplier;
    }

    public double calculateFare(double distanceKm, String tier, double surgeMultiplier) {
        double base = switch (tier) {
            case "PREMIUM" -> 30 + (distanceKm * 18);
            case "SUV"     -> 50 + (distanceKm * 22);
            default        -> 20 + (distanceKm * 12);
        };
        return Math.round(base * surgeMultiplier * 100.0) / 100.0;
    }

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100.0) / 100.0;
    }
}
