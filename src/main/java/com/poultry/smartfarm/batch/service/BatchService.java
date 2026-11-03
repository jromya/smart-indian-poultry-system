package com.poultry.smartfarm.batch.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.dto.BatchCreateRequest;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import com.poultry.smartfarm.batch.dto.BirdHarvestSaleRequest;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.document.PoultryHouse;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.farm.repository.PoultryHouseRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final PoultryHouseRepository houseRepository;
    private final BreedRepository breedRepository;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;
    private Consumer<Batch> onBatchCompletedHook;

    public BatchService(BatchRepository batchRepository,
                        FarmRepository farmRepository,
                        PoultryHouseRepository houseRepository,
                        BreedRepository breedRepository,
                        FarmAccessGuard farmAccessGuard,
                        AuditLogService auditLogService) {
        this.batchRepository = batchRepository;
        this.farmRepository = farmRepository;
        this.houseRepository = houseRepository;
        this.breedRepository = breedRepository;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public List<BatchResponse> listBatches(String farmId, String category, String status) {
        User user = farmAccessGuard.getCurrentUser();
        List<Batch> batches;
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            batches = batchRepository.findByFarmIdOrderByChickArrivalDateDesc(farmId);
        } else if (user.getRole() == Role.SUPER_ADMIN) {
            batches = batchRepository.findAllByOrderByChickArrivalDateDesc();
        } else if (user.getRole().isFarmRole()) {
            if (user.getAssignedFarmIds() == null || user.getAssignedFarmIds().isEmpty()) {
                batches = Collections.emptyList();
            } else {
                batches = batchRepository.findByFarmIdInOrderByChickArrivalDateDesc(user.getAssignedFarmIds());
            }
        } else {
            throw new AccessDeniedException("Only farm roles can access batch management.");
        }

        return batches.stream()
                .filter(b -> category == null || category.isBlank() || category.equalsIgnoreCase(b.getBatchCategory()))
                .filter(b -> status == null || status.isBlank() || status.equalsIgnoreCase(b.getStatus()))
                .map(BatchResponse::fromDocument)
                .collect(Collectors.toList());
    }

    public Batch getBatchDocumentWithAccess(String batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + batchId));
        farmAccessGuard.requireFarmAccess(batch.getFarmId());
        return batch;
    }

    public BatchResponse getBatchById(String batchId) {
        return BatchResponse.fromDocument(getBatchDocumentWithAccess(batchId));
    }

    public void ensureBatchEditable(Batch batch) {
        if ("COMPLETED".equalsIgnoreCase(batch.getStatus())) {
            throw new BadRequestException("Batch '" + batch.getBatchCode()
                    + "' is COMPLETED and locked against ordinary edits. An Admin can reopen it with an audit reason if correction is required.");
        }
    }

    public BatchResponse createBatch(BatchCreateRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        if (actor.getRole() != Role.SUPER_ADMIN && actor.getRole() != Role.FARM_ADMIN && actor.getRole() != Role.FARM_MANAGER) {
            throw new AccessDeniedException("Only Super Admin, Farm Admin, or Farm Manager can create a poultry batch.");
        }
        farmAccessGuard.requireFarmWriteAccess(req.getFarmId());

        String code = req.getBatchCode().trim().toUpperCase();
        if (batchRepository.existsByBatchCodeIgnoreCase(code)) {
            throw new BadRequestException("Batch ID '" + code + "' already exists. Choose a unique Batch ID.");
        }

        Farm farm = farmRepository.findById(req.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found."));
        PoultryHouse house = houseRepository.findById(req.getPoultryHouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Poultry house not found."));
        if (!house.getFarmId().equals(farm.getId())) {
            throw new BadRequestException("Selected poultry house does not belong to the selected farm.");
        }
        if (house.getMaxBirdCapacity() != null && req.getStartingBirdQuantity() > house.getMaxBirdCapacity()) {
            throw new BadRequestException("Starting birds (" + req.getStartingBirdQuantity()
                    + ") exceed shed capacity (" + house.getMaxBirdCapacity() + ").");
        }

        Breed breed = breedRepository.findById(req.getBreedId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected breed not found in Breed Master."));

        // Never force 50 days: default planned duration from Breed's standardMarketAgeDays if not specified
        int defaultDuration = (breed.getExpectedGrowth() != null && breed.getExpectedGrowth().getStandardMarketAgeDays() != null)
                ? breed.getExpectedGrowth().getStandardMarketAgeDays()
                : ("LAYER".equalsIgnoreCase(req.getBatchCategory()) ? 504 : ("INDIGENOUS".equalsIgnoreCase(req.getBatchCategory()) ? 120 : 42));
        int plannedDuration = (req.getPlannedDurationDays() != null && req.getPlannedDurationDays() > 0)
                ? req.getPlannedDurationDays()
                : defaultDuration;

        double chickPrice = (req.getPurchasePricePerChick() != null)
                ? req.getPurchasePricePerChick()
                : (breed.getPurchasePricePerChick() != null ? breed.getPurchasePricePerChick() : 40.0);

        double initialChickWeight = req.getChickInitialWeightGrams() != null
                ? req.getChickInitialWeightGrams()
                : ("INDIGENOUS".equalsIgnoreCase(breed.getBreedCategory()) ? 32.0 : 42.0);

        Batch batch = new Batch();
        batch.setBatchCode(code);
        batch.setFarmId(farm.getId());
        batch.setFarmName(farm.getFarmName());
        batch.setPoultryHouseId(house.getId());
        batch.setPoultryHouseName(house.getHouseName());
        batch.setBatchName(req.getBatchName().trim());
        batch.setBreedId(breed.getId());
        batch.setBreedCode(breed.getBreedCode());
        batch.setBreedName(breed.getBreedName());
        batch.setBatchCategory(req.getBatchCategory().trim().toUpperCase());
        batch.setPurchaseDate(req.getPurchaseDate() != null ? req.getPurchaseDate() : req.getChickArrivalDate());
        batch.setChickArrivalDate(req.getChickArrivalDate());
        batch.setStartingBirdQuantity(req.getStartingBirdQuantity());
        batch.setPurchasePricePerChick(chickPrice);
        batch.setSupplierName(req.getSupplierName());
        batch.setInitialBirdAgeDays(req.getInitialBirdAgeDays() != null ? req.getInitialBirdAgeDays() : 0);
        batch.setPlannedDurationDays(plannedDuration);
        batch.setChickInitialWeightGrams(initialChickWeight);
        batch.setLatestAverageWeightGrams(initialChickWeight);
        if (req.getEstimatedFeedRatePerKg() != null) batch.setEstimatedFeedRatePerKg(req.getEstimatedFeedRatePerKg());
        if (req.getLiveSellingPricePerKg() != null) batch.setLiveSellingPricePerKg(req.getLiveSellingPricePerKg());
        batch.setStatus("ACTIVE");
        batch.setNotes(req.getNotes());

        Batch saved = batchRepository.save(batch);
        syncHouseOccupancy(house.getId());

        auditLogService.record(actor, farm.getId(), "BATCH_CREATED", "BATCH", saved.getId(),
                "Created batch " + saved.getBatchCode() + " (" + saved.getBreedName() + ", "
                        + saved.getStartingBirdQuantity() + " birds, " + plannedDuration + "d planned)");
        return BatchResponse.fromDocument(saved);
    }

    public BatchResponse updatePricingTargets(String batchId, Double livePricePerKg, Double feedRatePerKg) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = getBatchDocumentWithAccess(batchId);
        farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());
        ensureBatchEditable(batch);

        if (livePricePerKg != null && livePricePerKg > 0) batch.setLiveSellingPricePerKg(livePricePerKg);
        if (feedRatePerKg != null && feedRatePerKg > 0) batch.setEstimatedFeedRatePerKg(feedRatePerKg);
        Batch saved = batchRepository.save(batch);

        auditLogService.record(actor, batch.getFarmId(), "BATCH_PRICING_UPDATED", "BATCH", saved.getId(),
                "Updated pricing targets for " + saved.getBatchCode());
        return BatchResponse.fromDocument(saved);
    }

    public BatchResponse recordBirdHarvestSale(String batchId, BirdHarvestSaleRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = getBatchDocumentWithAccess(batchId);
        farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());
        ensureBatchEditable(batch);

        BatchResponse currentView = BatchResponse.fromDocument(batch);
        if (req.getBirdsSold() > currentView.getCurrentLiveBirds()) {
            throw new BadRequestException("Cannot sell " + req.getBirdsSold() + " birds; only "
                    + currentView.getCurrentLiveBirds() + " live birds currently remain in batch.");
        }

        double saleAmount = Math.round(req.getTotalLiveWeightKg() * req.getSellingPricePerKg() * 100.0) / 100.0;
        batch.setTotalBirdsSold(batch.getTotalBirdsSold() + req.getBirdsSold());
        batch.setTotalSoldLiveWeightKg(Math.round((batch.getTotalSoldLiveWeightKg() + req.getTotalLiveWeightKg()) * 100.0) / 100.0);
        batch.setTotalBirdSalesRevenue(Math.round((batch.getTotalBirdSalesRevenue() + saleAmount) * 100.0) / 100.0);
        batch.setLiveSellingPricePerKg(req.getSellingPricePerKg());

        Batch saved = batchRepository.save(batch);
        syncHouseOccupancy(saved.getPoultryHouseId());

        auditLogService.record(actor, batch.getFarmId(), "BATCH_BIRD_SALE", "BATCH", saved.getId(),
                "Sold " + req.getBirdsSold() + " birds (" + req.getTotalLiveWeightKg() + " kg @ ₹"
                        + req.getSellingPricePerKg() + "/kg) from " + saved.getBatchCode());
        return BatchResponse.fromDocument(saved);
    }

    public void setOnBatchCompletedHook(Consumer<Batch> onBatchCompletedHook) {
        this.onBatchCompletedHook = onBatchCompletedHook;
    }

    public BatchResponse completeBatch(String batchId, LocalDate completionDate) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = getBatchDocumentWithAccess(batchId);
        farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());

        batch.setStatus("COMPLETED");
        batch.setActualCompletionDate(completionDate != null ? completionDate : LocalDate.now());
        Batch saved = batchRepository.save(batch);
        syncHouseOccupancy(saved.getPoultryHouseId());

        auditLogService.record(actor, batch.getFarmId(), "BATCH_COMPLETED", "BATCH", saved.getId(),
                "Completed batch " + saved.getBatchCode() + " on " + saved.getActualCompletionDate());

        if (this.onBatchCompletedHook != null) {
            this.onBatchCompletedHook.accept(saved);
        }
        return BatchResponse.fromDocument(saved);
    }

    public BatchResponse reopenBatch(String batchId, String reason) {
        User actor = farmAccessGuard.getCurrentUser();
        if (actor.getRole() != Role.SUPER_ADMIN && actor.getRole() != Role.FARM_ADMIN) {
            throw new AccessDeniedException("Only Super Admin or Farm Admin can reopen a completed batch.");
        }
        if (reason == null || reason.trim().length() < 5) {
            throw new BadRequestException("A clear audit reason (minimum 5 characters) is required to reopen a completed batch.");
        }
        Batch batch = getBatchDocumentWithAccess(batchId);
        farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());

        batch.setStatus("REOPENED");
        batch.setReopenReason(reason.trim());
        Batch saved = batchRepository.save(batch);
        syncHouseOccupancy(saved.getPoultryHouseId());

        auditLogService.record(actor, batch.getFarmId(), "BATCH_REOPENED", "BATCH", saved.getId(),
                "Reopened completed batch " + saved.getBatchCode() + ". Reason: " + reason.trim());
        return BatchResponse.fromDocument(saved);
    }

    public void syncHouseOccupancy(String poultryHouseId) {
        if (poultryHouseId == null) return;
        houseRepository.findById(poultryHouseId).ifPresent(house -> {
            List<Batch> activeBatches = batchRepository.findByPoultryHouseIdAndStatusIn(
                    poultryHouseId, List.of("ACTIVE", "REOPENED"));
            int totalLive = activeBatches.stream()
                    .mapToInt(b -> BatchResponse.fromDocument(b).getCurrentLiveBirds())
                    .sum();
            house.setCurrentBirdOccupancy(totalLive);
            house.setStatus(totalLive > 0 ? "OCCUPIED" : "CLEANING_REST");
            houseRepository.save(house);
        });
    }
}
