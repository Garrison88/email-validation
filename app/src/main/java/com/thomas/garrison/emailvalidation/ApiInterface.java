package com.thomas.garrison.emailvalidation;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("verify?")
    Call<Email> emailResponse(@Query("email") String email, @Query("apikey") String apikey);
}