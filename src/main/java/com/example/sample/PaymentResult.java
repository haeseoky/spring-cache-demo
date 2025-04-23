package com.example.sample;

import java.time.LocalDateTime;

// Spring에서 사용하는 sealed 클래스 예제
public sealed class PaymentResult permits SuccessfulPayment, FailedPayment, PendingPayment {
    private final String transactionId;
    private final LocalDateTime timestamp;
    
    public PaymentResult(String transactionId) {
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
