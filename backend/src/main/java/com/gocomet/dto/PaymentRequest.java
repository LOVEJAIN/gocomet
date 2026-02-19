package com.gocomet.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentRequest {
    @NotNull private Long rideId;
    @NotNull private Long riderId;
    private String idempotencyKey;

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public Long getRiderId() { return riderId; }
    public void setRiderId(Long riderId) { this.riderId = riderId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
