package com.example.roome.apartment_searcher_activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roome.R;
import com.example.roome.apartment_searcher_activities.MainActivityApartmentSearcher;
import com.example.roome.apartment_searcher_activities.RecyclerAdapter;
import com.example.roome.general_activities.ChoosingActivity;

import java.util.ArrayList;

public class TrashApartmentSearcherActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash_apartment_searcher);
    }

    /**
     * Initializes the Matches fragment by presenting in the page the matched apartments - takes the
     * data from the firebase
     */
    @Override
    public void onResume() {
        super.onResume();
        ArrayList<String> deletedRoommateSearchersUids =
                MainActivityApartmentSearcher.getSpecificList(ChoosingActivity.NO_TO_HOUSE);
        if (deletedRoommateSearchersUids.size() != 0) {
            TextView noDeleted = findViewById(R.id.tv_no_more_deleted);
            noDeleted.setVisibility(View.INVISIBLE);
            recyclerView = findViewById(R.id.rv_delete_list_as);
            layoutManager = new GridLayoutManager(getApplicationContext(), 2);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new RecyclerAdapter(deletedRoommateSearchersUids);
            recyclerView.setAdapter(adapter);
        }
    }
}
