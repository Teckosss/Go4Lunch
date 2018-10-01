package com.deguffroy.adrien.go4lunch.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.deguffroy.adrien.go4lunch.Models.User;
import com.deguffroy.adrien.go4lunch.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Adrien Deguffroy on 30/08/2018.
 */
public class DetailViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.detail_main_picture) ImageView mImageView;
    @BindView(R.id.detail_textview_username) TextView mTextView;

    public DetailViewHolder(View itemView) {
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

        this.mTextView.setText(itemView.getResources().getString(R.string.restaurant_detail_recyclerview, results.getUsername()));
        this.changeTextColor(R.color.colorBlack);
    }

    private void changeTextColor(int color){
        int mColor = itemView.getContext().getResources().getColor(color);
        this.mTextView.setTextColor(mColor);
    }
}
