package com.poultry.smartfarm.farm.controller;

import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.farm.document.PoultryHouse;
import com.poultry.smartfarm.farm.dto.PoultryHouseRequest;
import com.poultry.smartfarm.farm.service.PoultryHouseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/poultry-houses")
public class PoultryHouseController {

    private final PoultryHouseService houseService;

    public PoultryHouseController(PoultryHouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<PoultryHouse>>> getPoultryHouses(
            @RequestParam(required = false) String farmId) {
        return ResponseEntity.ok(ApiResponse.ok(houseService.getAccessibleHouses(farmId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<PoultryHouse>> createPoultryHouse(
            @Valid @RequestBody PoultryHouseRequest request) {
        PoultryHouse created = houseService.createHouse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Poultry house created successfully!", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<PoultryHouse>> updatePoultryHouse(
            @PathVariable String id,
            @Valid @RequestBody PoultryHouseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Poultry house updated successfully!", houseService.updateHouse(id, request)));
    }
}
