package com.example.sample;

public final class PendingPayment extends PaymentResult {
    private final String callbackUrl;
    
    public PendingPayment(String transactionId, String callbackUrl) {
        super(transactionId);
        this.callbackUrl = callbackUrl;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
}
