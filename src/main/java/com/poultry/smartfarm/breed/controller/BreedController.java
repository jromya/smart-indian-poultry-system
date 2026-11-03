package com.poultry.smartfarm.breed.controller;

import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.dto.BreedRequest;
import com.poultry.smartfarm.breed.service.BreedService;
import com.poultry.smartfarm.common.dto.ApiResponse;
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
@RequestMapping("/api/breeds")
public class BreedController {

    private final BreedService breedService;

    public BreedController(BreedService breedService) {
        this.breedService = breedService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Breed>>> getAllBreeds(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.ok(breedService.listBreeds(category)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Breed>> getBreedById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(breedService.getBreedById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN')")
    public ResponseEntity<ApiResponse<Breed>> createBreed(@Valid @RequestBody BreedRequest request) {
        Breed created = breedService.createBreed(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Breed added to master catalog!", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN')")
    public ResponseEntity<ApiResponse<Breed>> updateBreed(
            @PathVariable String id,
            @Valid @RequestBody BreedRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Breed updated successfully!", breedService.updateBreed(id, request)));
    }
}
