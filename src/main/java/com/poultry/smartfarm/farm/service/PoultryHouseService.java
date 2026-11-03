package com.poultry.smartfarm.farm.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.document.PoultryHouse;
import com.poultry.smartfarm.farm.dto.PoultryHouseRequest;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.farm.repository.PoultryHouseRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PoultryHouseService {

    private final PoultryHouseRepository houseRepository;
    private final FarmRepository farmRepository;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public PoultryHouseService(PoultryHouseRepository houseRepository,
                               FarmRepository farmRepository,
                               FarmAccessGuard farmAccessGuard,
                               AuditLogService auditLogService) {
        this.houseRepository = houseRepository;
        this.farmRepository = farmRepository;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public List<PoultryHouse> getAccessibleHouses(String optionalFarmId) {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (optionalFarmId != null && !optionalFarmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(optionalFarmId);
            return houseRepository.findByFarmId(optionalFarmId);
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return houseRepository.findAll();
        }
        if (!currentUser.getRole().isFarmRole()) {
            throw new AccessDeniedException("Only farm roles can view poultry houses.");
        }
        if (currentUser.getAssignedFarmIds() == null || currentUser.getAssignedFarmIds().isEmpty()) {
            return Collections.emptyList();
        }
        return houseRepository.findByFarmIdIn(currentUser.getAssignedFarmIds());
    }

    public PoultryHouse createHouse(PoultryHouseRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        farmAccessGuard.requireFarmWriteAccess(req.getFarmId());

        Farm farm = farmRepository.findById(req.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + req.getFarmId()));

        String code = req.getHouseCode().trim().toUpperCase();
        if (houseRepository.existsByFarmIdAndHouseCodeIgnoreCase(farm.getId(), code)) {
            throw new BadRequestException("Poultry House Code '" + code + "' already exists in farm '" + farm.getFarmName() + "'.");
        }

        PoultryHouse house = new PoultryHouse();
        house.setFarmId(farm.getId());
        house.setFarmName(farm.getFarmName());
        house.setHouseCode(code);
        copyFields(req, house);

        PoultryHouse saved = houseRepository.save(house);
        auditLogService.record(actor, farm.getId(), "POULTRY_HOUSE_CREATED", "POULTRY_HOUSE", saved.getId(),
                "Created shed '" + saved.getHouseName() + "' (" + saved.getHouseCode() + ") in " + farm.getFarmName());
        return saved;
    }

    public PoultryHouse updateHouse(String id, PoultryHouseRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        PoultryHouse existing = houseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poultry House not found: " + id));

        farmAccessGuard.requireFarmWriteAccess(existing.getFarmId());

        String code = req.getHouseCode().trim().toUpperCase();
        if (!existing.getHouseCode().equalsIgnoreCase(code)
                && houseRepository.existsByFarmIdAndHouseCodeIgnoreCase(existing.getFarmId(), code)) {
            throw new BadRequestException("Poultry House Code '" + code + "' already exists in this farm.");
        }

        existing.setHouseCode(code);
        copyFields(req, existing);

        PoultryHouse saved = houseRepository.save(existing);
        auditLogService.record(actor, existing.getFarmId(), "POULTRY_HOUSE_UPDATED", "POULTRY_HOUSE", saved.getId(),
                "Updated shed '" + saved.getHouseName() + "'");
        return saved;
    }

    private void copyFields(PoultryHouseRequest req, PoultryHouse house) {
        house.setHouseName(req.getHouseName().trim());
        house.setHouseType(req.getHouseType().trim().toUpperCase());
        house.setBirdPurpose(req.getBirdPurpose().trim().toUpperCase());
        house.setMaxBirdCapacity(req.getMaxBirdCapacity());
        house.setLengthFeet(req.getLengthFeet());
        house.setWidthFeet(req.getWidthFeet());
        house.setVentilationType(req.getVentilationType());
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            house.setStatus(req.getStatus().trim().toUpperCase());
        }
        house.setNotes(req.getNotes());
    }
}
