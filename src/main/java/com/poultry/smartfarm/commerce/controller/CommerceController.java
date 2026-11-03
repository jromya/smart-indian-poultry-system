package com.poultry.smartfarm.commerce.controller;

import com.poultry.smartfarm.commerce.document.Customer;
import com.poultry.smartfarm.commerce.document.ProcessingRecord;
import com.poultry.smartfarm.commerce.document.RetailerStore;
import com.poultry.smartfarm.commerce.document.WholesalerDeal;
import com.poultry.smartfarm.commerce.dto.ChickenShopProcessRequest;
import com.poultry.smartfarm.commerce.dto.CustomerRequest;
import com.poultry.smartfarm.commerce.dto.RetailStockPurchaseRequest;
import com.poultry.smartfarm.commerce.dto.WholesalerDealRequest;
import com.poultry.smartfarm.commerce.service.CommerceAndShopService;
import com.poultry.smartfarm.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommerceController {

    private final CommerceAndShopService commerceService;

    public CommerceController(CommerceAndShopService commerceService) {
        this.commerceService = commerceService;
    }

    // --- CUSTOMERS (All 5 Buyer Categories) ---
    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ApiResponse<List<Customer>> listCustomers(@RequestParam(required = false) String farmId) {
        return ApiResponse.ok(commerceService.listCustomers(farmId));
    }

    @PostMapping("/customers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@Valid @RequestBody CustomerRequest req) {
        Customer saved = commerceService.createCustomer(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer added successfully!", saved));
    }

    // --- WHOLESALERS ---
    @GetMapping("/wholesalers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ApiResponse<List<WholesalerDeal>> listWholesaleDeals(@RequestParam(required = false) String farmId) {
        return ApiResponse.ok(commerceService.listWholesaleDeals(farmId));
    }

    @PostMapping("/wholesalers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<WholesalerDeal>> recordWholesaleDeal(@Valid @RequestBody WholesalerDealRequest req) {
        WholesalerDeal saved = commerceService.recordWholesaleDeal(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Wholesale live bird dispatch recorded & batch live birds updated!", saved));
    }

    // --- RETAILER BUSINESS MODULE ---
    @GetMapping("/retailers/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'RETAILER')")
    public ApiResponse<Map<String, Object>> getRetailDashboard() {
        return ApiResponse.ok(commerceService.getRetailDashboardData());
    }

    @PostMapping("/retailers/stock-purchase")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'RETAILER')")
    public ResponseEntity<ApiResponse<RetailerStore>> purchaseRetailStock(
            @Valid @RequestBody RetailStockPurchaseRequest req) {
        RetailerStore saved = commerceService.recordRetailStockPurchase(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Retail stock purchased & inventory updated!", saved));
    }

    // --- CHICKEN CUTTING SHOP MODULE (ACTUAL DRESSED WEIGHT — NEVER FIXED YIELD!) ---
    @GetMapping("/chicken-shops/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'CHICKEN_SHOP_OWNER')")
    public ApiResponse<Map<String, Object>> getChickenShopDashboard() {
        return ApiResponse.ok(commerceService.getChickenShopDashboardData());
    }

    @PostMapping("/chicken-shops/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'CHICKEN_SHOP_OWNER')")
    public ResponseEntity<ApiResponse<ProcessingRecord>> recordShopProcessing(
            @Valid @RequestBody ChickenShopProcessRequest req) {
        ProcessingRecord saved = commerceService.recordShopProcessingBatch(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Live bird cutting & actual dressed meat weight recorded!", saved));
    }
}
