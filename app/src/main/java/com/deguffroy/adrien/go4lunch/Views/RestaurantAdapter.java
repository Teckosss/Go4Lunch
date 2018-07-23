package com.deguffroy.adrien.go4lunch.Views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.Result;
import com.deguffroy.adrien.go4lunch.R;

import java.util.List;

/**
 * Created by Adrien Deguffroy on 22/07/2018.
 */
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    // FOR DATA
    private List<Result> mResults;

    // CONSTRUCTOR
    public RestaurantAdapter(List<Result> result) {
        this.mResults = result;
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_list_item, parent,false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RestaurantViewHolder viewHolder, int position) {
        viewHolder.updateWithData(this.mResults.get(position));
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mResults != null) itemCount = mResults.size();
        return itemCount;
    }
}
