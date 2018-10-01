package com.deguffroy.adrien.go4lunch.Utils;

import com.deguffroy.adrien.go4lunch.Models.AutoComplete.AutoCompleteResult;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.MapPlacesInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Adrien Deguffroy on 20/07/2018.
 */
public interface PlacesService {
    @GET("nearbysearch/json?")
    Observable<MapPlacesInfo> getNearbyPlaces(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key);

    @GET("details/json")
    Observable<PlaceDetailsInfo> getPlacesInfo(@Query("placeid") String placeId, @Query("key") String key);

    @GET("autocomplete/json?strictbounds&types=establishment")
    Observable<AutoCompleteResult> getPlaceAutoComplete(@Query("input") String query, @Query("location") String location, @Query("radius") int radius, @Query("key") String apiKey );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)).build())
            .build();
}