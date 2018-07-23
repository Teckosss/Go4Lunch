package com.deguffroy.adrien.go4lunch.Utils;

import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.MapPlacesInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Adrien Deguffroy on 20/07/2018.
 */
public interface PlacesService {
    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=50.3730841,3.5595396&radius=1000&type=restaurant&key=AIzaSyAUayt1xNiRgY4sCb5Zw0XpofM18nY7pt8

    @GET("nearbysearch/json")
    Observable<MapPlacesInfo> getNearbyPlaces(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
}