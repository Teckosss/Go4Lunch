package com.deguffroy.adrien.go4lunch.Utils;

import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.MapPlacesInfo;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Adrien Deguffroy on 20/07/2018.
 */
public class PlacesStreams {
    public static Observable<MapPlacesInfo> streamFetchNearbyPlaces(String location, int radius, String type, String key){
        PlacesService mapPlacesInfo = PlacesService.retrofit.create(PlacesService.class);
        return mapPlacesInfo.getNearbyPlaces(location,radius,type,key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}