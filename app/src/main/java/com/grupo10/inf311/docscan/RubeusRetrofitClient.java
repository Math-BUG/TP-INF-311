package com.grupo10.inf311.docscan;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RubeusRetrofitClient {
    private static final String BASE_URL = "https://crmufvgrupo10.apprubeus.com.br/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static RubeusApiService getApiService() {
        return getClient().create(RubeusApiService.class);
    }
}