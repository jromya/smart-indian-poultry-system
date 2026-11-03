package com.poultry.smartfarm.user.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.dto.UserCreateUpdateRequest;
import com.poultry.smartfarm.user.dto.UserResponse;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.model.SupportedLanguage;
import com.poultry.smartfarm.user.repository.UserRepository;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       FarmAccessGuard farmAccessGuard,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public List<UserResponse> listManageableUsers() {
        User actor = farmAccessGuard.getCurrentUser();
        List<User> users;
        if (actor.getRole() == Role.SUPER_ADMIN) {
            users = userRepository.findAll();
        } else if (actor.getRole() == Role.FARM_ADMIN) {
            if (actor.getAssignedFarmIds() == null || actor.getAssignedFarmIds().isEmpty()) {
                users = Collections.singletonList(actor);
            } else {
                users = userRepository.findByAssignedFarmIdsIn(actor.getAssignedFarmIds());
            }
        } else {
            throw new AccessDeniedException("Only Super Admin or Farm Admin can list staff accounts.");
        }

        return users.stream().map(UserResponse::fromDocument).collect(Collectors.toList());
    }

    public UserResponse createUser(UserCreateUpdateRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        validateActorPermissionsForUserTarget(actor, req.getRole(), req.getAssignedFarmIds());

        if (req.getPassword() == null || req.getPassword().trim().length() < 6) {
            throw new BadRequestException("Password of at least 6 characters is required when creating a user.");
        }

        String username = req.getUsername().trim().toLowerCase();
        String phone = AuthService.normalizePhone(req.getPhone());
        String email = (req.getEmail() != null && !req.getEmail().isBlank()) ? req.getEmail().trim().toLowerCase() : null;

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("Username '" + username + "' already exists.");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Mobile number '" + phone + "' already exists.");
        }
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email '" + email + "' already exists.");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword().trim()));
        user.setRole(req.getRole());
        user.setPreferredLanguage(SupportedLanguage.normalizeOrDefault(req.getPreferredLanguage()));
        user.setAssignedFarmIds(req.getAssignedFarmIds());
        user.setBusinessName(req.getBusinessName());
        user.setVillageOrCity(req.getVillageOrCity());
        user.setDistrict(req.getDistrict());
        user.setState(req.getState());
        user.setPincode(req.getPincode());
        user.setActive(req.getActive() == null || req.getActive());
        user.setCreatedBy(actor.getUsername());

        User saved = userRepository.save(user);
        String primaryFarmId = saved.getAssignedFarmIds().isEmpty() ? null : saved.getAssignedFarmIds().get(0);
        auditLogService.record(actor, primaryFarmId, "USER_CREATED", "USER", saved.getId(),
                "Created user " + saved.getUsername() + " with role " + saved.getRole().name());

        return UserResponse.fromDocument(saved);
    }

    public UserResponse updateUser(String userId, UserCreateUpdateRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (actor.getRole() == Role.FARM_ADMIN) {
            boolean sharesFarm = existing.getAssignedFarmIds().stream()
                    .anyMatch(fid -> actor.getAssignedFarmIds().contains(fid));
            if (!sharesFarm || existing.getRole() == Role.SUPER_ADMIN || existing.getRole() == Role.FARM_ADMIN) {
                throw new AccessDeniedException("Farm Admin can only edit Farm Managers and Farmers assigned to their farms.");
            }
        }

        validateActorPermissionsForUserTarget(actor, req.getRole(), req.getAssignedFarmIds());

        String phone = AuthService.normalizePhone(req.getPhone());
        if (!existing.getPhone().equals(phone) && userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Mobile number '" + phone + "' is already used by another user.");
        }

        existing.setFullName(req.getFullName().trim());
        existing.setPhone(phone);
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            existing.setEmail(req.getEmail().trim().toLowerCase());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            if (req.getPassword().trim().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters.");
            }
            existing.setPasswordHash(passwordEncoder.encode(req.getPassword().trim()));
        }
        existing.setRole(req.getRole());
        existing.setPreferredLanguage(SupportedLanguage.normalizeOrDefault(req.getPreferredLanguage()));
        existing.setAssignedFarmIds(req.getAssignedFarmIds());
        existing.setBusinessName(req.getBusinessName());
        existing.setVillageOrCity(req.getVillageOrCity());
        existing.setDistrict(req.getDistrict());
        existing.setState(req.getState());
        existing.setPincode(req.getPincode());
        if (req.getActive() != null) {
            existing.setActive(req.getActive());
        }

        User saved = userRepository.save(existing);
        String primaryFarmId = saved.getAssignedFarmIds().isEmpty() ? null : saved.getAssignedFarmIds().get(0);
        auditLogService.record(actor, primaryFarmId, "USER_UPDATED", "USER", saved.getId(),
                "Updated user " + saved.getUsername() + " (" + saved.getRole().name() + ")");

        return UserResponse.fromDocument(saved);
    }

    public UserResponse toggleStatus(String userId, boolean active) {
        User actor = farmAccessGuard.getCurrentUser();
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (actor.getId().equals(target.getId())) {
            throw new BadRequestException("You cannot deactivate your own account.");
        }

        if (actor.getRole() == Role.FARM_ADMIN) {
            boolean sharesFarm = target.getAssignedFarmIds().stream()
                    .anyMatch(fid -> actor.getAssignedFarmIds().contains(fid));
            if (!sharesFarm || (target.getRole() != Role.FARM_MANAGER && target.getRole() != Role.FARMER)) {
                throw new AccessDeniedException("Farm Admin can only change status of assigned Farm Managers and Farmers.");
            }
        }

        target.setActive(active);
        User saved = userRepository.save(target);
        auditLogService.record(actor, null, active ? "USER_ACTIVATED" : "USER_DEACTIVATED",
                "USER", saved.getId(), "Set active=" + active + " for " + saved.getUsername());
        return UserResponse.fromDocument(saved);
    }

    private void validateActorPermissionsForUserTarget(User actor, Role targetRole, List<String> targetFarmIds) {
        if (actor.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (actor.getRole() == Role.FARM_ADMIN) {
            if (targetRole != Role.FARM_MANAGER && targetRole != Role.FARMER) {
                throw new AccessDeniedException("Farm Admin can only create/manage Farm Manager or Farmer accounts.");
            }
            if (targetFarmIds == null || targetFarmIds.isEmpty()) {
                throw new BadRequestException("Please assign at least one of your farms to this staff member.");
            }
            for (String fid : targetFarmIds) {
                if (!actor.getAssignedFarmIds().contains(fid)) {
                    throw new AccessDeniedException("You cannot assign staff to a farm you do not manage: " + fid);
                }
            }
            return;
        }
        throw new AccessDeniedException("Insufficient permissions to manage users.");
    }
}
