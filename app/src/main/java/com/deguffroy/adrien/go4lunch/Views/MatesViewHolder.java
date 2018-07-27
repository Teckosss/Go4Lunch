package com.deguffroy.adrien.go4lunch.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.Result;
import com.deguffroy.adrien.go4lunch.Models.User;
import com.deguffroy.adrien.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Adrien Deguffroy on 27/07/2018.
 */
public class MatesViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.mates_main_picture) ImageView mImageView;
    @BindView(R.id.mates_textview_username) TextView mTextView;
    @BindView(R.id.mates_textview_eatingAt) TextView mTextViewEatingAt;
    @BindView(R.id.mates_textview_restaurant) TextView mTextViewRestaurant;

    public MatesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void updateWithData(User results){
        RequestManager glide = Glide.with(itemView);
        if (!(results.getUrlPicture() == null)){
            glide.load(results.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(mImageView);
        }else{
            glide.load(R.drawable.ic_no_image_available).apply(RequestOptions.circleCropTransform()).into(mImageView);
        }
        if (results.getRestaurantSelected().equals("null")){
            this.mTextView.setText(results.getUsername());
            this.mTextViewEatingAt.setText(R.string.mates_hasnt_decided);
            this.changeTextColor(R.color.colorGray);
        }else{
            this.mTextView.setText(results.getUsername());
            this.mTextViewEatingAt.setText(R.string.mates_is_eating_at);
            this.mTextViewRestaurant.setText(results.getRestaurantSelected());
            this.changeTextColor(R.color.colorBlack);
        }
    }

    private void changeTextColor(int color){
        int mColor = itemView.getContext().getResources().getColor(color);
        this.mTextView.setTextColor(mColor);
        this.mTextViewEatingAt.setTextColor(mColor);
        this.mTextViewRestaurant.setTextColor(mColor);
    }
}
