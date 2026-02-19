package com.gocomet.repository;

import com.gocomet.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {
    Optional<Ride> findByIdempotencyKey(String key);
    List<Ride> findByRiderIdOrderByRequestedAtDesc(Long riderId);
    List<Ride> findByDriverIdAndStatus(Long driverId, Ride.RideStatus status);
    long countByStatus(Ride.RideStatus status);
}
