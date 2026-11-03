package com.poultry.smartfarm.user.controller;

import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.user.dto.LanguageUpdateRequest;
import com.poultry.smartfarm.user.dto.LoginRequest;
import com.poultry.smartfarm.user.dto.PasswordChangeRequest;
import com.poultry.smartfarm.user.dto.RegisterRequest;
import com.poultry.smartfarm.user.dto.UserResponse;
import com.poultry.smartfarm.user.model.SupportedLanguage;
import com.poultry.smartfarm.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account registered successfully! You can now log in.", created));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        UserResponse loggedIn = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.ok("Welcome back, " + loggedIn.getFullName() + "!", loggedIn));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentUserProfile()));
    }

    @PatchMapping("/language")
    public ResponseEntity<ApiResponse<UserResponse>> updatePreferredLanguage(
            @Valid @RequestBody LanguageUpdateRequest request) {
        UserResponse updated = authService.updateLanguage(request.getPreferredLanguage());
        return ResponseEntity.ok(ApiResponse.ok("Preferred language updated.", updated));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully.", null));
    }

    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSupportedLanguages() {
        return ResponseEntity.ok(ApiResponse.ok(SupportedLanguage.LANGUAGES));
    }
}
