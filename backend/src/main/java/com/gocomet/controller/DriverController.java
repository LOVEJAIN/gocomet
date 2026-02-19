package com.gocomet.controller;

import com.gocomet.dto.ApiResponse;
import com.gocomet.dto.LocationUpdateRequest;
import com.gocomet.model.Driver;
import com.gocomet.service.DriverService;
import com.gocomet.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/drivers")
public class DriverController {

    private final DriverService driverService;
    private final RideService rideService;

    public DriverController(DriverService driverService, RideService rideService) {
        this.driverService = driverService;
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Driver>> createDriver(@RequestBody Driver driver) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.createDriver(driver)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Driver>>> getAllDrivers() {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getAllDrivers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Driver>> getDriver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getDriver(id)));
    }

    @PostMapping("/{id}/location")
    public ResponseEntity<ApiResponse<Driver>> updateLocation(@PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.updateLocation(id, req.getLatitude(), req.getLongitude())));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<?>> acceptRide(@PathVariable Long id, @RequestParam Long rideId) {
        return ResponseEntity.ok(ApiResponse.ok(rideService.acceptRide(id, rideId)));
    }
}
