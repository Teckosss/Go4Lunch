package com.deguffroy.adrien.go4lunch.Views;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.Location;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.Result;
import com.deguffroy.adrien.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

/**
 * Created by Adrien Deguffroy on 22/07/2018.
 */
public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_imageview_1star) ImageView m1StarPicture;
    @BindView(R.id.item_imageview_2star) ImageView m2StarPicture;
    @BindView(R.id.item_imageview_3star) ImageView m3StarPicture;
    @BindView(R.id.item_imageview_main_pic) ImageView mMainPicture;
    @BindView(R.id.item_imageview_mates) ImageView mMatesPicture;
    @BindView(R.id.item_textview_name) TextView mNameText;
    @BindView(R.id.item_textview_address) TextView mAddressText;
    @BindView(R.id.item_textview_distance) TextView mDistanceText;
    @BindView(R.id.item_textview_mates) TextView mMatesText;
    @BindView(R.id.item_textview_opening) TextView mOpeningText;

    private float[] distanceResults = new float[3];

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void updateWithData(Result results, String userLocation){
        RequestManager glide = Glide.with(itemView);
        getLatLng(userLocation,results.getGeometry().getLocation());
        String distance = Integer.toString(Math.round(distanceResults[0]));
        this.mDistanceText.setText(distance+"m");
        this.mNameText.setText(results.getName());
        this.mAddressText.setText(results.getVicinity());

        //this.mDistanceText.setText(results.get(0).getName());
        //this.mMatesText.setText(results.get(0).getName());
        if (results.getOpeningHours() != null){
            if (results.getOpeningHours().getOpenNow().toString().equals("false")){
                this.mOpeningText.setText(R.string.restaurant_closed);
                this.mOpeningText.setTextColor(itemView.getContext().getResources().getColor(R.color.colorError));
            }
        }else{
            this.mOpeningText.setText(R.string.restaurant_opening_not_know);
        }
        if (!(results.getPhotos() == null)){
            if (!(results.getPhotos().isEmpty())){
            }
        }else{
            glide.load(R.drawable.ic_no_image_available).apply(RequestOptions.centerCropTransform()).into(mMainPicture);
        }
    }

    private void getLatLng(String startLocation, Location endLocation){
        String[] separatedStart = startLocation.split(",");
        double startLatitude = Double.parseDouble(separatedStart[0]);
        double startLongitude = Double.parseDouble(separatedStart[1]);
        double endLatitude = endLocation.getLat();
        double endLongitude = endLocation.getLng();
        android.location.Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude,distanceResults);
    }
}