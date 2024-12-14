package com.example.lab4_fragments.network;

import com.example.lab4_fragments.models.RouteResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/v2/directions/driving-car")
    Call<RouteResponse> getRoute(
            @Query("api_key") String key,
            @Query("start") String start,
            @Query("end") String end
    );
}
