package com.example.roome.roommate_searcher_activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.roome.R;
import com.example.roome.Roommate_searcher_tabs_classes.EditProfileRoommateSearcher;
import com.example.roome.Roommate_searcher_tabs_classes.MatchesRoommateSearcher;
import com.example.roome.Roommate_searcher_tabs_classes.RoommateSearcherHome;
import com.example.roome.general_activities.ChoosingActivity;
import com.example.roome.general_activities.MainActivity;
import com.example.roome.general_activities.MyPreferences;
import com.example.roome.user_classes.RoommateSearcherUser;
import com.example.roome.utils.CustomViewPager;
import com.example.roome.utils.FirebaseMediate;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The mainActivity for the roommate searcher , contains the tab layout
 * and viewPager handlers
 */
public class MainActivityRoommateSearcher extends AppCompatActivity {


    private static final int OFFSCREEN_PAGE_LIMIT = 3;
    private TabLayout tabLayout;
    private CustomViewPager viewPager;
    private int[] selectedtabIcons = {R.drawable.ic_action_filled_home,
            R.drawable.ic_action_filled_heart,
            R.drawable.ic_action_filled_person};

    private int[] unselectedtabIcons = {R.drawable.ic_action_empty_home,
            R.drawable.ic_action_empty_heart,
            R.drawable.ic_action_empty_person};

    /* Firebase instance variables */
    private FirebaseDatabase firebaseDatabase;

    /* All lists of the apartment user */
    public static HashMap<String,ArrayList<String>> allLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_main_roommate_searcher);
        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();

        allLists = new HashMap<>();

        viewPager = (CustomViewPager) findViewById(R.id.viewpager_roomate);
        viewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs_roomate);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        addTabLayoutListeners();
        if(isFirstTimeInApp()) {
            startActivityOnEditProfileTab();
        }
        retrieveUserLists();
        updateUserLists();


    }

    private boolean isFirstTimeInApp() {
        boolean isFirstTime = MyPreferences.isFirstTime(getApplicationContext());
        if (isFirstTime) {
            MyPreferences.setIsFirstTimeToFalse(getApplicationContext());
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        String rmtUid = MyPreferences.getUserUid(getApplicationContext());
        if (rmtUid == null){return;}
        for (String listName : allLists.keySet()){
            FirebaseMediate.setRoommatePrefList(listName,rmtUid,allLists.get(listName));
        }
    }


    /**
     * updating the lists of the user ( according to prefs)
     */
    private void updateUserLists() {
        ArrayList<String> updated_not_seen =
                makeUniqueValuesList(allLists.get(ChoosingActivity.NOT_SEEN));
        allLists.put(ChoosingActivity.NOT_SEEN,updated_not_seen);
        deleteDeletedAptUsersFromAllLists();
        deleteUnseenValuesFromAllLists();
    }

    /**
     * deleting the values from the unseen list from the other list of the user
     */
    private void deleteUnseenValuesFromAllLists() {
        // when roommate side will be done
        for (String aptId : allLists.get(ChoosingActivity.NOT_SEEN))
        {
            removeValueFromList(ChoosingActivity.NO_TO_ROOMMATE,aptId);
            removeValueFromList(ChoosingActivity.NOT_MATCH,aptId);
            removeValueFromList(ChoosingActivity.MATCH,aptId);
        }
    }

    /**
     * deleting the values from the unseen list from the other list of the user
     */
    private void deleteDeletedAptUsersFromAllLists() {
        // when roommate side will be done
        for (String aptId : allLists.get(ChoosingActivity.DELETE_USERS))
        {
            removeValueFromList(ChoosingActivity.NO_TO_ROOMMATE,aptId);
            removeValueFromList(ChoosingActivity.NOT_MATCH,aptId);
            removeValueFromList(ChoosingActivity.MATCH,aptId);
            removeValueFromList(ChoosingActivity.NOT_SEEN,aptId);
        }
        setSpecificList(ChoosingActivity.DELETE_USERS,new ArrayList<String>());
    }

    /**
     * deleting duplicate values from the list
     * @param list - the list
     * @return list with unique values
     */
    private ArrayList<String> makeUniqueValuesList(ArrayList<String> list) {
        //todo check if working
        Set<String> uniqueSet = new HashSet<>(list);
        ArrayList<String> uniqueArray = new ArrayList<>();
        uniqueArray.addAll(uniqueSet);
        return uniqueArray;
    }

    private void startActivityOnEditProfileTab() {
        tabLayout.getTabAt(2).select();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(selectedtabIcons[0]);
        tabLayout.getTabAt(1).setIcon(unselectedtabIcons[1]);
        tabLayout.getTabAt(2).setIcon(unselectedtabIcons[2]);
    }


    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new RoommateSearcherHome(), "HOME");
        adapter.addFragment(new MatchesRoommateSearcher(), "MATCHES");
        adapter.addFragment(new EditProfileRoommateSearcher(), "PROFILE");
        viewPager.setAdapter(adapter);
    }

    public void showSignOutDialog(View view) {
        AccountDeleter deleter = new AccountDeleter(this,this);
        deleter.showSignOutDialog();
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }


    /**
     * Adding tab layout listeners
     */
    private void addTabLayoutListeners() {
        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        tab.setIcon(selectedtabIcons[tab.getPosition()]);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        tab.setIcon(unselectedtabIcons[tab.getPosition()]);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
    }

    private void retrieveUserLists() {
        String rmtUid = MyPreferences.getUserUid(getApplicationContext());
        allLists.put(ChoosingActivity.NOT_SEEN,FirebaseMediate.getRoommatePrefList(ChoosingActivity.NOT_SEEN,
                rmtUid)); //todo: check dis shit
        allLists.put(ChoosingActivity.NO_TO_ROOMMATE,
                FirebaseMediate.getRoommatePrefList(ChoosingActivity.NO_TO_ROOMMATE,
                        rmtUid));
        allLists.put(ChoosingActivity.NOT_MATCH,
                FirebaseMediate.getRoommatePrefList(ChoosingActivity.NOT_MATCH,
                        rmtUid)); //todo: delete
        allLists.put(ChoosingActivity.MATCH,
                FirebaseMediate.getRoommatePrefList(ChoosingActivity.MATCH,
                        rmtUid));
        allLists.put(ChoosingActivity.DELETE_USERS,
                FirebaseMediate.getRoommatePrefList(ChoosingActivity.DELETE_USERS,
                        rmtUid));
    }


    public static ArrayList<String> getSpecificList(String listName){
        return allLists.get(listName);
    }
    public static void setSpecificList(String listName,ArrayList<String> list){
        allLists.put(listName,list);
    }
    public static boolean removeValueFromList(String listName,String value){
        ArrayList<String> list = getSpecificList(listName);
        boolean exist = list.remove(value);
        setSpecificList(listName,list);
        return exist;
    }
    public static void addValueToList(String listName,String value){
        ArrayList<String> list = getSpecificList(listName);
        list.add(value);
        setSpecificList(listName,list);
    }

    /**
     * @return - The apartment searcher user (from firebase).
     */
    private RoommateSearcherUser getCurrentRoommateSearcherUser() {
        String rmtUid = MyPreferences.getUserUid(getApplicationContext());
        return FirebaseMediate.getRoommateSearcherUserByUid(rmtUid);
    }


    /**
     * Setting animation for the activity
     */
    public void setAnimation() {
        if (Build.VERSION.SDK_INT > MainActivity.MIN_SUPPORTED_API_LEVEL) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(ChoosingActivity.ANIMATION_DELAY_TIME);
            slide.setInterpolator(new DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }
}
