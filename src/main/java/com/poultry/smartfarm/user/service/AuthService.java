package com.poultry.smartfarm.user.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.dto.LoginRequest;
import com.poultry.smartfarm.user.dto.PasswordChangeRequest;
import com.poultry.smartfarm.user.dto.RegisterRequest;
import com.poultry.smartfarm.user.dto.UserResponse;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.model.SupportedLanguage;
import com.poultry.smartfarm.user.repository.UserRepository;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository,
                       FarmAccessGuard farmAccessGuard,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public static String normalizePhone(String rawPhone) {
        if (rawPhone == null) return "";
        String digits = rawPhone.replaceAll("\\D", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits.substring(2);
        }
        return digits;
    }

    public UserResponse register(RegisterRequest req) {
        if (req.getRole() == Role.SUPER_ADMIN || req.getRole() == Role.FARM_MANAGER) {
            throw new BadRequestException("Self-registration is not allowed for " + req.getRole().getDisplayName()
                    + ". Farm Managers must be created by a Farm Admin.");
        }

        String username = req.getUsername().trim().toLowerCase();
        String phone = normalizePhone(req.getPhone());
        String email = (req.getEmail() != null && !req.getEmail().isBlank()) ? req.getEmail().trim().toLowerCase() : null;

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("Username '" + username + "' is already taken.");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Mobile number '" + phone + "' is already registered.");
        }
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email '" + email + "' is already registered.");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setPreferredLanguage(SupportedLanguage.normalizeOrDefault(req.getPreferredLanguage()));
        user.setBusinessName(req.getBusinessName());
        user.setVillageOrCity(req.getVillageOrCity());
        user.setDistrict(req.getDistrict());
        user.setState(req.getState());
        user.setPincode(req.getPincode());
        user.setActive(true);
        user.setCreatedBy("SELF_REGISTRATION");

        User saved = userRepository.save(user);
        auditLogService.record(saved, null, "USER_REGISTER", "USER", saved.getId(),
                "Self-registered user " + saved.getUsername() + " as " + saved.getRole().name());

        return UserResponse.fromDocument(saved);
    }

    public UserResponse login(LoginRequest req, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String identifier = req.getIdentifier().trim();
        String normalizedIdentifier = identifier.matches("^[+\\d\\s-]+$") ? normalizePhone(identifier) : identifier;

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedIdentifier, req.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User currentUser = farmAccessGuard.getCurrentUser();
        auditLogService.record(currentUser, null, "USER_LOGIN", "USER", currentUser.getId(),
                "User " + currentUser.getUsername() + " logged in");

        return UserResponse.fromDocument(currentUser);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    public UserResponse getCurrentUserProfile() {
        return UserResponse.fromDocument(farmAccessGuard.getCurrentUser());
    }

    public UserResponse updateLanguage(String languageCode) {
        User user = farmAccessGuard.getCurrentUser();
        String normalized = SupportedLanguage.normalizeOrDefault(languageCode);
        user.setPreferredLanguage(normalized);
        User saved = userRepository.save(user);
        return UserResponse.fromDocument(saved);
    }

    public void changePassword(PasswordChangeRequest req) {
        User user = farmAccessGuard.getCurrentUser();
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        auditLogService.record(user, null, "PASSWORD_CHANGED", "USER", user.getId(),
                "User changed own password");
    }
}
