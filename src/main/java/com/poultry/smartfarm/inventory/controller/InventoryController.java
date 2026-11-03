package com.poultry.smartfarm.inventory.controller;

import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.inventory.document.AppNotification;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.document.StockMovement;
import com.poultry.smartfarm.inventory.dto.InventoryItemRequest;
import com.poultry.smartfarm.inventory.dto.StockAdjustmentRequest;
import com.poultry.smartfarm.inventory.dto.StockIssueRequest;
import com.poultry.smartfarm.inventory.dto.StockTransferRequest;
import com.poultry.smartfarm.inventory.service.InventoryService;
import com.poultry.smartfarm.inventory.service.NotificationAlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;
    private final NotificationAlertService alertService;

    public InventoryController(InventoryService inventoryService,
                               NotificationAlertService alertService) {
        this.inventoryService = inventoryService;
        this.alertService = alertService;
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<List<InventoryItem>> listInventory(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String category) {
        return ApiResponse.ok(inventoryService.listInventoryItems(farmId, category));
    }

    @GetMapping("/inventory/valuation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<Map<String, Object>> getValuation(@RequestParam(required = false) String farmId) {
        return ApiResponse.ok(inventoryService.getInventoryValuationSummary(farmId));
    }

    @PostMapping("/inventory")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryItem>> createItem(@Valid @RequestBody InventoryItemRequest req) {
        InventoryItem created = inventoryService.createInventoryItem(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Inventory item created successfully!", created));
    }

    @PutMapping("/inventory/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ApiResponse<InventoryItem> updateItem(
            @PathVariable String id,
            @Valid @RequestBody InventoryItemRequest req) {
        return ApiResponse.ok("Inventory item updated!", inventoryService.updateInventoryItem(id, req));
    }

    @PostMapping("/inventory/issue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<InventoryItem> issueStock(@Valid @RequestBody StockIssueRequest req) {
        return ApiResponse.ok("Stock issued and balance reduced!", inventoryService.issueStockDirect(req));
    }

    @PostMapping("/inventory/adjust")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ApiResponse<InventoryItem> adjustStock(@Valid @RequestBody StockAdjustmentRequest req) {
        return ApiResponse.ok("Stock adjusted and logged in stock movements!", inventoryService.adjustStock(req));
    }

    @PostMapping("/inventory/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ApiResponse<Map<String, InventoryItem>> transferStock(@Valid @RequestBody StockTransferRequest req) {
        return ApiResponse.ok("Stock transferred between farms!", inventoryService.transferStockBetweenFarms(req));
    }

    @GetMapping("/inventory/movements")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<List<StockMovement>> getMovements(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String inventoryItemId) {
        return ApiResponse.ok(inventoryService.getStockMovements(farmId, inventoryItemId));
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<List<AppNotification>> getActiveAlerts(@RequestParam(required = false) String farmId) {
        return ApiResponse.ok(alertService.getActiveNotifications(farmId));
    }

    @PatchMapping("/notifications/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<AppNotification> acknowledgeAlert(@PathVariable String id) {
        return ApiResponse.ok("Alert acknowledged", alertService.acknowledgeNotification(id));
    }
}
