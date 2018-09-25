package com.deguffroy.adrien.go4lunch.ViewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.deguffroy.adrien.go4lunch.Utils.PlacesStreams;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Adrien Deguffroy on 13/08/2018.
 */
public class ListViewModel extends ViewModel {
    private CompositeDisposable disposable = new CompositeDisposable();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<List<PlaceDetailsResults>> places = new MutableLiveData<>();
    public MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

    public void streamFetchPlaceInfo(String location, int radius, String type, String key){
        this.disposable.add(PlacesStreams.streamFetchPlaceInfo(location, radius, type, key)
                .doOnSubscribe(sub -> isLoading.postValue(true))
                .doOnComplete(() -> isLoading.postValue(false))
                .doOnError(error -> isLoading.postValue(false))
                .subscribe((List<PlaceDetailsResults>result) -> places.postValue(result),
                        (Throwable error) -> this.error.postValue(error)));
    }

}
