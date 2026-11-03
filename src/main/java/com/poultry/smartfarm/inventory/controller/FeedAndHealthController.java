package com.poultry.smartfarm.inventory.controller;

import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.inventory.document.FeedRecord;
import com.poultry.smartfarm.inventory.document.MedicineRecord;
import com.poultry.smartfarm.inventory.document.VaccinationRecord;
import com.poultry.smartfarm.inventory.dto.FeedIssueRequest;
import com.poultry.smartfarm.inventory.dto.MedicineRecordRequest;
import com.poultry.smartfarm.inventory.dto.VaccinationCompleteRequest;
import com.poultry.smartfarm.inventory.dto.VaccinationScheduleRequest;
import com.poultry.smartfarm.inventory.service.FeedAndHealthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FeedAndHealthController {

    private final FeedAndHealthService feedAndHealthService;

    public FeedAndHealthController(FeedAndHealthService feedAndHealthService) {
        this.feedAndHealthService = feedAndHealthService;
    }

    // --- FEED MANAGEMENT ---
    @GetMapping("/feed-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<List<FeedRecord>> listFeedRecords(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String batchId) {
        return ApiResponse.ok(feedAndHealthService.listFeedRecords(farmId, batchId));
    }

    @PostMapping("/feed-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<FeedRecord>> issueFeedToBatch(@Valid @RequestBody FeedIssueRequest req) {
        FeedRecord saved = feedAndHealthService.issueFeedToBatch(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Feed issued to batch & inventory stock deducted automatically!", saved));
    }

    // --- MEDICINE MANAGEMENT ---
    @GetMapping("/medicines")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<List<MedicineRecord>> listMedicines(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String batchId) {
        return ApiResponse.ok(feedAndHealthService.listMedicineRecords(farmId, batchId));
    }

    @PostMapping("/medicines")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<MedicineRecord>> recordMedicine(@Valid @RequestBody MedicineRecordRequest req) {
        MedicineRecord saved = feedAndHealthService.recordMedicineAdministration(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Medicine treatment recorded & stock deducted automatically!", saved));
    }

    // --- VACCINATION SCHEDULE & COMPLETION ---
    @GetMapping("/vaccinations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<List<VaccinationRecord>> listVaccinations(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String batchId) {
        return ApiResponse.ok(feedAndHealthService.listVaccinations(farmId, batchId));
    }

    @PostMapping("/vaccinations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<VaccinationRecord>> addVaccinationSchedule(
            @Valid @RequestBody VaccinationScheduleRequest req) {
        VaccinationRecord saved = feedAndHealthService.addCustomVaccinationSchedule(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Vaccination added to schedule!", saved));
    }

    @PostMapping("/vaccinations/generate-schedule/{batchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<List<VaccinationRecord>>> generateBreedSchedule(@PathVariable String batchId) {
        List<VaccinationRecord> generated = feedAndHealthService.generateBreedSpecificSchedule(batchId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Breed-specific veterinary vaccination schedule generated!", generated));
    }

    @PatchMapping("/vaccinations/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<VaccinationRecord> completeVaccination(
            @PathVariable String id,
            @Valid @RequestBody VaccinationCompleteRequest req) {
        return ApiResponse.ok("Vaccination marked COMPLETED!", feedAndHealthService.completeVaccination(id, req));
    }
}
