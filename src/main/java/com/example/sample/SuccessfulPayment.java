package com.example.sample;

public final class SuccessfulPayment extends PaymentResult {
    private final String confirmationCode;
    
    public SuccessfulPayment(String transactionId, String confirmationCode) {
        super(transactionId);
        this.confirmationCode = confirmationCode;
    }
    
    public String getConfirmationCode() {
        return confirmationCode;
    }
}
