package com.deguffroy.adrien.go4lunch.Utils;

import android.util.Log;

import com.deguffroy.adrien.go4lunch.Models.AutoComplete.AutoCompleteResult;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.MapPlacesInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Adrien Deguffroy on 20/07/2018.
 */
public class PlacesStreams {
    private static PlacesService mapPlacesInfo = PlacesService.retrofit.create(PlacesService.class);

    public static Observable<MapPlacesInfo> streamFetchNearbyPlaces(String location, int radius, String type, String key){
        return mapPlacesInfo.getNearbyPlaces(location,radius,type,key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static  Observable<List<PlaceDetailsResults>> streamFetchPlaceInfo(String location, int radius, String type, String key){
        return mapPlacesInfo.getNearbyPlaces(location,radius,type,key)
                .flatMapIterable(MapPlacesInfo::getResults)
                .flatMap(info -> mapPlacesInfo.getPlacesInfo(info.getPlaceId(), key))
                .map(PlaceDetailsInfo::getResult)
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<PlaceDetailsInfo> streamSimpleFetchPlaceInfo(String placeId, String key){
      return  mapPlacesInfo.getPlacesInfo(placeId, key)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<AutoCompleteResult> streamFetchAutoComplete(String query, String location, int radius, String apiKey){
        return mapPlacesInfo.getPlaceAutoComplete(query, location, radius, apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10,TimeUnit.SECONDS);
    }

    public static  Observable<List<PlaceDetailsResults>> streamFetchAutoCompleteInfo(String query, String location, int radius, String apiKey){
        return mapPlacesInfo.getPlaceAutoComplete(query, location, radius, apiKey)
                .flatMapIterable(AutoCompleteResult::getPredictions)
                .flatMap(info -> mapPlacesInfo.getPlacesInfo(info.getPlaceId(), apiKey))
                .map(PlaceDetailsInfo::getResult)
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}