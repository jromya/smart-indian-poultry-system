package com.poultry.smartfarm.inventory.repository;

import com.poultry.smartfarm.inventory.document.AppNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AppNotificationRepository extends MongoRepository<AppNotification, String> {
    List<AppNotification> findByFarmIdAndAcknowledgedFalseOrderByCreatedAtDesc(String farmId);
    List<AppNotification> findByFarmIdInAndAcknowledgedFalseOrderByCreatedAtDesc(List<String> farmIds);
    List<AppNotification> findByAcknowledgedFalseOrderByCreatedAtDesc();
    Optional<AppNotification> findFirstByFarmIdAndAlertTypeAndReferenceIdAndAcknowledgedFalse(
            String farmId, String alertType, String referenceId);
}
