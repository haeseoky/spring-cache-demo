package com.example.sample;

public final class RedirectResponse implements HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final String redirectUrl;
    
    public RedirectResponse(int statusCode, String statusMessage, String redirectUrl) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.redirectUrl = redirectUrl;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
}
