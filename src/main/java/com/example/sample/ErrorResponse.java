package com.example.sample;

public final class ErrorResponse implements HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final String errorDetail;
    
    public ErrorResponse(int statusCode, String statusMessage, String errorDetail) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.errorDetail = errorDetail;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public String getErrorDetail() {
        return errorDetail;
    }
}
