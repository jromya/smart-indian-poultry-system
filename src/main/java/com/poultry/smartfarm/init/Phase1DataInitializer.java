package com.poultry.smartfarm.init;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.document.PoultryHouse;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.farm.repository.PoultryHouseRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Order(1)
public class Phase1DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Phase1DataInitializer.class);

    private final BreedRepository breedRepository;
    private final FarmRepository farmRepository;
    private final PoultryHouseRepository houseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Value("${app.security.seed-demo-users:true}")
    private boolean seedDemoUsers;

    @Value("${app.security.super-admin-username:superadmin}")
    private String superAdminUsername;

    @Value("${app.security.super-admin-password:Admin@12345}")
    private String superAdminPassword;

    public Phase1DataInitializer(BreedRepository breedRepository,
                                 FarmRepository farmRepository,
                                 PoultryHouseRepository houseRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuditLogService auditLogService) {
        this.breedRepository = breedRepository;
        this.farmRepository = farmRepository;
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Override
    public void run(String... args) {
        seedBreedsIfMissing();
        seedSuperAdminAndSampleEcosystem();
    }

    private void seedBreedsIfMissing() {
        List<Breed> defaultBreeds = List.of(
                // --- 8 INDIGENOUS INDIAN BREEDS ---
                buildBreed("IND-ASEEL", "Aseel", "असील / अस्सल",
                        Map.of("hi", "असील", "mr", "अस्सल", "te", "అసీల్", "ta", "असील / கட்டு சேவல்"),
                        "INDIGENOUS", "Andhra Pradesh, Telangana & Chhattisgarh", "DUAL_PURPOSE", 65.0,
                        new Breed.GrowthInfo(150, 1850.0, 3500.0, 2200.0, "Slow muscular growth; prized high-value desi table meat"),
                        new Breed.EggInfo(true, 195, 45, 28.0, 46.0, "Tinted Brown"),
                        new Breed.FeedInfo(28, 84, 95.0, 7.2, true, "Desi chick starter (0-4 wk), grower + free-range grain foraging"),
                        new Breed.PerformanceInfo(3.8, 4.0, "20°C - 38°C", "Hardy heat-tolerant indigenous breed; keep males separated after maturity")),

                buildBreed("IND-KADAKNATH", "Kadaknath", "कड़कनाथ / काळीमासी",
                        Map.of("hi", "कड़कनाथ", "mr", "काळीमासी / कडकनाथ", "gu", "કડકનાથ", "mp", "कड़कनाथ"),
                        "INDIGENOUS", "Jhabua & Alirajpur, Madhya Pradesh (GI Tagged)", "DUAL_PURPOSE", 70.0,
                        new Breed.GrowthInfo(120, 1350.0, 1800.0, 1300.0, "Medium-slow indigenous growth; medicinal melanin-rich black meat"),
                        new Breed.EggInfo(true, 180, 105, 45.0, 44.0, "Light Pinkish Tinted"),
                        new Breed.FeedInfo(28, 75, 85.0, 4.8, true, "Starter mash (0-4 wk), semi-intensive grower + green azolla/forage"),
                        new Breed.PerformanceInfo(3.4, 4.5, "18°C - 38°C", "High protein (>25%), low cholesterol premium black meat")),

                buildBreed("IND-GHAGUS", "Ghagus", "घागुस",
                        Map.of("hi", "घागुस", "kn", "ಘಾಗುಸ್", "te", "ఘాгуస్", "mr", "घागुस"),
                        "INDIGENOUS", "Kolar (Karnataka) & Chittoor/Anantapur (Andhra Pradesh)", "DUAL_PURPOSE", 45.0,
                        new Breed.GrowthInfo(135, 1450.0, 2150.0, 1450.0, "Steady rural backyard growth under minimal supplementation"),
                        new Breed.EggInfo(true, 175, 60, 35.0, 43.0, "Light Brown"),
                        new Breed.FeedInfo(28, 84, 80.0, 5.1, true, "Backyard scavenging + broken rice/maize evening ration"),
                        new Breed.PerformanceInfo(3.6, 5.0, "20°C - 37°C", "Strong maternal brooding instinct; disease resistant")),

                buildBreed("IND-NICOBARI", "Nicobari", "निकोबारी",
                        Map.of("hi", "निकोबारी", "bn", "নিকোবরী", "ta", "நிக்கோபாரி", "mr", "निकोबारी"),
                        "INDIGENOUS", "Andaman & Nicobar Islands", "EGG", 50.0,
                        new Breed.GrowthInfo(130, 1300.0, 1750.0, 1250.0, "Compact short-legged island body type"),
                        new Breed.EggInfo(true, 165, 155, 62.0, 47.0, "Pinkish White / Tinted"),
                        new Breed.FeedInfo(28, 90, 88.0, 5.0, true, "Layer grower + coastal/island backyard foraging"),
                        new Breed.PerformanceInfo(3.2, 4.0, "22°C - 35°C", "Highest egg producer among indigenous Indian breeds; resistant to Ranikhet")),

                buildBreed("IND-ANKALESHWAR", "Ankaleshwar", "अंकलेश्वर",
                        Map.of("gu", "અંકલેશ્વર દેશી", "hi", "अंकलेश्वर", "mr", "अंकलेश्वर"),
                        "INDIGENOUS", "Bharuch & Narmada Districts, Gujarat", "DUAL_PURPOSE", 45.0,
                        new Breed.GrowthInfo(130, 1400.0, 1800.0, 1450.0, "Hardy tribal free-range growth"),
                        new Breed.EggInfo(true, 178, 80, 42.0, 43.0, "Cream / Light Brown"),
                        new Breed.FeedInfo(28, 84, 82.0, 4.9, true, "Grain scratch + free-range pasture"),
                        new Breed.PerformanceInfo(3.5, 4.5, "18°C - 40°C", "Thrives in hot semi-arid Gujarat climate")),

                buildBreed("IND-MIRI", "Miri", "मिरी",
                        Map.of("hi", "मिरी", "bn", "মিরি", "mr", "मिरी"),
                        "INDIGENOUS", "Lakhimpur, Dhemaji & Majuli, Upper Assam", "DUAL_PURPOSE", 42.0,
                        new Breed.GrowthInfo(140, 1300.0, 1650.0, 1250.0, "Small agile bird adapted to high-rainfall floodplains"),
                        new Breed.EggInfo(true, 175, 70, 38.0, 41.0, "Light Brown"),
                        new Breed.FeedInfo(28, 84, 78.0, 4.7, true, "Paddy gleanings + homestead kitchen feed"),
                        new Breed.PerformanceInfo(3.6, 5.0, "15°C - 34°C", "High humidity tolerance in North-East India")),

                buildBreed("IND-HANSLI", "Hansli", "हंसली",
                        Map.of("or", "ହଂসଳୀ (Hansli)", "hi", "हंसली", "bn", "হাঁসলি"),
                        "INDIGENOUS", "Mayurbhanj & Keonjhar Districts, Odisha", "DUAL_PURPOSE", 55.0,
                        new Breed.GrowthInfo(145, 1750.0, 2500.0, 1750.0, "Tall, muscular indigenous bird with tasty game-style meat"),
                        new Breed.EggInfo(true, 185, 65, 34.0, 45.0, "Brownish Tinted"),
                        new Breed.FeedInfo(28, 84, 92.0, 6.2, true, "Maize-rice polish + free-range forest-edge foraging"),
                        new Breed.PerformanceInfo(3.7, 4.5, "18°C - 38°C", "Strong disease resistance in eastern plateau zone")),

                buildBreed("IND-KALASTHI", "Kalasthi", "कालास्ती",
                        Map.of("te", "కాళహస్తి కోడి", "ta", "காளஹஸ்தி", "hi", "कालास्ती"),
                        "INDIGENOUS", "Srikalahasti / Chittoor, Andhra Pradesh", "DUAL_PURPOSE", 52.0,
                        new Breed.GrowthInfo(140, 1600.0, 2350.0, 1650.0, "Erect posture, bluish-black plumage with golden neck"),
                        new Breed.EggInfo(true, 185, 50, 32.0, 43.0, "Brown"),
                        new Breed.FeedInfo(28, 84, 88.0, 5.6, true, "Millets (ragi/bajra) + semi-intensive range"),
                        new Breed.PerformanceInfo(3.7, 4.5, "20°C - 39°C", "Highly valued local table delicacy in Rayalaseema")),

                // --- 4 IMPROVED RURAL POULTRY VARIETIES ---
                buildBreed("RUR-VANARAJA", "Vanaraja", "वनराजा",
                        Map.of("hi", "वनराजा", "mr", "वनराजा", "te", "వనరాజ", "kn", "ವನರಾಜ"),
                        "IMPROVED_RURAL", "ICAR - Directorate of Poultry Research, Hyderabad", "DUAL_PURPOSE", 38.0,
                        new Breed.GrowthInfo(84, 1800.0, 2800.0, 2000.0, "Fast rural growth reaching 1.5-1.8 kg by 12 weeks"),
                        new Breed.EggInfo(true, 165, 120, 58.0, 54.0, "Attractive Brown"),
                        new Breed.FeedInfo(28, 56, 105.0, 4.5, true, "Nursery brooding feed (0-4 wk), then semi-intensive backyard"),
                        new Breed.PerformanceInfo(2.5, 4.0, "18°C - 36°C", "Avoid excess obesity in hens to maintain 110-130 annual eggs")),

                buildBreed("RUR-GRAMAPRIYA", "Gramapriya", "ग्रामप्रिया",
                        Map.of("hi", "ग्रामप्रिया", "mr", "ग्रामप्रिया", "te", "గ్రామప్రియ", "ta", "கிராமப்பிரியா"),
                        "IMPROVED_RURAL", "ICAR - Directorate of Poultry Research, Hyderabad", "EGG", 40.0,
                        new Breed.GrowthInfo(112, 1450.0, 2000.0, 1550.0, "Light-to-medium body ideal for predator evasion & egg production"),
                        new Breed.EggInfo(true, 155, 210, 76.0, 56.0, "Tinted Brown (Desi Look)"),
                        new Breed.FeedInfo(28, 112, 108.0, 4.2, true, "Balanced chick/grower mash + calcium grit during laying"),
                        new Breed.PerformanceInfo(2.4, 3.5, "18°C - 36°C", "Produces 200+ desi-style tinted eggs/year earning premium rural price")),

                buildBreed("RUR-GIRIRAJA", "Giriraja", "गिरिराजा",
                        Map.of("kn", "ಗಿರಿರಾಜ", "hi", "गिरिराजा", "mr", "गिरिराजा", "ml", "ഗിരിരാജ"),
                        "IMPROVED_RURAL", "KVAFSU, Bengaluru, Karnataka", "DUAL_PURPOSE", 40.0,
                        new Breed.GrowthInfo(75, 1950.0, 3200.0, 2300.0, "Heavy multi-colored rural bird; rapid early weight gain"),
                        new Breed.EggInfo(true, 160, 140, 62.0, 55.0, "Brown"),
                        new Breed.FeedInfo(28, 56, 115.0, 4.6, true, "Starter mash (0-4 wk) + farm grain & green fodder"),
                        new Breed.PerformanceInfo(2.4, 4.0, "18°C - 35°C", "Popular across South & Western India for both meat and brown eggs")),

                buildBreed("RUR-SRINIDHI", "Srinidhi", "श्रीनिधी",
                        Map.of("te", "శ్రీనిధి", "hi", "श्रीनिधि", "mr", "श्रीनिधी", "or", "ଶ୍ରୀନିଧି"),
                        "IMPROVED_RURAL", "ICAR - Directorate of Poultry Research, Hyderabad", "DUAL_PURPOSE", 39.0,
                        new Breed.GrowthInfo(90, 1750.0, 2600.0, 1950.0, "Multi-colored plumage with fast juvenile growth"),
                        new Breed.EggInfo(true, 160, 150, 66.0, 54.0, "Tinted / Brown"),
                        new Breed.FeedInfo(28, 70, 105.0, 4.4, true, "Nursery starter (0-6 wk) + low-cost rural ration"),
                        new Breed.PerformanceInfo(2.45, 3.8, "18°C - 36°C", "Excellent dual-purpose rural income bird")),

                // --- COMMERCIAL VARIETIES ---
                buildBreed("COM-ROSS308", "Ross 308", "रॉस 308 (ब्रॉयलर)",
                        Map.of("hi", "रॉस 308", "mr", "रॉस 308", "pa", "ਰੌਸ 308", "gu", "રોસ 308"),
                        "COMMERCIAL", "Aviagen Commercial Broiler Line", "MEAT", 38.0,
                        new Breed.GrowthInfo(40, 2400.0, 2650.0, 2250.0, "High-yield commercial broiler; ~2.4 kg in 38-42 days"),
                        new Breed.EggInfo(false, null, 0, 0.0, 0.0, "N/A"),
                        new Breed.FeedInfo(14, 28, 165.0, 3.88, false, "Pre-starter/Starter (0-14d), Grower (15-28d), Finisher pellet (29d+)"),
                        new Breed.PerformanceInfo(1.62, 3.0, "21°C - 28°C (Brooding 32°C)", "Requires strict biosecurity, cool drinking water & ventilation")),

                buildBreed("COM-COBB500", "Cobb 500", "कॉब 500 (ब्रॉयलर)",
                        Map.of("hi", "कॉब 500", "mr", "कॉब 500", "pa", "ਕੌਬ 500", "te", "కాబ్ 500"),
                        "COMMERCIAL", "Cobb-Vantress Commercial Broiler Line", "MEAT", 39.0,
                        new Breed.GrowthInfo(40, 2380.0, 2600.0, 2200.0, "Superior feed conversion commercial broiler on Indian maize-soya diets"),
                        new Breed.EggInfo(false, null, 0, 0.0, 0.0, "N/A"),
                        new Breed.FeedInfo(14, 28, 162.0, 3.80, false, "Starter crumble (0-14d), Grower pellet (15-28d), Finisher pellet (29d+)"),
                        new Breed.PerformanceInfo(1.60, 3.0, "21°C - 28°C (Brooding 32°C)", "Most widely reared broiler in Maharashtra, Telangana & Punjab")),

                buildBreed("COM-HUBBARD", "Hubbard", "हबर्ड (ब्रॉयलर)",
                        Map.of("hi", "हबर्ड", "mr", "हबर्ड", "ta", "ஹப்பர்ட்", "bn", "হাবার্ড"),
                        "COMMERCIAL", "Hubbard Commercial Broiler Line", "MEAT", 37.0,
                        new Breed.GrowthInfo(42, 2420.0, 2680.0, 2260.0, "Robust commercial broiler tolerant to tropical Indian shed climates"),
                        new Breed.EggInfo(false, null, 0, 0.0, 0.0, "N/A"),
                        new Breed.FeedInfo(14, 28, 165.0, 3.98, false, "Starter (0-14d), Grower (15-28d), Finisher (29-42d)"),
                        new Breed.PerformanceInfo(1.65, 3.2, "21°C - 30°C", "Strong leg health and heat resilience in open-sided Indian sheds")),

                buildBreed("COM-BV300", "BV-300 Commercial Layer", "बीव्ही-300 (अंडी लेयर)",
                        Map.of("hi", "बीवी-300 लेयर", "mr", "बीव्ही-300 लेयर", "te", "బివి-300 లేయర్", "ta", "பிவி-300 லேயர்"),
                        "COMMERCIAL", "Venkateshwara Research & Breeding Farm (Pune, India)", "EGG", 46.0,
                        new Breed.GrowthInfo(504, 1520.0, 1850.0, 1520.0, "Long 72-100 week commercial egg production cycle"),
                        new Breed.EggInfo(true, 130, 330, 95.0, 58.0, "Pure White"),
                        new Breed.FeedInfo(56, 126, 108.0, 42.0, false, "Chick mash (0-8 wk), Grower (9-18 wk), Phase-1/2 Layer mash (19-72+ wk)"),
                        new Breed.PerformanceInfo(2.05, 5.0, "20°C - 30°C", "India's leading commercial white-egg layer; long production batch up to 500+ days"))
        );

        int inserted = 0;
        for (Breed b : defaultBreeds) {
            if (!breedRepository.existsByBreedCodeIgnoreCase(b.getBreedCode())) {
                breedRepository.save(b);
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Seeded {} Indian & Commercial Chicken Breeds into 'breeds' collection.", inserted);
        }
    }

    private Breed buildBreed(String code, String name, String localName, Map<String, String> localized,
                             String category, String origin, String purpose, Double price,
                             Breed.GrowthInfo growth, Breed.EggInfo egg, Breed.FeedInfo feed, Breed.PerformanceInfo perf) {
        Breed b = new Breed();
        b.setBreedCode(code);
        b.setBreedName(name);
        b.setLocalLanguageName(localName);
        b.setLocalizedNames(localized);
        b.setBreedCategory(category);
        b.setOrigin(origin);
        b.setPrimaryPurpose(purpose);
        b.setPurchasePricePerChick(price);
        b.setExpectedGrowth(growth);
        b.setEggProduction(egg);
        b.setFeedRequirements(feed);
        b.setPerformanceTargets(perf);
        b.setActive(true);
        return b;
    }

    private void seedSuperAdminAndSampleEcosystem() {
        User superAdmin = userRepository.findByUsernameIgnoreCase(superAdminUsername).orElseGet(() -> {
            User admin = new User();
            admin.setFullName("System Super Administrator");
            admin.setUsername(superAdminUsername.toLowerCase());
            admin.setEmail("superadmin@smartindianpoultry.in");
            admin.setPhone("9800000001");
            admin.setPasswordHash(passwordEncoder.encode(superAdminPassword));
            admin.setRole(Role.SUPER_ADMIN);
            admin.setPreferredLanguage("en");
            admin.setVillageOrCity("Pune");
            admin.setDistrict("Pune");
            admin.setState("Maharashtra");
            admin.setActive(true);
            admin.setCreatedBy("SYSTEM_INIT");
            User saved = userRepository.save(admin);
            log.info("Created default SUPER_ADMIN account: username='{}'", saved.getUsername());
            return saved;
        });

        if (!seedDemoUsers || farmRepository.count() > 0) {
            return;
        }

        // Seed Farm 1: Nashik Mixed Broiler + Layer + Vanaraja Farm
        Farm farm1 = new Farm();
        farm1.setFarmCode("FARM-MH-001");
        farm1.setFarmName("Sahyadri Krushi Poultry Farm");
        farm1.setOwnerName("Rajesh Patil");
        farm1.setContactPhone("9800000002");
        farm1.setContactEmail("rajesh.patil@sahyadripoultry.in");
        farm1.setVillageOrCity("Dindori");
        farm1.setTaluka("Dindori");
        farm1.setDistrict("Nashik");
        farm1.setState("Maharashtra");
        farm1.setPincode("422202");
        farm1.setFarmCategory("MIXED");
        farm1.setTotalAreaSqFt(28000.0);
        farm1.setTotalBirdCapacity(12000);
        farm1.setWaterSource("BOREWELL");
        farm1.setElectricityBackup("SOLAR");
        farm1.setRegistrationNumber("MH-NSK-PF-2026-104");
        farm1.setActive(true);
        farm1 = farmRepository.save(farm1);

        // Seed Farm 2: Jhabua Indigenous Kadaknath & Aseel Farm (for testing Farm Isolation!)
        Farm farm2 = new Farm();
        farm2.setFarmCode("FARM-MP-002");
        farm2.setFarmName("Satpura GI Kadaknath & Desi Breeder Farm");
        farm2.setOwnerName("Vikram Bhuria");
        farm2.setContactPhone("9800000007");
        farm2.setVillageOrCity("Thandla");
        farm2.setTaluka("Thandla");
        farm2.setDistrict("Jhabua");
        farm2.setState("Madhya Pradesh");
        farm2.setPincode("457777");
        farm2.setFarmCategory("INDIGENOUS");
        farm2.setTotalAreaSqFt(18000.0);
        farm2.setTotalBirdCapacity(4500);
        farm2.setWaterSource("WELL");
        farm2.setElectricityBackup("SOLAR");
        farm2.setRegistrationNumber("MP-JHB-GI-2026-019");
        farm2.setActive(true);
        farm2 = farmRepository.save(farm2);

        // Seed Poultry Houses for Farm 1 & Farm 2
        createSampleHouse(farm1, "SHED-01", "North EC Broiler Shed 1", "EC_SHED", "BROILER", 5000, 120.0, 42.0, "TUNNEL_FAN_PAD");
        createSampleHouse(farm1, "SHED-02", "East Elevated Layer House", "CAGE_SYSTEM", "LAYER", 4500, 110.0, 36.0, "NATURAL_OPEN");
        createSampleHouse(farm1, "SHED-03", "South Vanaraja Backyard Unit", "FREE_RANGE_DESI", "INDIGENOUS", 2500, 90.0, 35.0, "NATURAL_OPEN");
        createSampleHouse(farm2, "SHED-K1", "Kadaknath Semi-Intensive House 1", "FREE_RANGE_DESI", "INDIGENOUS", 3000, 100.0, 35.0, "NATURAL_OPEN");

        // Seed Demo Accounts for all remaining roles
        createDemoUser("Rajesh Patil (Farm Admin)", "farmadmin1", "9800000002", "Farm@12345",
                Role.FARM_ADMIN, "mr", List.of(farm1.getId()), "Sahyadri Krushi Poultry Farm", "Dindori", "Nashik", "Maharashtra");

        createDemoUser("Suresh Deshmukh (Farm Manager)", "manager1", "9800000003", "Manager@123",
                Role.FARM_MANAGER, "hi", List.of(farm1.getId()), "Sahyadri Krushi Poultry Farm", "Dindori", "Nashik", "Maharashtra");

        createDemoUser("Tukaram Shinde (Farmer)", "farmer1", "9800000004", "Farmer@123",
                Role.FARMER, "mr", List.of(farm1.getId()), "Sahyadri Krushi Poultry Farm", "Dindori", "Nashik", "Maharashtra");

        createDemoUser("Anand Egg & Bird Mart (Retailer)", "retailer1", "9800000005", "Retail@123",
                Role.RETAILER, "hi", List.of(), "Anand Fresh Eggs & Desi Birds", "Panchavati", "Nashik", "Maharashtra");

        createDemoUser("Bismillah & Fresh Choice Chicken Shop", "shopowner1", "9800000006", "Shop@12345",
                Role.CHICKEN_SHOP_OWNER, "en", List.of(), "Fresh Choice Hygienic Chicken Cutting Center", "Cidco", "Nashik", "Maharashtra");

        createDemoUser("Vikram Bhuria (MP Farm Admin)", "farmadmin2", "9800000007", "Farm@12345",
                Role.FARM_ADMIN, "hi", List.of(farm2.getId()), "Satpura GI Kadaknath Farm", "Thandla", "Jhabua", "Madhya Pradesh");

        auditLogService.record(superAdmin, farm1.getId(), "SYSTEM_SEED", "SYSTEM", farm1.getId(),
                "Initialized Phase 1 sample farms, sheds, 16 breeds, and 6 role demo accounts.");
        log.info("Phase 1 sample farms, sheds, and role-based demo accounts initialized!");
    }

    private void createSampleHouse(Farm farm, String code, String name, String type, String purpose,
                                   int capacity, double len, double width, String vent) {
        PoultryHouse house = new PoultryHouse();
        house.setFarmId(farm.getId());
        house.setFarmName(farm.getFarmName());
        house.setHouseCode(code);
        house.setHouseName(name);
        house.setHouseType(type);
        house.setBirdPurpose(purpose);
        house.setMaxBirdCapacity(capacity);
        house.setCurrentBirdOccupancy(0);
        house.setLengthFeet(len);
        house.setWidthFeet(width);
        house.setVentilationType(vent);
        house.setStatus("AVAILABLE");
        houseRepository.save(house);
    }

    private void createDemoUser(String fullName, String username, String phone, String rawPassword,
                                Role role, String lang, List<String> farmIds,
                                String bizName, String village, String district, String state) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        User u = new User();
        u.setFullName(fullName);
        u.setUsername(username);
        u.setEmail(username + "@smartindianpoultry.in");
        u.setPhone(phone);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setPreferredLanguage(lang);
        u.setAssignedFarmIds(farmIds);
        u.setBusinessName(bizName);
        u.setVillageOrCity(village);
        u.setDistrict(district);
        u.setState(state);
        u.setActive(true);
        u.setCreatedBy("SYSTEM_INIT");
        userRepository.save(u);
    }
}
