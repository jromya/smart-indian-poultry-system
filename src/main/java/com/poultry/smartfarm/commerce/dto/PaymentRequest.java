package com.poultry.smartfarm.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class PaymentRequest {

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String farmId;

    // CUSTOMER_RECEIPT or SUPPLIER_PAYMENT
    @NotBlank(message = "Payment type (CUSTOMER_RECEIPT or SUPPLIER_PAYMENT) is required")
    private String paymentType;

    private String customerId;
    private String supplierId;
    private String relatedInvoiceId;

    @NotNull
    @Positive(message = "Payment amount must be greater than zero")
    private Double amountInr;

    private String paymentMethod = "UPI";
    private String transactionRef;
    private String remarks;

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getRelatedInvoiceId() { return relatedInvoiceId; }
    public void setRelatedInvoiceId(String relatedInvoiceId) { this.relatedInvoiceId = relatedInvoiceId; }
    public Double getAmountInr() { return amountInr; }
    public void setAmountInr(Double amountInr) { this.amountInr = amountInr; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
