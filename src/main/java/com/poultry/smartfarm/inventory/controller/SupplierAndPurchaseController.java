package com.poultry.smartfarm.inventory.controller;

import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.inventory.document.Purchase;
import com.poultry.smartfarm.inventory.document.Supplier;
import com.poultry.smartfarm.inventory.dto.PurchaseRequest;
import com.poultry.smartfarm.inventory.dto.SupplierRequest;
import com.poultry.smartfarm.inventory.service.SupplierAndPurchaseService;
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
@RequestMapping("/api")
public class SupplierAndPurchaseController {

    private final SupplierAndPurchaseService supplierPurchaseService;

    public SupplierAndPurchaseController(SupplierAndPurchaseService supplierPurchaseService) {
        this.supplierPurchaseService = supplierPurchaseService;
    }

    @GetMapping("/suppliers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ApiResponse<List<Supplier>> listSuppliers(@RequestParam(required = false) String farmId) {
        return ApiResponse.ok(supplierPurchaseService.listSuppliers(farmId));
    }

    @PostMapping("/suppliers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<Supplier>> createSupplier(@Valid @RequestBody SupplierRequest req) {
        Supplier saved = supplierPurchaseService.createSupplier(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Supplier added successfully!", saved));
    }

    @PutMapping("/suppliers/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ApiResponse<Supplier> updateSupplier(
            @PathVariable String id,
            @Valid @RequestBody SupplierRequest req) {
        return ApiResponse.ok("Supplier updated!", supplierPurchaseService.updateSupplier(id, req));
    }

    @GetMapping("/purchases")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ApiResponse<List<Purchase>> listPurchases(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String supplierId) {
        return ApiResponse.ok(supplierPurchaseService.listPurchases(farmId, supplierId));
    }

    @PostMapping("/purchases")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<Purchase>> recordPurchase(@Valid @RequestBody PurchaseRequest req) {
        Purchase saved = supplierPurchaseService.recordPurchaseInvoice(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Purchase invoice recorded & inventory stock increased automatically!", saved));
    }
}
