package com.grupo10.inf311.docscan;

import com.google.gson.annotations.SerializedName;

public class RubeusContactRequest {
    @SerializedName("token")
    private String token;

    public RubeusContactRequest(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}