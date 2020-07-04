package com.example.roome.roommate_searcher_activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roome.R;
import com.example.roome.general_activities.ChoosingActivity;
import com.example.roome.roommate_searcher_activities.MainActivityRoommateSearcher;
import com.example.roome.roommate_searcher_activities.RecyclerAdapterRS;

import java.util.ArrayList;

public class TrashRoommateSearcherActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerAdapterRS adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash_roommate_searcher);
    }

    /**
     * Initializes the Matches fragment by presenting in the page the matched apartments - takes the
     * data from the firebase
     */
    @Override
    public void onResume() {
        super.onResume();
        ArrayList<String> deletedApartmentSearchersUids  =
                MainActivityRoommateSearcher.getSpecificList(ChoosingActivity.NO_TO_ROOMMATE);
        if (deletedApartmentSearchersUids.size() != 0){
            TextView noDeleted = findViewById(R.id.tv_no_more_deleted_rs);
            noDeleted.setVisibility(View.INVISIBLE);
            recyclerView = findViewById(R.id.rv_delete_list_as);
            layoutManager = new GridLayoutManager(getApplicationContext(), 2);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new RecyclerAdapterRS(deletedApartmentSearchersUids);
            recyclerView.setAdapter(adapter);
        }
    }
}
