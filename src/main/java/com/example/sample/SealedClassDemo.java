package com.example.sample;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

public class SealedClassDemo {
    public static void main(String[] args) {
        // 1. Shape 예제 사용
        System.out.println("===== Shape 예제 =====");
        List<Shape> shapes = List.of(
            new Circle("원", 5.0),
            new Rectangle("직사각형", 4.0, 6.0),
            new Triangle("삼각형", 3.0, 4.0)
        );
        
        for (Shape shape : shapes) {
            System.out.println(shape.getName() + "의 면적: " + shape.area());
            
            // Java 17+ 패턴 매칭 사용
            String details = switch (shape) {
                case Circle c -> "반지름: " + c.getRadius();
                case Rectangle r -> "너비: " + r.getWidth() + ", 높이: " + r.getHeight();
                case Triangle t -> "밑변: " + t.getBase() + ", 높이: " + t.getHeight();
            };
            System.out.println("  세부 정보: " + details);
        }
        
        // 2. Vehicle 예제 사용
        System.out.println("\n===== Vehicle 예제 =====");
        List<Vehicle> vehicles = List.of(
            new Sedan("123가4567", 5, true),
            new SUV("234나5678", 7, true),
            new Motorcycle("345다6789", false),
            new Truck("456라7890", 5000)
        );
        
        for (Vehicle vehicle : vehicles) {
            System.out.println("등록번호: " + vehicle.getRegistrationNumber());
            System.out.println("  바퀴 수: " + vehicle.getWheelCount());
            
            // Java 17+ 패턴 매칭 사용
            String vehicleType = switch (vehicle) {
                case Sedan s -> "세단 (트렁크: " + (s.hasTrunk() ? "있음" : "없음") + ")";
                case SUV suv -> "SUV (4륜구동: " + (suv.hasAllWheelDrive() ? "있음" : "없음") + ")";
                case Motorcycle m -> "오토바이 (사이드카: " + (m.hasSidecar() ? "있음" : "없음") + ")";
                case Truck t -> "트럭 (최대 적재량: " + t.getMaxPayload() + "kg)";
                default -> "알 수 없는 차량 유형";
            };
            System.out.println("  차량 유형: " + vehicleType);
        }
        
        // 3. HttpResponse 예제 사용
        System.out.println("\n===== HttpResponse 예제 =====");
        List<HttpResponse> responses = List.of(
            new SuccessResponse(200, "OK", Map.of("name", "홍길동", "age", 30)),
            new ErrorResponse(404, "Not Found", "Resource not found at the specified URL"),
            new RedirectResponse(302, "Found", "https://www.example.com/new-location")
        );
        
        for (HttpResponse response : responses) {
            System.out.println("Status: " + response.getStatusCode() + " " + response.getStatusMessage());
            
            // Java 17+ 패턴 매칭 사용
            String details = switch (response) {
                case SuccessResponse s -> "데이터: " + s.getData();
                case ErrorResponse e -> "오류 상세: " + e.getErrorDetail();
                case RedirectResponse r -> "리다이렉트 URL: " + r.getRedirectUrl();
            };
            System.out.println("  " + details);
        }
        
        // 4. Operation 예제 사용
        System.out.println("\n===== Operation 예제 =====");
        List<Operation> operations = List.of(
            new Addition(10, 5),
            new Subtraction(10, 5),
            new Multiplication(10, 5),
            new Division(10, 5)
        );
        
        for (Operation operation : operations) {
            System.out.println("실행 결과: " + operation.execute());
            
            // Calculator 클래스의 패턴 매칭 사용
            System.out.println("  Calculator 결과: " + Calculator.calculate(operation));
        }
        
        // 5. PaymentResult 예제 사용
        System.out.println("\n===== PaymentResult 예제 =====");
        List<PaymentResult> paymentResults = List.of(
            new SuccessfulPayment("TX12345", "CONF98765"),
            new FailedPayment("TX23456", "ERR_INSUFFICIENT_FUNDS", "잔액이 부족합니다"),
            new PendingPayment("TX34567", "https://api.example.com/callbacks/payments")
        );
        
        PaymentService paymentService = new PaymentService();
        
        for (PaymentResult result : paymentResults) {
            ResponseEntity<?> response = paymentService.processPaymentResult(result);
            System.out.println("응답 상태 코드: " + response.getStatusCode());
            System.out.println("  응답 본문: " + response.getBody());
        }
    }
}
