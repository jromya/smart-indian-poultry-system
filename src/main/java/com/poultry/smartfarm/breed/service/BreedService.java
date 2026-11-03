package com.poultry.smartfarm.breed.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.dto.BreedRequest;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BreedService {

    private final BreedRepository breedRepository;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public BreedService(BreedRepository breedRepository,
                        FarmAccessGuard farmAccessGuard,
                        AuditLogService auditLogService) {
        this.breedRepository = breedRepository;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public List<Breed> listBreeds(String optionalCategory) {
        if (optionalCategory != null && !optionalCategory.isBlank()) {
            return breedRepository.findByBreedCategoryIgnoreCaseOrderByBreedNameAsc(optionalCategory.trim());
        }
        return breedRepository.findAllByOrderByBreedCategoryAscBreedNameAsc();
    }

    public Breed getBreedById(String id) {
        return breedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Breed not found with ID: " + id));
    }

    public Breed createBreed(BreedRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        String code = req.getBreedCode().trim().toUpperCase();
        String name = req.getBreedName().trim();

        if (breedRepository.existsByBreedCodeIgnoreCase(code)) {
            throw new BadRequestException("Breed Code '" + code + "' already exists.");
        }
        if (breedRepository.existsByBreedNameIgnoreCase(name)) {
            throw new BadRequestException("Breed Name '" + name + "' already exists.");
        }

        Breed breed = new Breed();
        copyFields(req, breed);
        breed.setBreedCode(code);
        breed.setBreedName(name);

        Breed saved = breedRepository.save(breed);
        auditLogService.record(actor, null, "BREED_CREATED", "BREED", saved.getId(),
                "Added poultry breed '" + saved.getBreedName() + "' (" + saved.getBreedCategory() + ")");
        return saved;
    }

    public Breed updateBreed(String id, BreedRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Breed existing = getBreedById(id);

        String code = req.getBreedCode().trim().toUpperCase();
        String name = req.getBreedName().trim();

        if (!existing.getBreedCode().equalsIgnoreCase(code) && breedRepository.existsByBreedCodeIgnoreCase(code)) {
            throw new BadRequestException("Breed Code '" + code + "' already exists.");
        }
        if (!existing.getBreedName().equalsIgnoreCase(name) && breedRepository.existsByBreedNameIgnoreCase(name)) {
            throw new BadRequestException("Breed Name '" + name + "' already exists.");
        }

        copyFields(req, existing);
        existing.setBreedCode(code);
        existing.setBreedName(name);

        Breed saved = breedRepository.save(existing);
        auditLogService.record(actor, null, "BREED_UPDATED", "BREED", saved.getId(),
                "Updated poultry breed '" + saved.getBreedName() + "'");
        return saved;
    }

    private void copyFields(BreedRequest req, Breed breed) {
        breed.setLocalLanguageName(req.getLocalLanguageName().trim());
        breed.setLocalizedNames(req.getLocalizedNames());
        breed.setBreedCategory(req.getBreedCategory().trim().toUpperCase());
        breed.setOrigin(req.getOrigin().trim());
        breed.setPrimaryPurpose(req.getPrimaryPurpose().trim().toUpperCase());
        breed.setPurchasePricePerChick(req.getPurchasePricePerChick());
        if (req.getExpectedGrowth() != null) breed.setExpectedGrowth(req.getExpectedGrowth());
        if (req.getEggProduction() != null) breed.setEggProduction(req.getEggProduction());
        if (req.getFeedRequirements() != null) breed.setFeedRequirements(req.getFeedRequirements());
        if (req.getPerformanceTargets() != null) breed.setPerformanceTargets(req.getPerformanceTargets());
        if (req.getActive() != null) breed.setActive(req.getActive());
    }
}
