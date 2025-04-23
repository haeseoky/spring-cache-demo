package com.example.sample;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

// Spring 서비스 클래스에서 사용 예제
@Service
public class PaymentService {
    public ResponseEntity<?> processPaymentResult(PaymentResult result) {
        return switch (result) {
            case SuccessfulPayment s -> ResponseEntity.ok()
                .body(Map.of(
                    "status", "success",
                    "transactionId", s.getTransactionId(),
                    "confirmationCode", s.getConfirmationCode(),
                    "timestamp", s.getTimestamp()
                ));
                
            case FailedPayment f -> ResponseEntity.badRequest()
                .body(Map.of(
                    "status", "failed",
                    "transactionId", f.getTransactionId(),
                    "errorCode", f.getErrorCode(),
                    "errorMessage", f.getErrorMessage(),
                    "timestamp", f.getTimestamp()
                ));
                
            case PendingPayment p -> ResponseEntity.accepted()
                .body(Map.of(
                    "status", "pending",
                    "transactionId", p.getTransactionId(),
                    "callbackUrl", p.getCallbackUrl(),
                    "timestamp", p.getTimestamp()
                ));
                
            default -> ResponseEntity.badRequest()
                .body(Map.of(
                    "status", "unknown",
                    "message", "Unknown payment result type"
                ));
        };
    }
}
