package com.deguffroy.adrien.go4lunch.Fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.MainActivity;
import com.deguffroy.adrien.go4lunch.Models.User;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.DividerItemDecoration;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.deguffroy.adrien.go4lunch.Views.MatesAdapter;
import com.deguffroy.adrien.go4lunch.Views.RestaurantAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatesFragment extends Fragment {

    @BindView(R.id.mates_recycler_view) RecyclerView mRecyclerView;

    private List<User> mUsers;
    private MatesAdapter mMatesAdapter;

    private CommunicationViewModel mViewModel;

    public static MatesFragment newInstance() {
        return new MatesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mates, container, false);
        ButterKnife.bind(this, view);

        mViewModel = ViewModelProviders.of(getActivity()).get(CommunicationViewModel.class);
        mViewModel.currentUserUID.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String uid) {
                configureRecyclerView();
                updateUIWhenCreating();
            }
        });

        return view;
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    // Configure RecyclerView, Adapter, LayoutManager & glue it together
    private void configureRecyclerView(){
        this.mUsers = new ArrayList<>();
        this.mMatesAdapter = new MatesAdapter(this.mUsers);
        this.mRecyclerView.setAdapter(this.mMatesAdapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    // --------------------
    // UI
    // --------------------

    // 1 - Update UI when activity is creating
    private void updateUIWhenCreating(){
        CollectionReference collectionReference = UserHelper.getUsersCollection();
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    mUsers.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Log.e("TAG", "UID from ViewModel : " + mViewModel.getCurrentUserUID());
                        //Log.e("TAG", "UID from Document : " + document.getData().get("uid").toString());
                        if (!(mViewModel.getCurrentUserUID().equals(document.getData().get("uid").toString()))){
                            String uid = document.getData().get("uid").toString();
                            String username = document.getData().get("username").toString();
                            String urlPicture = document.getData().get("urlPicture").toString();
                            String restaurantSelected;
                            if (document.getData().get("restaurantSelected") != "null"){
                                restaurantSelected = "null";
                            }else{
                                restaurantSelected = document.getData().get("restaurantSelected").toString();
                            }
                            User userToAdd = new User(uid,username,urlPicture,restaurantSelected);
                            mUsers.add(userToAdd);
                        }
                    }
                }else {
                    Log.e("TAG", "Error getting documents: ", task.getException());
                }
                mMatesAdapter.notifyDataSetChanged();
            }
        });
    }
}