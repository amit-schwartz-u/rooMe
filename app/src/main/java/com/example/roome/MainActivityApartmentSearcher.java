package com.example.roome;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.example.roome.Apartment_searcher_tabs_classes.ApartmentSearcherHome;
import com.example.roome.Apartment_searcher_tabs_classes.EditProfileApartmentSearcher;
import com.example.roome.Apartment_searcher_tabs_classes.MatchesApartmentSearcher;
import com.example.roome.user_classes.ApartmentSearcherUser;
import com.example.roome.user_classes.RoommateSearcherUser;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The mainActivity for the apartment searcher , contains the tab layout
 * and viewPager handlers
 */
public class MainActivityApartmentSearcher extends AppCompatActivity {


    public static ApartmentSearcherUser aUser; //todo we need this?

    /* Tabs and viewPager */
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
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;

    /* All lists of the apartment user */
    public static HashMap<String,ArrayList<String>> allLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_main_apartment_searcher);
        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabaseReference = firebaseDatabase.getReference();
        aUser = getCurrentApartmentSearcherUser(); //todo we need this?
        allLists = new HashMap<>(); // local lists of the current user
        //initialize viewPager and tabs
        viewPager = (CustomViewPager) findViewById(R.id.viewpager_apartment);
        viewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs_apartment);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        addTabLayoutListeners();
        retrieveUserLists();
        checkIfNewMatchFromLastSession();
        updateUserLists();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        String aptUid = MyPreferences.getUserUid(getApplicationContext());
        if ( aptUid==null){return;}
        for (String listName : allLists.keySet()){
            FirebaseMediate.setAptPrefList(listName,aptUid,allLists.get(listName));
        }
    }
    public void showSignOutDialog(View view) {
        AccountDeleter deleter = new AccountDeleter(this,this);
        deleter.showSignOutDialog();
    }

    /**
     * updating the lists of the user ( according to prefs)
     */
    private void updateUserLists() {
        // first time
        ArrayList<String> updated_not_seen =
                makeUniqueValuesList(allLists.get(ChoosingActivity.NOT_SEEN));
        allLists.put(ChoosingActivity.NOT_SEEN,updated_not_seen);
        deleteDeletedRmtUsersFromAllLists();
        deleteUnseenValuesFromAllLists();
        filterOutRoommatesFromList(getCurrentApartmentSearcherUser());
    }

    /**
     * deleting the values from the deleted list from the other list of the user
     */
    private void deleteDeletedRmtUsersFromAllLists() {
        for (String rmtId : allLists.get(ChoosingActivity.DELETE_USERS))
        {
            removeValueFromList(ChoosingActivity.NO_TO_HOUSE,rmtId);
            removeValueFromList(ChoosingActivity.NOT_MATCH,rmtId);
            removeValueFromList(ChoosingActivity.MATCH,rmtId);
            removeValueFromList(ChoosingActivity.NOT_SEEN,rmtId);
        }
        setSpecificList(ChoosingActivity.DELETE_USERS,new ArrayList<String>());
    }

    /**
     * deleting the values from the unseen list from the other list of the user
     */
    private void deleteUnseenValuesFromAllLists() {
        // when roommate side will be done
        for (String roommateId : allLists.get(ChoosingActivity.NOT_SEEN))
        {
            removeValueFromList(ChoosingActivity.NO_TO_HOUSE,roommateId);
            removeValueFromList(ChoosingActivity.NOT_MATCH,roommateId);
            removeValueFromList(ChoosingActivity.MATCH,roommateId);
        }
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

    /**
     * retrieve the preferences list of the user from the firebase, stores in
     * the local allLists
     */
    private void retrieveUserLists() {
        String aptUid = MyPreferences.getUserUid(getApplicationContext());
        allLists.put(ChoosingActivity.NOT_SEEN,FirebaseMediate.getAptPrefList(ChoosingActivity.NOT_SEEN,
                aptUid));
        allLists.put(ChoosingActivity.NO_TO_HOUSE,
                FirebaseMediate.getAptPrefList(ChoosingActivity.NO_TO_HOUSE,
                aptUid));
        allLists.put(ChoosingActivity.NOT_MATCH,
                FirebaseMediate.getAptPrefList(ChoosingActivity.NOT_MATCH,
                aptUid));
        allLists.put(ChoosingActivity.MATCH,
                FirebaseMediate.getAptPrefList(ChoosingActivity.MATCH,
                aptUid));
        allLists.put(ChoosingActivity.DELETE_USERS,
                FirebaseMediate.getAptPrefList(ChoosingActivity.DELETE_USERS,
                        aptUid));
    }

    /**
     * return specific list from allLists
     * @param listName - name of the list
     * @return the list
     */
    public static ArrayList<String> getSpecificList(String listName){
        return allLists.get(listName);
    }

    /**
     * set specific list to the given list
     * @param listName - name of the list
     * @param list - the given list
     */
    public static void setSpecificList(String listName,ArrayList<String> list){
        allLists.put(listName,list);
    }

    /**
     * remove value from specific list
     * @param listName - name of the list
     * @param value - value to remove
     * @return True - the value existed in the list , False otherwise
     */
    public static boolean removeValueFromList(String listName,String value){
        ArrayList<String> list = getSpecificList(listName);
        boolean exist = list.remove(value);
        setSpecificList(listName,list);
        return exist;
    }

    /**
     * add specific value to specific list
     * @param listName - name of the list
     * @param value - value to add
     */
    public static void addValueToList(String listName,String value){
        ArrayList<String> list = getSpecificList(listName);
        list.add(value);
        setSpecificList(listName,list);
    }


    /**
     * filtering the "not seen" and "not match" lists according to the user
     * filters , updating reflects in the local allLists object
     * @param asUser - the user object
     */
    public static void filterOutRoommatesFromList(ApartmentSearcherUser asUser) {
        ArrayList<String> listRoommatesIds =
                getSpecificList(ChoosingActivity.NOT_SEEN);
        ArrayList<String> filteredOutRoommatesIds =
                getSpecificList(ChoosingActivity.NOT_MATCH);
        ArrayList<String> updatedUnSeenRoommatesIds = new ArrayList<>();
        ArrayList<String> updatedFilteredOutRoommatesIds = new ArrayList<>();
        listRoommatesIds.addAll(filteredOutRoommatesIds);
        for (String roommateId : listRoommatesIds) {
            RoommateSearcherUser roommate = FirebaseMediate.getRoommateSearcherUserByUid(roommateId);
            if (roommate.getApartment() != null) {
                double roommatesApartmentRent = roommate.getApartment().getRent();
                if (roommatesApartmentRent <= asUser.getMaxRent() &&
                        roommatesApartmentRent >= asUser.getMinRent()) { //checks if there's a match
                    // according to filters
                    updatedUnSeenRoommatesIds.add(roommateId);
                } else {
                    updatedFilteredOutRoommatesIds.add(roommateId);
                }
            }
            else {
                updatedFilteredOutRoommatesIds.add(roommateId);
            }
        }
        getSpecificList(ChoosingActivity.NOT_SEEN).clear();
        getSpecificList(ChoosingActivity.NOT_MATCH).clear();
        getSpecificList(ChoosingActivity.NOT_SEEN).addAll(updatedUnSeenRoommatesIds);
        getSpecificList(ChoosingActivity.NOT_MATCH).addAll(updatedFilteredOutRoommatesIds);
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

    /**
     * @return - The apartment searcher user (from firebase).
     */
    private ApartmentSearcherUser getCurrentApartmentSearcherUser() {
        String aptUid = MyPreferences.getUserUid(getApplicationContext());
        return FirebaseMediate.getApartmentSearcherUserByUid(aptUid);
    }

    /**
     * Setting icons of the tabs
     */
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(selectedtabIcons[0]);
        tabLayout.getTabAt(1).setIcon(unselectedtabIcons[1]);
        tabLayout.getTabAt(2).setIcon(unselectedtabIcons[2]);
    }

    /**
     * Setting the fragments connected to the tabs
     * @param viewPager - The viewPager
     */
    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ApartmentSearcherHome(), "HOME");
        adapter.addFragment(new MatchesApartmentSearcher(), "MATCHES");
        adapter.addFragment(new EditProfileApartmentSearcher(), "PROFILE");
        viewPager.setAdapter(adapter);
    }

    /**
     * Adapter for the fragments with animation
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        /**
         * Getting the item in the given position
         * @param position - The position to take the item from
         */
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        /**
         * returns the size of the adapter
         */
        @Override
        public int getCount() {
            return fragmentList.size();
        }

        /**
         * Adding fragment to the adapter
         * @param fragment - The fragment to add
         * @param title - Title of the fragment
         */
        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        /**
         * returns the page title for the fragment in the given position
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentList.get(position).getTag();
        }
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

    private void checkIfNewMatchFromLastSession() {
        int lastSize = MyPreferences.getLastNumberOfMatches(getApplicationContext());
        int newSize = allLists.get(ChoosingActivity.MATCH).size();
        if (newSize>lastSize){
            startNewMatchDialog();
        }
    }

    private void startNewMatchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("You have a new match!")
                .setMessage("check you matches tab for new matches")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDestroy() {
        int matchListSize = allLists.get(ChoosingActivity.MATCH).size();
        MyPreferences.setLastNumberOfMatches(getApplicationContext(), matchListSize);
        super.onDestroy();
    }
}
