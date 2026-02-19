package com.gocomet.controller;

import com.gocomet.dto.ApiResponse;
import com.gocomet.dto.PaymentRequest;
import com.gocomet.model.Payment;
import com.gocomet.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<Payment>> processPayment(@Valid @RequestBody PaymentRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.processPayment(req)));
    }

    @GetMapping("/payments/ride/{rideId}")
    public ResponseEntity<ApiResponse<Object>> getPaymentForRide(@PathVariable Long rideId) {
        return paymentService.getByRideId(rideId)
            .map(p -> ResponseEntity.ok(ApiResponse.ok((Object) p)))
            .orElse(ResponseEntity.ok(ApiResponse.error("Payment not found")));
    }
}
