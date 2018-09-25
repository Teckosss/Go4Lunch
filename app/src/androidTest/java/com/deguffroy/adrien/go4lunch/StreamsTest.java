package com.deguffroy.adrien.go4lunch;

import android.support.test.runner.AndroidJUnit4;

import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.MapPlacesInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.deguffroy.adrien.go4lunch.Utils.PlacesStreams;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.Observable;

import io.reactivex.observers.TestObserver;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Adrien Deguffroy on 24/09/2018.
 */

@RunWith(AndroidJUnit4.class)
public class StreamsTest {
    private String placeId;
    private String api_KEY;
    private String location;
    private int radius;
    private String type = "restaurant";

    @Before
    public void setUp() throws Exception {
        placeId = "ChIJ4V5WgPjtwkcRPCAt0H7xGF0";
        api_KEY = BuildConfig.google_maps_api_key;
        location = "50.3663983,3.55778";
        radius = 1000;
    }

    @Test
    public void checkSimplePlaceInfo() throws Exception {
        Observable<PlaceDetailsInfo> detailsInfoObservable = PlacesStreams.streamSimpleFetchPlaceInfo(placeId, api_KEY);
        TestObserver<PlaceDetailsInfo> testObserver = new TestObserver<>();
        detailsInfoObservable.subscribeWith(testObserver)
                .assertNoErrors()
                .assertNoTimeout()
                .awaitTerminalEvent();
        PlaceDetailsInfo placeDetailsInfo = testObserver.values().get(0);
        assertEquals("Dolce Pizza", placeDetailsInfo.getResult().getName());
    }

    @Test
    public void checkNearbyPlace() {
        Observable<MapPlacesInfo> detailsInfoObservable = PlacesStreams.streamFetchNearbyPlaces(location,radius,type, api_KEY);
        TestObserver<MapPlacesInfo> testObserver = new TestObserver<>();
        detailsInfoObservable.subscribeWith(testObserver)
                .assertNoErrors()
                .assertNoTimeout()
                .awaitTerminalEvent();
        MapPlacesInfo mapPlacesInfo = testObserver.values().get(0);
        assertEquals(true, mapPlacesInfo.getResults().size() > 0);
    }
}
