package com.poultry.smartfarm.commerce.controller;

import com.poultry.smartfarm.commerce.document.Expense;
import com.poultry.smartfarm.commerce.document.PaymentRecord;
import com.poultry.smartfarm.commerce.document.Sale;
import com.poultry.smartfarm.commerce.dto.ExpenseRequest;
import com.poultry.smartfarm.commerce.dto.PaymentRequest;
import com.poultry.smartfarm.commerce.dto.SaleRequest;
import com.poultry.smartfarm.commerce.service.LedgerAndFinancialService;
import com.poultry.smartfarm.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FinancialController {

    private final LedgerAndFinancialService financialService;

    public FinancialController(LedgerAndFinancialService financialService) {
        this.financialService = financialService;
    }

    // --- SALES INVOICES ---
    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ApiResponse<List<Sale>> listSales(@RequestParam(required = false) String farmId) {
        return ApiResponse.ok(financialService.listSales(farmId));
    }

    @PostMapping("/sales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ResponseEntity<ApiResponse<Sale>> recordSale(@Valid @RequestBody SaleRequest req) {
        Sale saved = financialService.recordSaleInvoice(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Sales invoice recorded!", saved));
    }

    // --- OPERATING EXPENSES ---
    @GetMapping("/expenses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ApiResponse<List<Expense>> listExpenses(@RequestParam(required = false) String farmId) {
        return ApiResponse.ok(financialService.listExpenses(farmId));
    }

    @PostMapping("/expenses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ResponseEntity<ApiResponse<Expense>> recordExpense(@Valid @RequestBody ExpenseRequest req) {
        Expense saved = financialService.recordExpense(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Operating expense recorded!", saved));
    }

    // --- CUSTOMER RECEIPTS & SUPPLIER PAYMENTS ---
    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ApiResponse<List<PaymentRecord>> listPayments(@RequestParam(required = false) String farmId) {
        return ApiResponse.ok(financialService.listPayments(farmId));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ResponseEntity<ApiResponse<PaymentRecord>> recordPayment(@Valid @RequestBody PaymentRequest req) {
        PaymentRecord saved = financialService.recordPaymentSettlement(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Payment settled & party outstanding balance updated!", saved));
    }

    // --- PROFIT / LOSS & FINANCIAL SUMMARY REPORT (DAILY / BATCH / FARM / MONTHLY / YEARLY) ---
    @GetMapping("/finance/report")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'RETAILER', 'CHICKEN_SHOP_OWNER')")
    public ApiResponse<Map<String, Object>> getFinancialReport(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.ok(financialService.calculateFinancialReport(farmId, batchId, fromDate, toDate));
    }
}
