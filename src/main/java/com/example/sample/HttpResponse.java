package com.example.sample;

// 인터페이스와 함께 사용하는 sealed 클래스
public sealed interface HttpResponse permits SuccessResponse, ErrorResponse, RedirectResponse {
    int getStatusCode();
    String getStatusMessage();
}
