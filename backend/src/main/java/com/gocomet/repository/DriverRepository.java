package com.gocomet.repository;

import com.gocomet.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByStatus(Driver.DriverStatus status);

    // Find nearby available drivers using Haversine formula
    @Query(value = """
        SELECT *, (6371 * acos(
            cos(radians(:lat)) * cos(radians(latitude)) *
            cos(radians(longitude) - radians(:lng)) +
            sin(radians(:lat)) * sin(radians(latitude))
        )) AS distance
        FROM drivers
        WHERE status = 'AVAILABLE'
          AND latitude IS NOT NULL
        HAVING distance < :radiusKm
        ORDER BY distance
        LIMIT :limit
        """, nativeQuery = true)
    List<Driver> findNearbyAvailableDrivers(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radiusKm") double radiusKm,
        @Param("limit") int limit
    );
}
