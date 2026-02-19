package com.gocomet.service;

import com.gocomet.dto.PaymentRequest;
import com.gocomet.model.Payment;
import com.gocomet.model.Ride;
import com.gocomet.repository.PaymentRepository;
import com.gocomet.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;

    public PaymentService(PaymentRepository paymentRepository, RideRepository rideRepository) {
        this.paymentRepository = paymentRepository;
        this.rideRepository = rideRepository;
    }

    @Transactional
    public Payment processPayment(PaymentRequest req) {
        if (req.getIdempotencyKey() != null) {
            Optional<Payment> existing = paymentRepository.findByIdempotencyKey(req.getIdempotencyKey());
            if (existing.isPresent()) return existing.get();
        }
        Ride ride = rideRepository.findById(req.getRideId())
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        if (ride.getStatus() != Ride.RideStatus.COMPLETED)
            throw new RuntimeException("Ride must be COMPLETED before payment");

        Payment payment = new Payment();
        payment.setRideId(req.getRideId());
        payment.setRiderId(req.getRiderId());
        payment.setAmount(ride.getFare());
        payment.setMethod(ride.getPaymentMethod());
        payment.setIdempotencyKey(req.getIdempotencyKey() != null ? req.getIdempotencyKey() : UUID.randomUUID().toString());
        payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);

        log.info("Payment processed for ride {}: {}", req.getRideId(), payment.getAmount());
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getByRideId(Long rideId) { return paymentRepository.findByRideId(rideId); }
}
