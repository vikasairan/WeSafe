package com.example.womensafety.models.Remote;

import com.example.womensafety.models.Myplaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface GoogleAPIService
    {
      @GET
      Call<Myplaces> getNearbyPlaces(@Url String url);
    }

