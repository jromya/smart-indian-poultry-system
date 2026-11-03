package com.poultry.smartfarm;

import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.model.SupportedLanguage;
import com.poultry.smartfarm.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class Phase1UnitTests {

    @Test
    void testBCryptPasswordEncryption() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String raw = "Admin@12345";
        String hashed = encoder.encode(raw);
        assertNotEquals(raw, hashed);
        assertTrue(encoder.matches(raw, hashed));
        assertFalse(encoder.matches("WrongPass", hashed));
    }

    @Test
    void testSupportedIndianLanguages() {
        assertEquals(11, SupportedLanguage.CODES.size());
        assertEquals("mr", SupportedLanguage.normalizeOrDefault("MR"));
        assertEquals("hi", SupportedLanguage.normalizeOrDefault("hi"));
        assertEquals("ta", SupportedLanguage.normalizeOrDefault("ta"));
        assertEquals("or", SupportedLanguage.normalizeOrDefault("or"));
        assertEquals("en", SupportedLanguage.normalizeOrDefault("unknown_lang"));
    }

    @Test
    void testIndianPhoneNormalization() {
        assertEquals("9876543210", AuthService.normalizePhone("+91-9876543210"));
        assertEquals("9876543210", AuthService.normalizePhone("98765 43210"));
    }

    @Test
    void testRoleFarmScopeClassification() {
        assertTrue(Role.SUPER_ADMIN.isFarmRole());
        assertTrue(Role.FARM_ADMIN.isFarmRole());
        assertTrue(Role.FARM_MANAGER.isFarmRole());
        assertTrue(Role.FARMER.isFarmRole());
        assertFalse(Role.RETAILER.isFarmRole());
        assertFalse(Role.CHICKEN_SHOP_OWNER.isFarmRole());
    }
}
