package com.example.sample;

public final class SuccessResponse implements HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final Object data;
    
    public SuccessResponse(int statusCode, String statusMessage, Object data) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = data;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public Object getData() {
        return data;
    }
}
