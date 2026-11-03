package com.poultry.smartfarm.farm.controller;

import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.dto.FarmRequest;
import com.poultry.smartfarm.farm.service.FarmService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<Farm>>> getAllAccessibleFarms() {
        return ResponseEntity.ok(ApiResponse.ok(farmService.getAccessibleFarms()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<Farm>> getFarmById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(farmService.getFarmById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN')")
    public ResponseEntity<ApiResponse<Farm>> createFarm(@Valid @RequestBody FarmRequest request) {
        Farm created = farmService.createFarm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Farm created successfully!", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN')")
    public ResponseEntity<ApiResponse<Farm>> updateFarm(
            @PathVariable String id,
            @Valid @RequestBody FarmRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Farm updated successfully!", farmService.updateFarm(id, request)));
    }
}
