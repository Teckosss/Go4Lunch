package com.deguffroy.adrien.go4lunch.Views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deguffroy.adrien.go4lunch.Models.User;
import com.deguffroy.adrien.go4lunch.R;

import java.util.List;

/**
 * Created by Adrien Deguffroy on 30/08/2018.
 */
public class DetailAdapter extends RecyclerView.Adapter<DetailViewHolder> {
    // FOR DATA
    private List<User> mResults;

    // CONSTRUCTOR
    public DetailAdapter(List<User> result) {
        this.mResults = result;
    }

    @Override
    public DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_place_detail_item, parent,false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DetailViewHolder viewHolder, int position) {
        viewHolder.updateWithData(this.mResults.get(position));
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mResults != null) itemCount = mResults.size();
        return itemCount;
    }
}
