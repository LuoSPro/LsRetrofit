package com.ls.lsretrofit;


import com.ls.lsretrofit.annotation.Field;
import com.ls.lsretrofit.annotation.GET;
import com.ls.lsretrofit.annotation.POST;
import com.ls.lsretrofit.annotation.Query;

import okhttp3.Call;

public interface WeatherApi {

    @POST("/v3/weather/weatherInfo")
    Call postWeather(@Field("city") String city, @Field("key") String key);


    @GET("/v3/weather/weatherInfo")
    Call getWeather(@Query("city") String city, @Query("key") String key);
}
