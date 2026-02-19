package com.gocomet.repository;

import com.gocomet.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdempotencyKey(String key);
    Optional<Payment> findByRideId(Long rideId);
}
