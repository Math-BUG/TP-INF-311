package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;

public class RubeusContactResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}