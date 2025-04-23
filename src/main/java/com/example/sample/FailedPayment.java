package com.example.sample;

public final class FailedPayment extends PaymentResult {
    private final String errorCode;
    private final String errorMessage;
    
    public FailedPayment(String transactionId, String errorCode, String errorMessage) {
        super(transactionId);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
