package com.poultry.smartfarm.commerce.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "payments")
public class PaymentRecord {

    @Id
    private String id;

    @Indexed
    private LocalDate paymentDate;

    // FARM, RETAIL, CHICKEN_SHOP
    @Indexed
    private String businessContext = "FARM";

    @Indexed
    private String farmId;
    private String farmName;

    @Indexed
    private String ownerUserId;

    // CUSTOMER_RECEIPT or SUPPLIER_PAYMENT
    @Indexed
    private String paymentType;

    @Indexed
    private String customerId;
    private String customerName;

    @Indexed
    private String supplierId;
    private String supplierName;

    private String relatedInvoiceId;
    private String relatedInvoiceNumber;

    private Double amountInr;
    // CASH, UPI, NEFT_RTGS, CHEQUE
    private String paymentMethod = "UPI";
    private String transactionRef;
    private String remarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public PaymentRecord() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getBusinessContext() { return businessContext; }
    public void setBusinessContext(String businessContext) { this.businessContext = businessContext; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getRelatedInvoiceId() { return relatedInvoiceId; }
    public void setRelatedInvoiceId(String relatedInvoiceId) { this.relatedInvoiceId = relatedInvoiceId; }
    public String getRelatedInvoiceNumber() { return relatedInvoiceNumber; }
    public void setRelatedInvoiceNumber(String relatedInvoiceNumber) { this.relatedInvoiceNumber = relatedInvoiceNumber; }
    public Double getAmountInr() { return amountInr != null ? amountInr : 0.0; }
    public void setAmountInr(Double amountInr) { this.amountInr = amountInr; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
