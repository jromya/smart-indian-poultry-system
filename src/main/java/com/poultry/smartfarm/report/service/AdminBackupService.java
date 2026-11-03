package com.poultry.smartfarm.report.service;

import com.poultry.smartfarm.audit.repository.AuditLogRepository;
import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.repository.EggRecordRepository;
import com.poultry.smartfarm.batch.repository.MortalityRecordRepository;
import com.poultry.smartfarm.batch.repository.WeightRecordRepository;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.commerce.repository.ChickenShopRepository;
import com.poultry.smartfarm.commerce.repository.CustomerRepository;
import com.poultry.smartfarm.commerce.repository.ExpenseRepository;
import com.poultry.smartfarm.commerce.repository.PaymentRecordRepository;
import com.poultry.smartfarm.commerce.repository.ProcessingRecordRepository;
import com.poultry.smartfarm.commerce.repository.RetailerStoreRepository;
import com.poultry.smartfarm.commerce.repository.SaleRepository;
import com.poultry.smartfarm.commerce.repository.WholesalerDealRepository;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.farm.repository.PoultryHouseRepository;
import com.poultry.smartfarm.inventory.repository.AppNotificationRepository;
import com.poultry.smartfarm.inventory.repository.FeedRecordRepository;
import com.poultry.smartfarm.inventory.repository.InventoryItemRepository;
import com.poultry.smartfarm.inventory.repository.MedicineRecordRepository;
import com.poultry.smartfarm.inventory.repository.PurchaseRepository;
import com.poultry.smartfarm.inventory.repository.StockMovementRepository;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.inventory.repository.VaccinationRecordRepository;
import com.poultry.smartfarm.report.repository.BatchReportRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.repository.UserRepository;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminBackupService {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final PoultryHouseRepository houseRepository;
    private final BreedRepository breedRepository;
    private final AuditLogRepository auditLogRepository;
    private final BatchRepository batchRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final MortalityRecordRepository mortalityRepository;
    private final WeightRecordRepository weightRepository;
    private final EggRecordRepository eggRepository;
    private final InventoryItemRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final FeedRecordRepository feedRepository;
    private final MedicineRecordRepository medicineRepository;
    private final VaccinationRecordRepository vaccinationRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;
    private final AppNotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final WholesalerDealRepository wholesalerRepository;
    private final RetailerStoreRepository retailerRepository;
    private final ChickenShopRepository chickenShopRepository;
    private final ProcessingRecordRepository processingRepository;
    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRecordRepository paymentRepository;
    private final BatchReportRepository batchReportRepository;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public AdminBackupService(UserRepository userRepository,
                              FarmRepository farmRepository,
                              PoultryHouseRepository houseRepository,
                              BreedRepository breedRepository,
                              AuditLogRepository auditLogRepository,
                              BatchRepository batchRepository,
                              DailyRecordRepository dailyRecordRepository,
                              MortalityRecordRepository mortalityRepository,
                              WeightRecordRepository weightRepository,
                              EggRecordRepository eggRepository,
                              InventoryItemRepository inventoryRepository,
                              StockMovementRepository stockMovementRepository,
                              FeedRecordRepository feedRepository,
                              MedicineRecordRepository medicineRepository,
                              VaccinationRecordRepository vaccinationRepository,
                              SupplierRepository supplierRepository,
                              PurchaseRepository purchaseRepository,
                              AppNotificationRepository notificationRepository,
                              CustomerRepository customerRepository,
                              WholesalerDealRepository wholesalerRepository,
                              RetailerStoreRepository retailerRepository,
                              ChickenShopRepository chickenShopRepository,
                              ProcessingRecordRepository processingRepository,
                              SaleRepository saleRepository,
                              ExpenseRepository expenseRepository,
                              PaymentRecordRepository paymentRepository,
                              BatchReportRepository batchReportRepository,
                              FarmAccessGuard farmAccessGuard,
                              AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.farmRepository = farmRepository;
        this.houseRepository = houseRepository;
        this.breedRepository = breedRepository;
        this.auditLogRepository = auditLogRepository;
        this.batchRepository = batchRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.mortalityRepository = mortalityRepository;
        this.weightRepository = weightRepository;
        this.eggRepository = eggRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.feedRepository = feedRepository;
        this.medicineRepository = medicineRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
        this.wholesalerRepository = wholesalerRepository;
        this.retailerRepository = retailerRepository;
        this.chickenShopRepository = chickenShopRepository;
        this.processingRepository = processingRepository;
        this.saleRepository = saleRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.batchReportRepository = batchReportRepository;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    private User requireSuperAdmin() {
        User actor = farmAccessGuard.getCurrentUser();
        if (actor.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN can inspect or export full system database backups.");
        }
        return actor;
    }

    public Map<String, Object> getCollectionHealthSummary() {
        requireSuperAdmin();

        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("users", userRepository.count());
        counts.put("farms", farmRepository.count());
        counts.put("poultry_houses", houseRepository.count());
        counts.put("breeds", breedRepository.count());
        counts.put("audit_logs", auditLogRepository.count());
        counts.put("batches", batchRepository.count());
        counts.put("daily_records", dailyRecordRepository.count());
        counts.put("mortality_records", mortalityRepository.count());
        counts.put("weight_records", weightRepository.count());
        counts.put("egg_records", eggRepository.count());
        counts.put("inventory_items", inventoryRepository.count());
        counts.put("stock_movements", stockMovementRepository.count());
        counts.put("feed_records", feedRepository.count());
        counts.put("medicine_records", medicineRepository.count());
        counts.put("vaccination_records", vaccinationRepository.count());
        counts.put("suppliers", supplierRepository.count());
        counts.put("purchases", purchaseRepository.count());
        counts.put("notifications", notificationRepository.count());
        counts.put("customers", customerRepository.count());
        counts.put("wholesalers", wholesalerRepository.count());
        counts.put("retailers", retailerRepository.count());
        counts.put("chicken_shops", chickenShopRepository.count());
        counts.put("processing_records", processingRepository.count());
        counts.put("sales", saleRepository.count());
        counts.put("expenses", expenseRepository.count());
        counts.put("payments", paymentRepository.count());
        counts.put("batch_reports", batchReportRepository.count());

        long totalDocs = counts.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalCollections", counts.size());
        res.put("totalDocuments", totalDocs);
        res.put("collections", counts);
        res.put("checkedAt", LocalDateTime.now());
        return res;
    }

    public Map<String, Object> exportFullDatabaseSnapshot() {
        User actor = requireSuperAdmin();

        List<Map<String, Object>> sanitizedUsers = userRepository.findAll().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("fullName", u.getFullName());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("role", u.getRole());
            m.put("preferredLanguage", u.getPreferredLanguage());
            m.put("assignedFarmIds", u.getAssignedFarmIds());
            m.put("state", u.getState());
            m.put("district", u.getDistrict());
            m.put("active", u.isActive());
            m.put("passwordHash", "[REDACTED_BCRYPT_HASH]");
            return m;
        }).toList();

        Map<String, Object> collections = new LinkedHashMap<>();
        collections.put("users", sanitizedUsers);
        collections.put("farms", farmRepository.findAll());
        collections.put("poultry_houses", houseRepository.findAll());
        collections.put("breeds", breedRepository.findAll());
        collections.put("audit_logs", auditLogRepository.findAll());
        collections.put("batches", batchRepository.findAll());
        collections.put("daily_records", dailyRecordRepository.findAll());
        collections.put("mortality_records", mortalityRepository.findAll());
        collections.put("weight_records", weightRepository.findAll());
        collections.put("egg_records", eggRepository.findAll());
        collections.put("inventory_items", inventoryRepository.findAll());
        collections.put("stock_movements", stockMovementRepository.findAll());
        collections.put("feed_records", feedRepository.findAll());
        collections.put("medicine_records", medicineRepository.findAll());
        collections.put("vaccination_records", vaccinationRepository.findAll());
        collections.put("suppliers", supplierRepository.findAll());
        collections.put("purchases", purchaseRepository.findAll());
        collections.put("notifications", notificationRepository.findAll());
        collections.put("customers", customerRepository.findAll());
        collections.put("wholesalers", wholesalerRepository.findAll());
        collections.put("retailers", retailerRepository.findAll());
        collections.put("chicken_shops", chickenShopRepository.findAll());
        collections.put("processing_records", processingRepository.findAll());
        collections.put("sales", saleRepository.findAll());
        collections.put("expenses", expenseRepository.findAll());
        collections.put("payments", paymentRepository.findAll());
        collections.put("batch_reports", batchReportRepository.findAll());

        auditLogService.record(actor, null, "SYSTEM_BACKUP_EXPORTED", "SYSTEM_BACKUP", "ALL_27_COLLECTIONS",
                "Exported full 27-collection JSON database backup (passwords redacted).");

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("system", "Smart Indian Poultry Farm Management System");
        snapshot.put("version", "1.0.0");
        snapshot.put("exportedAt", LocalDateTime.now());
        snapshot.put("exportedBy", actor.getUsername());
        snapshot.put("totalCollections", collections.size());
        snapshot.put("collections", collections);
        return snapshot;
    }
}
