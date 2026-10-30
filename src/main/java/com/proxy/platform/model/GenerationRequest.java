package com.proxy.platform.model;

import jakarta.validation.constraints.NotBlank;

public class GenerationRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "prompt is required")
    private String prompt;

    private String plan;

    public GenerationRequest() {
    }

    public GenerationRequest(String userId, String prompt, String plan) {
        this.userId = userId;
        this.prompt = prompt;
        this.plan = plan;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }
}
