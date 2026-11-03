package com.poultry.smartfarm.dashboard.controller;

import com.poultry.smartfarm.audit.document.AuditLog;
import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.commerce.service.LedgerAndFinancialService;
import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.document.PoultryHouse;
import com.poultry.smartfarm.farm.service.FarmService;
import com.poultry.smartfarm.farm.service.PoultryHouseService;
import com.poultry.smartfarm.inventory.document.AppNotification;
import com.poultry.smartfarm.inventory.service.InventoryService;
import com.poultry.smartfarm.inventory.service.NotificationAlertService;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.repository.UserRepository;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final FarmAccessGuard farmAccessGuard;
    private final FarmService farmService;
    private final PoultryHouseService houseService;
    private final BatchService batchService;
    private final InventoryService inventoryService;
    private final NotificationAlertService alertService;
    private final LedgerAndFinancialService financialService;
    private final BreedRepository breedRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public DashboardController(FarmAccessGuard farmAccessGuard,
                               FarmService farmService,
                               PoultryHouseService houseService,
                               BatchService batchService,
                               InventoryService inventoryService,
                               NotificationAlertService alertService,
                               LedgerAndFinancialService financialService,
                               BreedRepository breedRepository,
                               UserRepository userRepository,
                               AuditLogService auditLogService) {
        this.farmAccessGuard = farmAccessGuard;
        this.farmService = farmService;
        this.houseService = houseService;
        this.batchService = batchService;
        this.inventoryService = inventoryService;
        this.alertService = alertService;
        this.financialService = financialService;
        this.breedRepository = breedRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("phase", "Phase 4 - Wholesale, Retail, Chicken Cutting Shop & Financial Profit/Loss");
        status.put("breedsConfigured", breedRepository.count());
        return ApiResponse.ok("Smart Indian Poultry Backend is healthy", status);
    }

    @GetMapping("/dashboard/summary")
    public ApiResponse<Map<String, Object>> getDashboardSummary() {
        User currentUser = farmAccessGuard.getCurrentUser();
        boolean isFarmRole = currentUser.getRole().isFarmRole();

        List<Farm> farms = isFarmRole ? farmService.getAccessibleFarms() : Collections.emptyList();
        List<PoultryHouse> houses = isFarmRole ? houseService.getAccessibleHouses(null) : Collections.emptyList();
        List<BatchResponse> batches = isFarmRole ? batchService.listBatches(null, null, null) : Collections.emptyList();
        Map<String, Object> invSummary = isFarmRole ? inventoryService.getInventoryValuationSummary(null) : Collections.emptyMap();
        List<AppNotification> activeAlerts = isFarmRole ? alertService.getActiveNotifications(null) : Collections.emptyList();
        Map<String, Object> finSummary = (currentUser.getRole() != Role.FARMER)
                ? financialService.calculateFinancialReport(null, null, null, null)
                : Collections.emptyMap();
        List<Breed> allBreeds = breedRepository.findAll();

        int totalBirdCapacity = farms.stream()
                .mapToInt(f -> f.getTotalBirdCapacity() != null ? f.getTotalBirdCapacity() : 0)
                .sum();
        long activeBatchesCount = batches.stream()
                .filter(b -> !"COMPLETED".equalsIgnoreCase(b.getStatus()))
                .count();
        int totalLiveBirds = batches.stream()
                .filter(b -> !"COMPLETED".equalsIgnoreCase(b.getStatus()))
                .mapToInt(BatchResponse::getCurrentLiveBirds)
                .sum();
        int totalEggStock = batches.stream()
                .mapToInt(BatchResponse::getCurrentEggStockGood)
                .sum();

        long indigenousCount = allBreeds.stream().filter(b -> "INDIGENOUS".equalsIgnoreCase(b.getBreedCategory())).count();
        long ruralCount = allBreeds.stream().filter(b -> "IMPROVED_RURAL".equalsIgnoreCase(b.getBreedCategory())).count();
        long commercialCount = allBreeds.stream().filter(b -> "COMMERCIAL".equalsIgnoreCase(b.getBreedCategory())).count();

        long manageableUsersCount = 0;
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            manageableUsersCount = userRepository.count();
        } else if (currentUser.getRole() == Role.FARM_ADMIN && !currentUser.getAssignedFarmIds().isEmpty()) {
            manageableUsersCount = userRepository.findByAssignedFarmIdsIn(currentUser.getAssignedFarmIds()).size();
        }

        List<AuditLog> recentAudit = auditLogService.getRecentLogsForUser(currentUser);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("userRole", currentUser.getRole().name());
        summary.put("roleDisplayName", currentUser.getRole().getDisplayName());
        summary.put("totalFarms", farms.size());
        summary.put("totalPoultryHouses", houses.size());
        summary.put("totalBirdCapacity", totalBirdCapacity);
        summary.put("activeBatches", activeBatchesCount);
        summary.put("totalLiveBirds", totalLiveBirds);
        summary.put("totalEggStockGood", totalEggStock);
        summary.put("inventoryValuationInr", invSummary.getOrDefault("totalValuationInr", 0.0));
        summary.put("lowStockItemsCount", invSummary.getOrDefault("lowStockItemsCount", 0));
        summary.put("expiringItemsCount", invSummary.getOrDefault("expiringSoonOrExpiredCount", 0));
        summary.put("activeAlertsCount", activeAlerts.size());
        summary.put("activeAlerts", activeAlerts);
        summary.put("financialOverview", finSummary);
        summary.put("totalBreeds", allBreeds.size());
        summary.put("indigenousBreeds", indigenousCount);
        summary.put("improvedRuralBreeds", ruralCount);
        summary.put("commercialBreeds", commercialCount);
        summary.put("manageableUsers", manageableUsersCount);
        summary.put("recentAuditLogs", recentAudit);
        summary.put("batches", batches);
        return ApiResponse.ok(summary);
    }
}
