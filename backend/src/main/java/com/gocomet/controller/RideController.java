package com.gocomet.controller;

import com.gocomet.dto.ApiResponse;
import com.gocomet.dto.CreateRideRequest;
import com.gocomet.model.Ride;
import com.gocomet.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) { this.rideService = rideService; }

    @PostMapping("/rides")
    public ResponseEntity<ApiResponse<Ride>> createRide(@Valid @RequestBody CreateRideRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.createRide(req)));
    }

    @GetMapping("/rides/{id}")
    public ResponseEntity<ApiResponse<Ride>> getRide(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.getRide(id)));
    }

    @GetMapping("/rides")
    public ResponseEntity<ApiResponse<List<Ride>>> getAllRides() {
        return ResponseEntity.ok(ApiResponse.ok(rideService.getAllRides()));
    }

    @PostMapping("/trips/{id}/start")
    public ResponseEntity<ApiResponse<Ride>> startTrip(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.startTrip(id)));
    }

    @PostMapping("/trips/{id}/end")
    public ResponseEntity<ApiResponse<Ride>> endTrip(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.endTrip(id)));
    }

    @PostMapping("/rides/{id}/cancel")
    public ResponseEntity<ApiResponse<Ride>> cancelRide(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.cancelRide(id)));
    }
}
