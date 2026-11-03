package com.poultry.smartfarm.init;

import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.commerce.document.ChickenShop;
import com.poultry.smartfarm.commerce.document.Customer;
import com.poultry.smartfarm.commerce.document.Expense;
import com.poultry.smartfarm.commerce.document.PaymentRecord;
import com.poultry.smartfarm.commerce.document.ProcessingRecord;
import com.poultry.smartfarm.commerce.document.RetailerStore;
import com.poultry.smartfarm.commerce.document.Sale;
import com.poultry.smartfarm.commerce.document.WholesalerDeal;
import com.poultry.smartfarm.commerce.repository.ChickenShopRepository;
import com.poultry.smartfarm.commerce.repository.CustomerRepository;
import com.poultry.smartfarm.commerce.repository.ExpenseRepository;
import com.poultry.smartfarm.commerce.repository.PaymentRecordRepository;
import com.poultry.smartfarm.commerce.repository.ProcessingRecordRepository;
import com.poultry.smartfarm.commerce.repository.RetailerStoreRepository;
import com.poultry.smartfarm.commerce.repository.SaleRepository;
import com.poultry.smartfarm.commerce.repository.WholesalerDealRepository;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.inventory.document.Supplier;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Order(4)
public class Phase4DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Phase4DataInitializer.class);

    private final CustomerRepository customerRepository;
    private final WholesalerDealRepository wholesalerRepository;
    private final RetailerStoreRepository retailerRepository;
    private final ChickenShopRepository chickenShopRepository;
    private final ProcessingRecordRepository processingRepository;
    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRecordRepository paymentRepository;
    private final FarmRepository farmRepository;
    private final BatchRepository batchRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    @Value("${app.security.seed-demo-users:true}")
    private boolean seedDemoData;

    public Phase4DataInitializer(CustomerRepository customerRepository,
                                 WholesalerDealRepository wholesalerRepository,
                                 RetailerStoreRepository retailerRepository,
                                 ChickenShopRepository chickenShopRepository,
                                 ProcessingRecordRepository processingRepository,
                                 SaleRepository saleRepository,
                                 ExpenseRepository expenseRepository,
                                 PaymentRecordRepository paymentRepository,
                                 FarmRepository farmRepository,
                                 BatchRepository batchRepository,
                                 SupplierRepository supplierRepository,
                                 UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.wholesalerRepository = wholesalerRepository;
        this.retailerRepository = retailerRepository;
        this.chickenShopRepository = chickenShopRepository;
        this.processingRepository = processingRepository;
        this.saleRepository = saleRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.farmRepository = farmRepository;
        this.batchRepository = batchRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData || customerRepository.count() > 0) {
            return;
        }

        Farm farm1 = farmRepository.findByFarmCodeIgnoreCase("FARM-MH-001").orElse(null);
        if (farm1 == null) return;

        LocalDate today = LocalDate.now();
        Batch broiler = batchRepository.findByBatchCodeIgnoreCase("BATCH-MH-2026-B01").orElse(null);
        Batch layer = batchRepository.findByBatchCodeIgnoreCase("BATCH-MH-2026-L01").orElse(null);
        User farmAdmin1 = userRepository.findByUsernameIgnoreCase("farmadmin1").orElse(null);
        User retailer1 = userRepository.findByUsernameIgnoreCase("retailer1").orElse(null);
        User shopOwner1 = userRepository.findByUsernameIgnoreCase("shopowner1").orElse(null);

        // 1. Seed Customers across all 5 Buyer Categories!
        Customer cWholesaler = saveCust("FARM", farm1, farmAdmin1, "CUST-WS-01", "Deccan Live Poultry Traders",
                "WHOLESALER", "9822100101", "Nashik", "Maharashtra", 77490.0, 60000.0, 17490.0);
        Customer cEggRet = saveCust("FARM", farm1, farmAdmin1, "CUST-EGG-01", "City Supermart Egg Distributors",
                "EGG_RETAILER", "9822100202", "Pune", "Maharashtra", 42000.0, 42000.0, 0.0);
        Customer cMeatShop = saveCust("FARM", farm1, farmAdmin1, "CUST-MS-01", "Fresh Choice Chicken Cutting Center",
                "MEAT_SHOP", "9800000006", "Nashik", "Maharashtra", 0.0, 0.0, 0.0);
        saveCust("FARM", farm1, farmAdmin1, "CUST-CR-01", "Anand Desi Chicken & Egg Corner",
                "CHICKEN_RETAILER", "9800000005", "Nashik", "Maharashtra", 0.0, 0.0, 0.0);
        Customer cDirect = saveCust("FARM", farm1, farmAdmin1, "CUST-DIR-01", "Green Valley Organic Vineyard (Manure Buyer)",
                "DIRECT_CUSTOMER", "9822100505", "Dindori", "Maharashtra", 14500.0, 14500.0, 0.0);

        // 2. Seed Wholesale Live Bird Dispatch linked to Broiler Batch's 300 sold birds
        if (broiler != null) {
            WholesalerDeal wd = new WholesalerDeal();
            wd.setFarmId(farm1.getId());
            wd.setFarmName(farm1.getFarmName());
            wd.setBatchId(broiler.getId());
            wd.setBatchCode(broiler.getBatchCode());
            wd.setDealNumber("WD-2026-001");
            wd.setDealDate(today.minusDays(2));
            wd.setCustomerId(cWholesaler.getId());
            wd.setCustomerName(cWholesaler.getCustomerName());
            wd.setCustomerPhone(cWholesaler.getPhone());
            wd.setBuyerCategory("WHOLESALER");
            wd.setLiveBirdQuantity(300);
            wd.setAverageWeightKg(2.05);
            wd.setTotalLiveWeightKg(615.0);
            wd.setPurchasePricePerKg(92.0);
            wd.setTransportCostInr(1800.0);
            wd.setTotalPurchaseAmountInr(58380.0);
            wd.setSellingPricePerKg(126.0);
            wd.setTotalSaleAmountInr(77490.0);
            wd.setCashReceivedInr(60000.0);
            wd.setOutstandingPaymentInr(17490.0);
            wd.setPaymentStatus("PARTIAL");
            wd.setVehicleNumber("MH-15-GV-4421");
            wd.setRemarks("First partial thinning lot of 300 broilers");
            wd.setRecordedBy("farmadmin1");
            wholesalerRepository.save(wd);

            Sale wsSale = new Sale();
            wsSale.setInvoiceNumber("WS-WD-2026-001");
            wsSale.setSaleDate(today.minusDays(2));
            wsSale.setBusinessContext("FARM");
            wsSale.setFarmId(farm1.getId());
            wsSale.setFarmName(farm1.getFarmName());
            wsSale.setBatchId(broiler.getId());
            wsSale.setBatchCode(broiler.getBatchCode());
            wsSale.setSaleCategory("LIVE_CHICKEN_SALE");
            wsSale.setProductDescription("Wholesale Live Broilers (300 birds, 615 kg)");
            wsSale.setBuyerCategory("WHOLESALER");
            wsSale.setCustomerId(cWholesaler.getId());
            wsSale.setCustomerName(cWholesaler.getCustomerName());
            wsSale.setCustomerPhone(cWholesaler.getPhone());
            wsSale.setQuantity(300.0);
            wsSale.setUnit("BIRDS");
            wsSale.setLiveWeightKg(615.0);
            wsSale.setUnitSellingPriceInr(126.0);
            wsSale.setTotalSaleAmountInr(77490.0);
            wsSale.setCashReceivedInr(60000.0);
            wsSale.setOutstandingBalanceInr(17490.0);
            wsSale.setPaymentStatus("PARTIAL");
            wsSale.setPaymentMode("NEFT_RTGS");
            wsSale.setRecordedBy("farmadmin1");
            saleRepository.save(wsSale);
        }

        // 3. Seed Poultry Manure / By-Product Sale & Farm Operating Expenses + Payment Receipt
        Sale manureSale = new Sale();
        manureSale.setInvoiceNumber("INV-MANURE-001");
        manureSale.setSaleDate(today.minusDays(3));
        manureSale.setBusinessContext("FARM");
        manureSale.setFarmId(farm1.getId());
        manureSale.setFarmName(farm1.getFarmName());
        if (layer != null) {
            manureSale.setBatchId(layer.getId());
            manureSale.setBatchCode(layer.getBatchCode());
        }
        manureSale.setSaleCategory("MANURE_BYPRODUCT_SALE");
        manureSale.setProductDescription("Composted Dry Poultry Litter Manure (5 Tractor Loads)");
        manureSale.setBuyerCategory("DIRECT_CUSTOMER");
        manureSale.setCustomerId(cDirect.getId());
        manureSale.setCustomerName(cDirect.getCustomerName());
        manureSale.setQuantity(5.0);
        manureSale.setUnit("TONS");
        manureSale.setUnitSellingPriceInr(2900.0);
        manureSale.setTotalSaleAmountInr(14500.0);
        manureSale.setCashReceivedInr(14500.0);
        manureSale.setOutstandingBalanceInr(0.0);
        manureSale.setPaymentStatus("PAID");
        manureSale.setPaymentMode("UPI");
        manureSale.setRecordedBy("farmadmin1");
        saleRepository.save(manureSale);

        Expense trExp = new Expense();
        trExp.setExpenseDate(today.minusDays(2));
        trExp.setBusinessContext("FARM");
        trExp.setFarmId(farm1.getId());
        trExp.setFarmName(farm1.getFarmName());
        if (broiler != null) {
            trExp.setBatchId(broiler.getId());
            trExp.setBatchCode(broiler.getBatchCode());
        }
        trExp.setExpenseCategory("TRANSPORTATION");
        trExp.setAmountInr(1800.0);
        trExp.setPaidAmountInr(1800.0);
        trExp.setPaymentMode("CASH");
        trExp.setPayeeName("MH-15-GV-4421 Cage Tempo");
        trExp.setDescription("Wholesale bird delivery diesel & cage loading");
        trExp.setRecordedBy("farmadmin1");
        expenseRepository.save(trExp);

        Supplier feedSup = supplierRepository.findBySupplierCodeIgnoreCase("SUP-MH-FEED01").orElse(null);
        if (feedSup != null) {
            PaymentRecord supPay = new PaymentRecord();
            supPay.setPaymentDate(today.minusDays(5));
            supPay.setBusinessContext("FARM");
            supPay.setFarmId(farm1.getId());
            supPay.setFarmName(farm1.getFarmName());
            supPay.setPaymentType("SUPPLIER_PAYMENT");
            supPay.setSupplierId(feedSup.getId());
            supPay.setSupplierName(feedSup.getSupplierName());
            supPay.setAmountInr(80000.0);
            supPay.setPaymentMethod("NEFT_RTGS");
            supPay.setTransactionRef("UTR20269918273");
            supPay.setRemarks("Part settlement against feed invoice");
            supPay.setRecordedBy("farmadmin1");
            paymentRepository.save(supPay);
        }

        // 4. Seed Retailer Store (`retailer1`)
        if (retailer1 != null) {
            RetailerStore rs = new RetailerStore();
            rs.setOwnerUserId(retailer1.getId());
            rs.setOwnerUsername(retailer1.getUsername());
            rs.setStoreCode("RET-NASHIK-01");
            rs.setStoreName(retailer1.getBusinessName());
            rs.setPhone(retailer1.getPhone());
            rs.setCity("Nashik");
            rs.setState("Maharashtra");
            rs.setLiveBirdStockCount(42);
            rs.setLiveBirdAvgPurchaseCost(230.0);
            rs.setEggStockPieces(1800);
            rs.setEggAvgPurchaseCost(5.60);
            rs.setTotalStockPurchaseCostInr(41800.0);
            rs.setTotalChickenSalesRevenueInr(22400.0);
            rs.setTotalEggSalesRevenueInr(29700.0);
            rs.setTotalOperatingExpensesInr(2600.0);
            rs = retailerRepository.save(rs);

            Sale retEggSale = new Sale();
            retEggSale.setInvoiceNumber("RET-INV-101");
            retEggSale.setSaleDate(today);
            retEggSale.setBusinessContext("RETAIL");
            retEggSale.setOwnerUserId(retailer1.getId());
            retEggSale.setRetailStoreId(rs.getId());
            retEggSale.setSaleCategory("EGG_SALE");
            retEggSale.setProductDescription("Retail Fresh White & Desi Brown Eggs");
            retEggSale.setCustomerName("Daily Counter Customers");
            retEggSale.setQuantity(900.0);
            retEggSale.setUnit("PIECES");
            retEggSale.setUnitSellingPriceInr(7.0);
            retEggSale.setTotalSaleAmountInr(6300.0);
            retEggSale.setCashReceivedInr(6300.0);
            retEggSale.setPaymentStatus("PAID");
            retEggSale.setRecordedBy("retailer1");
            saleRepository.save(retEggSale);
        }

        // 5. Seed Chicken Cutting Shop (`shopowner1`) with Actual Dressed Weight Record!
        if (shopOwner1 != null) {
            ChickenShop shop = new ChickenShop();
            shop.setOwnerUserId(shopOwner1.getId());
            shop.setOwnerUsername(shopOwner1.getUsername());
            shop.setShopCode("SHOP-NSK-01");
            shop.setShopName(shopOwner1.getBusinessName());
            shop.setPhone(shopOwner1.getPhone());
            shop.setCity("Nashik");
            shop.setState("Maharashtra");
            shop.setLiveBirdsStockCount(25);
            shop.setLiveBirdsStockWeightKg(52.5);
            shop.setDefaultMeatSellingPricePerKg(230.0);
            shop.setDressedMeatStockKg(32.5);
            shop.setTotalDressedMeatProducedKg(302.5);
            shop.setTotalDressedMeatSoldKg(270.0);
            // Exact math: 440 kg * ₹126/kg = ₹55,440 purchase cost
            shop.setTotalLivePurchaseCostInr(55440.0);
            shop.setTotalProcessingExpensesInr(3200.0);
            shop.setTotalOtherAllocatedExpensesInr(1500.0);
            // 270 kg * ₹230/kg = ₹62,100 meat sales + ₹2,450 liver/gizzard by-products = ₹64,550 revenue
            shop.setTotalMeatSalesRevenueInr(62100.0);
            shop.setTotalByProductSalesRevenueInr(2450.0);
            shop = chickenShopRepository.save(shop);

            ProcessingRecord pr = new ProcessingRecord();
            pr.setShopId(shop.getId());
            pr.setShopName(shop.getShopName());
            pr.setOwnerUserId(shopOwner1.getId());
            pr.setRecordDate(today);
            pr.setSupplierOrFarmName("Sahyadri Krushi Poultry Farm");
            pr.setBirdType("Commercial Broiler (Ross 308)");
            pr.setLiveBirdsPurchased(225);
            pr.setBirdsProcessedCount(200);
            pr.setLiveWeightKg(440.0);
            pr.setLivePurchaseRatePerKg(126.0);
            pr.setLivePurchaseCostInr(55440.0);
            pr.setProcessingExpensesInr(3200.0);
            pr.setOtherAllocatedExpensesInr(1500.0);
            pr.setActualDressedMeatWeightKg(302.5); // Actual weighed meat (68.75% observed yield — never assumed fixed!)
            pr.setObservedDressingPct(68.75);
            pr.setMeatSoldImmediatelyKg(270.0);
            pr.setRemainingMeatAddedToStockKg(32.5);
            pr.setSellingPricePerKg(230.0);
            pr.setMeatSalesRevenueInr(62100.0);
            pr.setWasteAndFeathersKg(137.5);
            pr.setByProductSalesInr(2450.0);
            pr.setTotalRevenueInr(64550.0);
            pr.setTotalCostInr(60140.0);
            pr.setNetProfitLossInr(4410.0);
            pr.setRemarks("25 live birds held in shop cage; 32.5 kg dressed meat in chiller counter");
            pr.setRecordedBy("shopowner1");
            processingRepository.save(pr);

            Sale shopMeatSale = new Sale();
            shopMeatSale.setInvoiceNumber("CS-M-2026-01");
            shopMeatSale.setSaleDate(today);
            shopMeatSale.setBusinessContext("CHICKEN_SHOP");
            shopMeatSale.setOwnerUserId(shopOwner1.getId());
            shopMeatSale.setChickenShopId(shop.getId());
            shopMeatSale.setSaleCategory("DRESSED_MEAT_SALE");
            shopMeatSale.setProductDescription("Hygienic Curry-Cut & Boneless Dressed Chicken");
            shopMeatSale.setCustomerName("Retail Counter & Restaurant Buyers");
            shopMeatSale.setQuantity(270.0);
            shopMeatSale.setUnit("KG");
            shopMeatSale.setUnitSellingPriceInr(230.0);
            shopMeatSale.setTotalSaleAmountInr(62100.0);
            shopMeatSale.setCashReceivedInr(62100.0);
            shopMeatSale.setPaymentStatus("PAID");
            shopMeatSale.setRecordedBy("shopowner1");
            saleRepository.save(shopMeatSale);
        }

        log.info("Seeded Phase 4 customers, wholesale deals, retailer store, chicken cutting shop (actual dressed meat), sales, expenses & payments!");
    }

    private Customer saveCust(String ctx, Farm farm, User owner, String code, String name, String cat,
                              String phone, String city, String state, double billed, double rec, double out) {
        Customer c = new Customer();
        c.setBusinessContext(ctx);
        if (farm != null) {
            c.setFarmId(farm.getId());
            c.setFarmName(farm.getFarmName());
        }
        if (owner != null) c.setOwnerUserId(owner.getId());
        c.setCustomerCode(code);
        c.setCustomerName(name);
        c.setBuyerCategory(cat);
        c.setPhone(phone);
        c.setCity(city);
        c.setState(state);
        c.setTotalBilledAmount(billed);
        c.setTotalReceivedAmount(rec);
        c.setOutstandingBalance(out);
        return customerRepository.save(c);
    }
}
