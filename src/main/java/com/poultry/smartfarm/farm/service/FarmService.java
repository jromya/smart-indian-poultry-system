package com.poultry.smartfarm.farm.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.dto.FarmRequest;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.repository.UserRepository;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import com.poultry.smartfarm.user.service.AuthService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FarmService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public FarmService(FarmRepository farmRepository,
                       UserRepository userRepository,
                       FarmAccessGuard farmAccessGuard,
                       AuditLogService auditLogService) {
        this.farmRepository = farmRepository;
        this.userRepository = userRepository;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public List<Farm> getAccessibleFarms() {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return farmRepository.findAll();
        }
        if (!currentUser.getRole().isFarmRole()) {
            throw new AccessDeniedException("Only farm roles can view confidential farm details.");
        }
        List<String> assignedIds = currentUser.getAssignedFarmIds();
        if (assignedIds == null || assignedIds.isEmpty()) {
            return Collections.emptyList();
        }
        return farmRepository.findByIdIn(assignedIds);
    }

    public Farm getFarmById(String id) {
        farmAccessGuard.requireFarmAccess(id);
        return farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with ID: " + id));
    }

    public Farm createFarm(FarmRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        if (actor.getRole() != Role.SUPER_ADMIN && actor.getRole() != Role.FARM_ADMIN) {
            throw new AccessDeniedException("Only Super Admin or Farm Admin can create a new farm.");
        }

        String code = req.getFarmCode().trim().toUpperCase();
        if (farmRepository.existsByFarmCodeIgnoreCase(code)) {
            throw new BadRequestException("Farm Code '" + code + "' already exists. Please choose another code.");
        }

        Farm farm = new Farm();
        copyFields(req, farm);
        farm.setFarmCode(code);
        farm.setOwnerUserId(actor.getId());

        Farm saved = farmRepository.save(farm);

        // Automatically grant the creating FARM_ADMIN access to their newly created farm
        if (actor.getRole() == Role.FARM_ADMIN) {
            actor.getAssignedFarmIds().add(saved.getId());
            userRepository.save(actor);
        }

        auditLogService.record(actor, saved.getId(), "FARM_CREATED", "FARM", saved.getId(),
                "Created farm '" + saved.getFarmName() + "' (" + saved.getFarmCode() + ")");
        return saved;
    }

    public Farm updateFarm(String id, FarmRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        if (actor.getRole() != Role.SUPER_ADMIN && actor.getRole() != Role.FARM_ADMIN) {
            throw new AccessDeniedException("Only Super Admin or Farm Admin can update farm details.");
        }
        farmAccessGuard.requireFarmWriteAccess(id);

        Farm existing = farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with ID: " + id));

        String newCode = req.getFarmCode().trim().toUpperCase();
        if (!existing.getFarmCode().equalsIgnoreCase(newCode) && farmRepository.existsByFarmCodeIgnoreCase(newCode)) {
            throw new BadRequestException("Farm Code '" + newCode + "' is already used by another farm.");
        }

        copyFields(req, existing);
        existing.setFarmCode(newCode);

        Farm saved = farmRepository.save(existing);
        auditLogService.record(actor, saved.getId(), "FARM_UPDATED", "FARM", saved.getId(),
                "Updated farm '" + saved.getFarmName() + "'");
        return saved;
    }

    private void copyFields(FarmRequest req, Farm farm) {
        farm.setFarmName(req.getFarmName().trim());
        farm.setOwnerName(req.getOwnerName().trim());
        farm.setContactPhone(AuthService.normalizePhone(req.getContactPhone()));
        farm.setContactEmail(req.getContactEmail());
        farm.setVillageOrCity(req.getVillageOrCity().trim());
        farm.setTaluka(req.getTaluka());
        farm.setDistrict(req.getDistrict().trim());
        farm.setState(req.getState().trim());
        farm.setPincode(req.getPincode());
        farm.setFarmCategory(req.getFarmCategory().trim().toUpperCase());
        farm.setTotalAreaSqFt(req.getTotalAreaSqFt());
        farm.setTotalBirdCapacity(req.getTotalBirdCapacity());
        farm.setWaterSource(req.getWaterSource());
        farm.setElectricityBackup(req.getElectricityBackup());
        farm.setRegistrationNumber(req.getRegistrationNumber());
        if (req.getActive() != null) {
            farm.setActive(req.getActive());
        }
    }
}
