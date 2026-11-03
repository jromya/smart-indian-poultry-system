package com.poultry.smartfarm.user.security;

import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FarmAccessGuard {

    private final UserRepository userRepository;

    public FarmAccessGuard(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Authentication is required to access this resource.");
        }

        if (auth.getPrincipal() instanceof CustomUserDetails details) {
            User user = userRepository.findById(details.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user no longer exists."));
            if (!user.isActive()) {
                throw new AccessDeniedException("User account is disabled.");
            }
            return user;
        }

        String name = auth.getName();
        return userRepository.findByUsernameIgnoreCase(name)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user record not found."));
    }

    public void requireFarmAccess(String farmId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (!currentUser.getRole().isFarmRole()) {
            throw new AccessDeniedException("Your role (" + currentUser.getRole().getDisplayName() + ") does not have farm management access.");
        }
        List<String> assigned = currentUser.getAssignedFarmIds();
        if (assigned == null || !assigned.contains(farmId)) {
            throw new AccessDeniedException("Farm isolation policy: You do not have permission to access data for farm ID: " + farmId);
        }
    }

    public void requireFarmWriteAccess(String farmId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (currentUser.getRole() != Role.FARM_ADMIN && currentUser.getRole() != Role.FARM_MANAGER) {
            throw new AccessDeniedException("Only Super Admin, Farm Admin, or Farm Manager can modify farm or shed settings.");
        }
        List<String> assigned = currentUser.getAssignedFarmIds();
        if (assigned == null || !assigned.contains(farmId)) {
            throw new AccessDeniedException("Farm isolation policy: You cannot modify an unassigned farm.");
        }
    }
}
