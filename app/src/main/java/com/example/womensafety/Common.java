package com.example.womensafety;

import com.example.womensafety.models.Remote.GoogleAPIService;
import com.example.womensafety.models.Remote.RetrofitClient;

public class Common {

    public static final String GOOGLE_API_URL = "https://maps.googleapis.com/";
    public static GoogleAPIService getGoogleAPIService(){
        return RetrofitClient.getClient(GOOGLE_API_URL).create(GoogleAPIService.class);
    }

}
