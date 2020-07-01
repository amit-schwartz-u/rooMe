package com.example.roome.Roommate_searcher_tabs_classes;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roome.general_activities.ChoosingActivity;
import com.example.roome.roommate_searcher_activities.MainActivityRoommateSearcher;
import com.example.roome.R;
import com.example.roome.roommate_searcher_activities.RecyclerAdapterRS;

import java.util.ArrayList;

/**
 * **** DON'T CHECK! ****
 * this class is for future implementation (we did not implement the roommate searcher side)
 */
public class MatchesRoommateSearcher extends Fragment {

    private RecyclerView recyclerView;
    //    private int[] images;
    private RecyclerAdapterRS adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_matches_roommate_searcher, container, false);
    }


    /**
     * Initializes the Matches fragment by presenting in the page the matched apartments - takes the
     * data from the firebase
     * @param savedInstanceState Bundle
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ArrayList<String> matchedUids  =
                MainActivityRoommateSearcher.getSpecificList(ChoosingActivity.MATCH);
        if (matchedUids.size() != 0){
            ImageView noMatches = getView().findViewById(R.id.iv_no_matches_rs);
            noMatches.setVisibility(View.INVISIBLE);
            recyclerView = getView().findViewById(R.id.rv_matches_rs);
            layoutManager = new GridLayoutManager(getContext(), 2);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new RecyclerAdapterRS(matchedUids);
            recyclerView.setAdapter(adapter);
        }
        super.onActivityCreated(savedInstanceState);
    }

}