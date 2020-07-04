package com.example.roome.Apartment_searcher_tabs_classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roome.general_activities.ChoosingActivity;
import com.example.roome.apartment_searcher_activities.MainActivityApartmentSearcher;
import com.example.roome.R;
import com.example.roome.apartment_searcher_activities.RecyclerAdapter;

import java.util.ArrayList;

/**
 * This class implements the Matches fragment. It shows the Apartments the user liked and was liked
 * back (there was a match). Info such as Rent and location is shown below the picture of the matched
 * apartment. In addition the phone number is shown - this enables privacy since the phone number
 * is shown ony after there is a match (both sides approved one another).
 */
public class MatchesApartmentSearcher extends Fragment {

    private RecyclerView recyclerView;
//    private int[] images;
    private RecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Inflate the layout for this fragment
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_matches_apartment_searcher, container, false);
    }

    /**
     * Initializes the Matches fragment by presenting in the page the matched apartments - takes the
     * data from the firebase
     * @param savedInstanceState Bundle
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ArrayList<String> matchedUids  =
                MainActivityApartmentSearcher.getSpecificList(ChoosingActivity.MATCH);
//        initMatchedImages(matchedUids);
        if (matchedUids.size() != 0){
            ImageView noMatches = getView().findViewById(R.id.iv_no_matches);
            noMatches.setVisibility(View.INVISIBLE);
            recyclerView = getView().findViewById(R.id.rv_matches_as);
            layoutManager = new GridLayoutManager(getContext(), 2);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new RecyclerAdapter(matchedUids);
            recyclerView.setAdapter(adapter);
        }
        super.onActivityCreated(savedInstanceState);
    }

//    /**
//     * Initializes the image array of matched apartments
//     * @param matchedUids an array containing the UIDs of roommate searcher users that are matched
//     * to this user
//     */
//    private void initMatchedImages(ArrayList<String> matchedUids) {
//        images = new int[matchedUids.size()];
//        for (int i=0;i<matchedUids.size();i++){
//            images[i]= UsersImageConnector.getImageByUid(matchedUids.get(i),
//                    UsersImageConnector.ROOMMATE_USER);
//        }
//    }
}
