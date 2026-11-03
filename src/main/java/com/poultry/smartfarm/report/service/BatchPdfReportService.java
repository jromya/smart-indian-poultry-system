package com.poultry.smartfarm.report.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.document.EggRecord;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.repository.EggRecordRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.batch.service.PoultryAnalyticsCalculationService;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.commerce.service.LedgerAndFinancialService;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.report.document.BatchReport;
import com.poultry.smartfarm.report.repository.BatchReportRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BatchPdfReportService {

    private final BatchReportRepository reportRepository;
    private final BatchService batchService;
    private final BreedRepository breedRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final EggRecordRepository eggRecordRepository;
    private final PoultryAnalyticsCalculationService flockCalcService;
    private final LedgerAndFinancialService financialService;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public BatchPdfReportService(BatchReportRepository reportRepository,
                                 BatchService batchService,
                                 BreedRepository breedRepository,
                                 DailyRecordRepository dailyRecordRepository,
                                 EggRecordRepository eggRecordRepository,
                                 PoultryAnalyticsCalculationService flockCalcService,
                                 LedgerAndFinancialService financialService,
                                 FarmAccessGuard farmAccessGuard,
                                 AuditLogService auditLogService) {
        this.reportRepository = reportRepository;
        this.batchService = batchService;
        this.breedRepository = breedRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.eggRecordRepository = eggRecordRepository;
        this.flockCalcService = flockCalcService;
        this.financialService = financialService;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    @PostConstruct
    public void registerBatchCompletionHook() {
        this.batchService.setOnBatchCompletedHook(completedBatch -> {
            String lang = "en";
            try {
                User actor = farmAccessGuard.getCurrentUser();
                if (actor != null && actor.getPreferredLanguage() != null) {
                    lang = actor.getPreferredLanguage();
                }
            } catch (Exception ignored) {
            }
            generateAndSaveReport(completedBatch.getId(), lang, true);
        });
    }

    public List<BatchReport> listAccessibleReports(String farmId, String batchId) {
        User user = farmAccessGuard.getCurrentUser();
        List<BatchReport> reports;
        if (batchId != null && !batchId.isBlank()) {
            Batch b = batchService.getBatchDocumentWithAccess(batchId);
            reports = reportRepository.findByBatchIdOrderByGeneratedAtDesc(b.getId());
        } else if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            reports = reportRepository.findByFarmIdOrderByGeneratedAtDesc(farmId);
        } else if (user.getRole() == Role.SUPER_ADMIN) {
            reports = reportRepository.findAllByOrderByGeneratedAtDesc();
        } else if (user.getRole().isFarmRole() && user.getAssignedFarmIds() != null && !user.getAssignedFarmIds().isEmpty()) {
            reports = reportRepository.findByFarmIdInOrderByGeneratedAtDesc(user.getAssignedFarmIds());
        } else {
            reports = Collections.emptyList();
        }
        return reports;
    }

    public BatchReport getReportWithAccess(String reportId) {
        BatchReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch report not found with ID: " + reportId));
        farmAccessGuard.requireFarmAccess(report.getFarmId());
        return report;
    }

    public BatchReport generateAndSaveReport(String batchId, String langCode, boolean autoGeneratedOnCompletion) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(batchId);
        BatchResponse bv = BatchResponse.fromDocument(batch);

        LocalDate endDt = batch.getActualCompletionDate() != null ? batch.getActualCompletionDate() : LocalDate.now();
        int cycleDays = batch.getChickArrivalDate() != null
                ? Math.max(1, (int) ChronoUnit.DAYS.between(batch.getChickArrivalDate(), endDt) + 1)
                : bv.getCurrentAgeDays();

        Map<String, Object> fin = financialService.calculateFinancialReport(batch.getFarmId(), batch.getId(), null, null);

        double totalFeedKg;
        double fcr = 0.0;
        double targetFcr = 1.65;
        if (!"LAYER".equalsIgnoreCase(batch.getBatchCategory())) {
            Map<String, Object> broilerCalc = flockCalcService.calculateBroilerAnalytics(batch.getId(), null, null);
            totalFeedKg = toDouble(broilerCalc.get("totalFeedKg"), 0.0);
            fcr = toDouble(broilerCalc.get("fcr"), 0.0);
            targetFcr = toDouble(broilerCalc.get("breedTargetFcr"), 1.65);
        } else {
            List<DailyRecord> daily = dailyRecordRepository.findByBatchIdOrderByRecordDateAsc(batch.getId());
            totalFeedKg = round2(daily.stream().mapToDouble(DailyRecord::getFeedConsumptionKg).sum());
            Breed breed = batch.getBreedId() != null ? breedRepository.findById(batch.getBreedId()).orElse(null) : null;
            if (breed != null && breed.getPerformanceTargets() != null && breed.getPerformanceTargets().getTargetFcr() != null) {
                targetFcr = breed.getPerformanceTargets().getTargetFcr();
            }
        }

        List<EggRecord> eggs = eggRecordRepository.findByBatchIdOrderByRecordDateAsc(batch.getId());
        double avgHenDay = eggs.isEmpty() ? 0.0
                : round2(eggs.stream().mapToDouble(EggRecord::getHenDayProductionPct).average().orElse(0.0));

        BatchReport rpt = new BatchReport();
        String suffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        rpt.setReportNumber("RPT-" + batch.getBatchCode() + "-" + suffix);
        rpt.setFarmId(batch.getFarmId());
        rpt.setFarmName(batch.getFarmName());
        rpt.setBatchId(batch.getId());
        rpt.setBatchCode(batch.getBatchCode());
        rpt.setBatchName(batch.getBatchName());
        rpt.setPoultryHouseName(batch.getPoultryHouseName());
        rpt.setBreedName(batch.getBreedName());
        rpt.setBatchCategory(batch.getBatchCategory());
        rpt.setBatchStatus(batch.getStatus());
        rpt.setChickArrivalDate(batch.getChickArrivalDate());
        rpt.setReportEndDate(endDt);
        rpt.setTotalCycleDays(cycleDays);

        rpt.setStartingBirds(batch.getStartingBirdQuantity());
        rpt.setTotalAdditions(batch.getTotalAdditions());
        rpt.setTotalMortality(batch.getTotalMortality());
        rpt.setTotalCulls(batch.getTotalCulls());
        rpt.setMortalityPercentage(bv.getMortalityPercentage());
        rpt.setTotalBirdsSold(batch.getTotalBirdsSold());
        rpt.setRemainingLiveBirds(bv.getCurrentLiveBirds());

        rpt.setFinalAverageWeightGrams(round2(batch.getLatestAverageWeightGrams()));
        rpt.setTotalFeedConsumedKg(round2(totalFeedKg));
        rpt.setTotalSoldLiveWeightKg(round2(batch.getTotalSoldLiveWeightKg()));
        rpt.setCalculatedFcr(fcr);
        rpt.setBreedTargetFcr(targetFcr);
        rpt.setTotalGoodEggsProduced(batch.getTotalGoodEggsProduced());
        rpt.setTotalEggsSold(batch.getTotalEggsSold());
        rpt.setAverageHenDayPct(avgHenDay);

        double chickenRev = toDouble(fin.get("chickenSalesInr"), 0.0);
        double eggRev = toDouble(fin.get("eggSalesInr"), 0.0);
        double manureRev = toDouble(fin.get("manureAndByProductSalesInr"), 0.0) + toDouble(fin.get("meatSalesInr"), 0.0);
        double totalRev = toDouble(fin.get("totalRevenueInr"), round2(chickenRev + eggRev + manureRev));

        double chickCost = toDouble(fin.get("chickPurchaseExpensesInr"), 0.0);
        double feedCost = toDouble(fin.get("feedExpensesInr"), 0.0);
        double medCost = toDouble(fin.get("medicineExpensesInr"), 0.0);
        double otherCost = round2(
                toDouble(fin.get("labourExpensesInr"), 0.0)
                        + toDouble(fin.get("electricityExpensesInr"), 0.0)
                        + toDouble(fin.get("transportationExpensesInr"), 0.0)
                        + toDouble(fin.get("equipmentExpensesInr"), 0.0)
                        + toDouble(fin.get("otherExpensesInr"), 0.0));
        double totalExp = toDouble(fin.get("totalExpensesInr"), round2(chickCost + feedCost + medCost + otherCost));
        double netPl = round2(totalRev - totalExp);

        int denomBirds = Math.max(1, batch.getStartingBirdQuantity());
        rpt.setChickenSalesInr(chickenRev);
        rpt.setEggSalesInr(eggRev);
        rpt.setManureAndByProductSalesInr(round2(manureRev));
        rpt.setTotalRevenueInr(totalRev);

        rpt.setChickPurchaseCostInr(chickCost);
        rpt.setFeedConsumedCostInr(feedCost);
        rpt.setMedicineAndVaccineCostInr(medCost);
        rpt.setLabourElectricityOtherCostInr(otherCost);
        rpt.setTotalExpensesInr(totalExp);

        rpt.setNetProfitLossInr(netPl);
        rpt.setCostPerBirdInr(round2(totalExp / denomBirds));
        rpt.setProfitPerBirdInr(round2(netPl / denomBirds));
        rpt.setProfitable(netPl >= 0);

        String resolvedLang = (langCode != null && !langCode.isBlank())
                ? langCode.toLowerCase()
                : (actor.getPreferredLanguage() != null ? actor.getPreferredLanguage() : "en");
        rpt.setPreferredLanguage(resolvedLang);
        rpt.setAutoGeneratedOnCompletion(autoGeneratedOnCompletion);
        rpt.setGeneratedBy(actor.getUsername());
        rpt.setGeneratedAt(LocalDateTime.now());

        BatchReport saved = reportRepository.save(rpt);
        auditLogService.record(actor, batch.getFarmId(),
                autoGeneratedOnCompletion ? "BATCH_REPORT_AUTO_GENERATED" : "BATCH_REPORT_GENERATED",
                "BATCH_REPORT", saved.getId(),
                "Generated Batch Report " + saved.getReportNumber() + " for " + batch.getBatchCode()
                        + " (Net P&L: Rs." + netPl + ")");
        return saved;
    }

    public byte[] renderPdfBytes(BatchReport rpt, String overrideLang) {
        String lang = (overrideLang != null && !overrideLang.isBlank())
                ? overrideLang.toLowerCase()
                : rpt.getPreferredLanguage();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 34, 34, 32, 32);
            PdfWriter.getInstance(doc, out);
            doc.open();

            ResolvedFont resolved = resolveUnicodeOrFallbackFont(lang);
            BaseFont bf = resolved.baseFont;
            String curr = resolved.unicodeCapable ? "₹" : "Rs. ";

            Font titleFont = new Font(bf, 16, Font.BOLD, new Color(255, 255, 255));
            Font subTitleFont = new Font(bf, 10, Font.NORMAL, new Color(232, 245, 233));
            Font secHeadingFont = new Font(bf, 12, Font.BOLD, new Color(27, 94, 32));
            Font tblHeaderFont = new Font(bf, 10, Font.BOLD, new Color(27, 94, 32));
            Font normalFont = new Font(bf, 9.5f, Font.NORMAL, new Color(33, 37, 41));
            Font boldFont = new Font(bf, 9.5f, Font.BOLD, new Color(33, 37, 41));
            Font profitFont = new Font(bf, 11, Font.BOLD, rpt.isProfitable() ? new Color(27, 94, 32) : new Color(198, 40, 40));

            // 1. Header Banner Table
            PdfPTable banner = new PdfPTable(1);
            banner.setWidthPercentage(100);
            String localizedTitle = resolved.unicodeCapable ? getLocalizedBannerTitle(lang) : "SMART INDIAN POULTRY FARM MANAGEMENT SYSTEM";
            PdfPCell bCell = new PdfPCell();
            bCell.setBackgroundColor(new Color(46, 125, 50));
            bCell.setPadding(14);
            bCell.setBorder(Rectangle.NO_BORDER);
            bCell.addElement(new Paragraph(localizedTitle, titleFont));
            bCell.addElement(new Paragraph("Batch Performance & Accrual Financial P&L Report  |  Report #" + rpt.getReportNumber(), subTitleFont));
            banner.addCell(bCell);
            doc.add(banner);

            doc.add(new Paragraph(" ", normalFont));

            // 2. Batch & Farm Metadata Grid
            doc.add(new Paragraph("1. Farm & Batch Summary" + (resolved.unicodeCapable ? getLocalizedSuffix(lang, "summary") : ""), secHeadingFont));
            doc.add(space(4));
            PdfPTable meta = new PdfPTable(new float[]{25f, 25f, 25f, 25f});
            meta.setWidthPercentage(100);
            addKvCell(meta, "Farm Name", safe(rpt.getFarmName()), tblHeaderFont, normalFont);
            addKvCell(meta, "Poultry Shed", safe(rpt.getPoultryHouseName()), tblHeaderFont, normalFont);
            addKvCell(meta, "Batch Code", safe(rpt.getBatchCode()) + " (" + safe(rpt.getBatchStatus()) + ")", tblHeaderFont, boldFont);
            addKvCell(meta, "Breed / Category", safe(rpt.getBreedName()) + " [" + safe(rpt.getBatchCategory()) + "]", tblHeaderFont, normalFont);
            addKvCell(meta, "Arrival Date", String.valueOf(rpt.getChickArrivalDate()), tblHeaderFont, normalFont);
            addKvCell(meta, "Report / End Date", String.valueOf(rpt.getReportEndDate()), tblHeaderFont, normalFont);
            addKvCell(meta, "Cycle Duration", rpt.getTotalCycleDays() + " Days", tblHeaderFont, normalFont);
            addKvCell(meta, "Generated By", safe(rpt.getGeneratedBy()) + (rpt.isAutoGeneratedOnCompletion() ? " (Auto)" : ""), tblHeaderFont, normalFont);
            doc.add(meta);

            doc.add(space(10));

            // 3. Flock Reconciliation & Growth/Egg Metrics
            doc.add(new Paragraph("2. Flock Balance, Growth, Feed & Egg Performance" + (resolved.unicodeCapable ? getLocalizedSuffix(lang, "flock") : ""), secHeadingFont));
            doc.add(space(4));
            PdfPTable flockTbl = new PdfPTable(new float[]{34f, 16f, 34f, 16f});
            flockTbl.setWidthPercentage(100);
            addRowPair(flockTbl, "Starting Chicks Placed", String.valueOf(rpt.getStartingBirds()),
                    "Final Avg Bird Weight", fmt(rpt.getFinalAverageWeightGrams()) + " g", boldFont, normalFont);
            addRowPair(flockTbl, "Additions", String.valueOf(rpt.getTotalAdditions()),
                    "Total Feed Consumed", fmt(rpt.getTotalFeedConsumedKg()) + " kg", boldFont, normalFont);
            addRowPair(flockTbl, "Total Mortality / Culls", rpt.getTotalMortality() + " / " + rpt.getTotalCulls() + " (" + fmt(rpt.getMortalityPercentage()) + "%)",
                    "Calculated FCR (Target)", fmt(rpt.getCalculatedFcr()) + " (" + fmt(rpt.getBreedTargetFcr()) + ")", boldFont, normalFont);
            addRowPair(flockTbl, "Total Birds Sold", rpt.getTotalBirdsSold() + " (" + fmt(rpt.getTotalSoldLiveWeightKg()) + " kg)",
                    "Good Eggs Produced / Sold", rpt.getTotalGoodEggsProduced() + " / " + rpt.getTotalEggsSold(), boldFont, normalFont);
            addRowPair(flockTbl, "Remaining Live Birds", String.valueOf(rpt.getRemainingLiveBirds()),
                    "Average Hen-Day Egg %", fmt(rpt.getAverageHenDayPct()) + "%", boldFont, normalFont);
            doc.add(flockTbl);

            doc.add(space(10));

            // 4. Non-Double-Counted Financial Profit & Loss Statement
            doc.add(new Paragraph("3. Batch Financial Profit & Loss Statement (Accrual — Non-Double-Counted)" + (resolved.unicodeCapable ? getLocalizedSuffix(lang, "finance") : ""), secHeadingFont));
            doc.add(space(4));
            PdfPTable finTbl = new PdfPTable(new float[]{35f, 15f, 35f, 15f});
            finTbl.setWidthPercentage(100);

            addHeaderCell(finTbl, "REVENUE STREAM", tblHeaderFont);
            addHeaderCell(finTbl, "AMOUNT", tblHeaderFont);
            addHeaderCell(finTbl, "CONSUMED EXPENSE HEAD", tblHeaderFont);
            addHeaderCell(finTbl, "AMOUNT", tblHeaderFont);

            addRowPair(finTbl, "Live Chicken Sales", curr + fmt(rpt.getChickenSalesInr()),
                    "Chick Purchase Cost", curr + fmt(rpt.getChickPurchaseCostInr()), normalFont, normalFont);
            addRowPair(finTbl, "Egg Sales Revenue", curr + fmt(rpt.getEggSalesInr()),
                    "Feed Consumed Cost", curr + fmt(rpt.getFeedConsumedCostInr()), normalFont, normalFont);
            addRowPair(finTbl, "Manure / Meat / By-Products", curr + fmt(rpt.getManureAndByProductSalesInr()),
                    "Medicine & Vaccination Cost", curr + fmt(rpt.getMedicineAndVaccineCostInr()), normalFont, normalFont);
            addRowPair(finTbl, "—", "—",
                    "Labour, Electricity, Transport & Other", curr + fmt(rpt.getLabourElectricityOtherCostInr()), normalFont, normalFont);
            addRowPair(finTbl, "TOTAL REVENUE", curr + fmt(rpt.getTotalRevenueInr()),
                    "TOTAL CONSUMED EXPENSES", curr + fmt(rpt.getTotalExpensesInr()), boldFont, boldFont);
            doc.add(finTbl);

            doc.add(space(10));

            // 5. Net Profit / Loss Summary Box
            PdfPTable summaryBox = new PdfPTable(new float[]{34f, 33f, 33f});
            summaryBox.setWidthPercentage(100);
            Color boxBg = rpt.isProfitable() ? new Color(232, 245, 233) : new Color(255, 235, 238);
            String verdict = rpt.isProfitable() ? "NET PROFIT: " : "NET LOSS: ";
            addSummaryBoxCell(summaryBox, verdict + curr + fmt(rpt.getNetProfitLossInr()), profitFont, boxBg);
            addSummaryBoxCell(summaryBox, "Cost / Bird: " + curr + fmt(rpt.getCostPerBirdInr()), boldFont, boxBg);
            addSummaryBoxCell(summaryBox, "Net Margin / Bird: " + curr + fmt(rpt.getProfitPerBirdInr()), boldFont, boxBg);
            doc.add(summaryBox);

            doc.add(space(14));
            String ts = rpt.getGeneratedAt() != null
                    ? rpt.getGeneratedAt().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))
                    : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
            Paragraph footer = new Paragraph(
                    "Generated on " + ts + " by Smart Indian Poultry Farm Management System • Warehouse stock purchases are excluded from consumed expenses to prevent double-counting.",
                    new Font(bf, 8f, Font.NORMAL, new Color(110, 110, 110)));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate Batch PDF Report: " + e.getMessage(), e);
        }
    }

    private record ResolvedFont(BaseFont baseFont, boolean unicodeCapable) {}

    private ResolvedFont resolveUnicodeOrFallbackFont(String lang) {
        List<String> candidates = new ArrayList<>();
        // Language-specific Linux Noto fonts installed via fonts-noto-core in Dockerfile
        switch (lang != null ? lang : "en") {
            case "mr", "hi" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansDevanagari-Regular.ttf");
            case "gu" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansGujarati-Regular.ttf");
            case "pa" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansGurmukhi-Regular.ttf");
            case "bn" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansBengali-Regular.ttf");
            case "te" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansTelugu-Regular.ttf");
            case "kn" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansKannada-Regular.ttf");
            case "ta" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansTamil-Regular.ttf");
            case "ml" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansMalayalam-Regular.ttf");
            case "or" -> candidates.add("/usr/share/fonts/truetype/noto/NotoSansOriya-Regular.ttf");
            default -> { }
        }
        // Windows pan-Indic font (Nirmala UI covers all 10 Indian scripts + Latin + Rupee symbol!)
        candidates.add("C:/Windows/Fonts/Nirmala.ttf");
        candidates.add("C:/Windows/Fonts/mangal.ttf");
        candidates.add("/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf");
        candidates.add("C:/Windows/Fonts/arial.ttf");

        for (String path : candidates) {
            try {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    BaseFont bf = BaseFont.createFont(f.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    boolean panOrScriptMatch = !path.toLowerCase().endsWith("arial.ttf");
                    return new ResolvedFont(bf, panOrScriptMatch);
                }
            } catch (Exception ignored) {
            }
        }
        try {
            return new ResolvedFont(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED), false);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load PDF fallback font", ex);
        }
    }

    private String getLocalizedBannerTitle(String lang) {
        return switch (lang != null ? lang : "en") {
            case "mr" -> "स्मार्ट भारतीय पोल्ट्री व्यवस्थापन — बॅच अहवाल (Smart Indian Poultry)";
            case "hi" -> "स्मार्ट इंडियन पोल्ट्री प्रबंधन — बैच रिपोर्ट (Smart Indian Poultry)";
            case "gu" -> "સ્માર્ટ ઇન્ડિયન પોલ્ટ્રી — બેચ રિપોર્ટ (Smart Indian Poultry)";
            case "pa" -> "ਸਮਾਰਟ ਇੰਡੀਅਨ ਪੋਲਟਰੀ — ਬੈਚ ਰਿਪੋਰਟ (Smart Indian Poultry)";
            case "bn" -> "স্মার্ট ইন্ডিয়ান পোল্ট্রি — ব্যাচ রিপোর্ট (Smart Indian Poultry)";
            case "te" -> "స్మార్ట్ ఇండియన్ పౌల్ట్రీ — బ్యాచ్ నివేదిక (Smart Indian Poultry)";
            case "kn" -> "ಸ್ಮಾರ್ಟ್ ಇಂಡಿಯನ್ ಪೌಲ್ಟ್ರಿ — ಬ್ಯಾಚ್ ವರದಿ (Smart Indian Poultry)";
            case "ta" -> "ஸ்மார்ட் இந்தியன் பண்ணை — தொகுதி அறிக்கை (Smart Indian Poultry)";
            case "ml" -> "സ്മാർട്ട് ഇന്ത്യൻ പൗൾട്രി — ബാച്ച് റിപ്പോർട്ട് (Smart Indian Poultry)";
            case "or" -> "ସ୍ମାର୍ଟ ଇଣ୍ଡିଆନ୍ ପୋଲ୍ଟ୍ରି — ବ୍ୟାଚ୍ ରିପୋର୍ଟ (Smart Indian Poultry)";
            default -> "SMART INDIAN POULTRY FARM MANAGEMENT SYSTEM";
        };
    }

    private String getLocalizedSuffix(String lang, String section) {
        if ("mr".equals(lang)) {
            return switch (section) {
                case "summary" -> " (फार्म व बॅच माहिती)";
                case "flock" -> " (पक्षी संख्या, खाद्य व अंडी तपशील)";
                case "finance" -> " (उत्पन्न, खर्च व निव्वळ नफा-तोटा)";
                default -> "";
            };
        }
        if ("hi".equals(lang)) {
            return switch (section) {
                case "summary" -> " (फार्म एवं बैच विवरण)";
                case "flock" -> " (पक्षी संख्या, दाना एवं अंडा प्रदर्शन)";
                case "finance" -> " (आय, खर्च एवं शुद्ध लाभ-हानि)";
                default -> "";
            };
        }
        return "";
    }

    private Paragraph space(float pts) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(pts);
        return p;
    }

    private void addKvCell(PdfPTable tbl, String label, String val, Font lFont, Font vFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.setBorderColor(new Color(200, 230, 201));
        cell.addElement(new Paragraph(label, lFont));
        cell.addElement(new Paragraph(val, vFont));
        tbl.addCell(cell);
    }

    private void addHeaderCell(PdfPTable tbl, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(new Color(232, 245, 233));
        c.setPadding(6);
        c.setBorderColor(new Color(165, 214, 167));
        tbl.addCell(c);
    }

    private void addRowPair(PdfPTable tbl, String k1, String v1, String k2, String v2, Font kf, Font vf) {
        tbl.addCell(styledCell(k1, kf, Element.ALIGN_LEFT));
        tbl.addCell(styledCell(v1, vf, Element.ALIGN_RIGHT));
        tbl.addCell(styledCell(k2, kf, Element.ALIGN_LEFT));
        tbl.addCell(styledCell(v2, vf, Element.ALIGN_RIGHT));
    }

    private PdfPCell styledCell(String txt, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setHorizontalAlignment(align);
        c.setPadding(6);
        c.setBorderColor(new Color(220, 220, 220));
        return c;
    }

    private void addSummaryBoxCell(PdfPTable tbl, String txt, Font f, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(10);
        c.setBorderColor(new Color(129, 199, 132));
        tbl.addCell(c);
    }

    private String safe(String s) {
        return s != null && !s.isBlank() ? s : "-";
    }

    private String fmt(Double d) {
        if (d == null) return "0.00";
        return String.format("%,.2f", d);
    }

    private double toDouble(Object v, double fallback) {
        return v instanceof Number n ? round2(n.doubleValue()) : fallback;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
