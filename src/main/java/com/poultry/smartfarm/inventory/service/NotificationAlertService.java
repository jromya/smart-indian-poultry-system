package com.poultry.smartfarm.inventory.service;

import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.inventory.document.AppNotification;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.document.VaccinationRecord;
import com.poultry.smartfarm.inventory.repository.AppNotificationRepository;
import com.poultry.smartfarm.inventory.repository.InventoryItemRepository;
import com.poultry.smartfarm.inventory.repository.VaccinationRecordRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationAlertService {

    private final AppNotificationRepository notificationRepository;
    private final InventoryItemRepository inventoryRepository;
    private final VaccinationRecordRepository vaccinationRepository;
    private final FarmRepository farmRepository;
    private final FarmAccessGuard farmAccessGuard;

    public NotificationAlertService(AppNotificationRepository notificationRepository,
                                    InventoryItemRepository inventoryRepository,
                                    VaccinationRecordRepository vaccinationRepository,
                                    FarmRepository farmRepository,
                                    FarmAccessGuard farmAccessGuard) {
        this.notificationRepository = notificationRepository;
        this.inventoryRepository = inventoryRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.farmRepository = farmRepository;
        this.farmAccessGuard = farmAccessGuard;
    }

    public List<AppNotification> getActiveNotifications(String farmId) {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            refreshFarmAlertsInternal(farmId);
            return notificationRepository.findByFarmIdAndAcknowledgedFalseOrderByCreatedAtDesc(farmId);
        }

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            farmRepository.findAll().forEach(f -> refreshFarmAlertsInternal(f.getId()));
            return notificationRepository.findByAcknowledgedFalseOrderByCreatedAtDesc();
        }

        if (!currentUser.getRole().isFarmRole() || currentUser.getAssignedFarmIds() == null || currentUser.getAssignedFarmIds().isEmpty()) {
            return Collections.emptyList();
        }

        currentUser.getAssignedFarmIds().forEach(this::refreshFarmAlertsInternal);
        return notificationRepository.findByFarmIdInAndAcknowledgedFalseOrderByCreatedAtDesc(currentUser.getAssignedFarmIds());
    }

    public AppNotification acknowledgeNotification(String notificationId) {
        User actor = farmAccessGuard.getCurrentUser();
        AppNotification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (n.getFarmId() != null) {
            farmAccessGuard.requireFarmAccess(n.getFarmId());
        }
        n.setAcknowledged(true);
        n.setAcknowledgedBy(actor.getUsername());
        n.setAcknowledgedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    public void refreshFarmAlertsInternal(String farmId) {
        if (farmId == null || farmId.isBlank()) return;
        String farmName = farmRepository.findById(farmId).map(Farm::getFarmName).orElse("Farm");
        LocalDate today = LocalDate.now();

        List<InventoryItem> items = inventoryRepository.findByFarmIdOrderByCategoryAscItemNameAsc(farmId);
        for (InventoryItem item : items) {
            if (!item.isActive()) continue;

            // 1. Low Stock Alert
            boolean isLow = item.getCurrentStock() <= item.getReorderLevel();
            if (isLow) {
                String sev = item.getCurrentStock() <= 0 ? "CRITICAL" : "WARNING";
                String title = "Low Stock: " + item.getItemName();
                String msg = item.getItemName() + " (" + item.getCategory() + ") balance is "
                        + item.getCurrentStock() + " " + item.getUnit()
                        + " (Reorder threshold: " + item.getReorderLevel() + " " + item.getUnit() + ").";
                upsertAlert(farmId, farmName, "LOW_STOCK", sev, title, msg, "INVENTORY_ITEM", item.getId());
            } else {
                autoResolveAlert(farmId, "LOW_STOCK", item.getId());
            }

            // 2. Expiry Alerts (Expired or Expiring within 30 days)
            if (item.getExpiryDate() != null && item.getCurrentStock() > 0) {
                long daysUntilExpiry = ChronoUnit.DAYS.between(today, item.getExpiryDate());
                if (daysUntilExpiry < 0) {
                    upsertAlert(farmId, farmName, "EXPIRED_ITEM", "CRITICAL",
                            "EXPIRED Stock: " + item.getItemName(),
                            item.getItemName() + " (Lot " + (item.getBatchLotNumber() != null ? item.getBatchLotNumber() : "N/A")
                                    + ") expired on " + item.getExpiryDate() + "! Do not administer to birds.",
                            "INVENTORY_ITEM", item.getId());
                    autoResolveAlert(farmId, "EXPIRING_SOON", item.getId());
                } else if (daysUntilExpiry <= 30) {
                    upsertAlert(farmId, farmName, "EXPIRING_SOON", "WARNING",
                            "Expiring in " + daysUntilExpiry + "d: " + item.getItemName(),
                            item.getItemName() + " expires on " + item.getExpiryDate()
                                    + " (" + item.getCurrentStock() + " " + item.getUnit() + " remaining).",
                            "INVENTORY_ITEM", item.getId());
                } else {
                    autoResolveAlert(farmId, "EXPIRED_ITEM", item.getId());
                    autoResolveAlert(farmId, "EXPIRING_SOON", item.getId());
                }
            }
        }

        // 3. Vaccination Due / Overdue Alerts
        List<VaccinationRecord> vaxList = vaccinationRepository.findByFarmIdOrderByScheduledDateAsc(farmId);
        for (VaccinationRecord v : vaxList) {
            if ("COMPLETED".equalsIgnoreCase(v.getStatus()) || "SKIPPED".equalsIgnoreCase(v.getStatus())) {
                autoResolveAlert(farmId, "VACCINATION_DUE", v.getId());
                continue;
            }
            if (v.getScheduledDate() == null) continue;
            long daysUntil = ChronoUnit.DAYS.between(today, v.getScheduledDate());
            if (daysUntil < 0) {
                if (!"OVERDUE".equalsIgnoreCase(v.getStatus())) {
                    v.setStatus("OVERDUE");
                    vaccinationRepository.save(v);
                }
                upsertAlert(farmId, farmName, "VACCINATION_DUE", "CRITICAL",
                        "OVERDUE Vaccine: " + v.getVaccineName(),
                        v.getVaccineName() + " for batch " + v.getBatchCode() + " was scheduled on " + v.getScheduledDate() + ".",
                        "VACCINATION_RECORD", v.getId());
            } else if (daysUntil <= 3) {
                upsertAlert(farmId, farmName, "VACCINATION_DUE", "WARNING",
                        "Upcoming Vaccine: " + v.getVaccineName(),
                        v.getVaccineName() + " for batch " + v.getBatchCode() + " is due on " + v.getScheduledDate()
                                + " (in " + daysUntil + " day(s)).",
                        "VACCINATION_RECORD", v.getId());
            }
        }
    }

    private void upsertAlert(String farmId, String farmName, String alertType, String severity,
                             String title, String message, String refType, String refId) {
        Optional<AppNotification> existing = notificationRepository
                .findFirstByFarmIdAndAlertTypeAndReferenceIdAndAcknowledgedFalse(farmId, alertType, refId);
        AppNotification alert = existing.orElseGet(AppNotification::new);
        alert.setFarmId(farmId);
        alert.setFarmName(farmName);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setReferenceType(refType);
        alert.setReferenceId(refId);
        if (alert.getCreatedAt() == null) {
            alert.setCreatedAt(LocalDateTime.now());
        }
        notificationRepository.save(alert);
    }

    private void autoResolveAlert(String farmId, String alertType, String refId) {
        notificationRepository.findFirstByFarmIdAndAlertTypeAndReferenceIdAndAcknowledgedFalse(farmId, alertType, refId)
                .ifPresent(alert -> {
                    alert.setAcknowledged(true);
                    alert.setAcknowledgedBy("SYSTEM_AUTO_RESOLVED");
                    alert.setAcknowledgedAt(LocalDateTime.now());
                    notificationRepository.save(alert);
                });
    }
}
