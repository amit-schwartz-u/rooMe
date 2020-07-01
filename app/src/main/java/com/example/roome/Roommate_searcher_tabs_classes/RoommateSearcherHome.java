package com.example.roome.Roommate_searcher_tabs_classes;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.roome.general_activities.ChoosingActivity;
import com.example.roome.utils.FirebaseMediate;
import com.example.roome.roommate_searcher_activities.MainActivityRoommateSearcher;
import com.example.roome.general_activities.MyPreferences;
import com.example.roome.R;
import com.example.roome.roommate_searcher_activities.TrashRoommateSearcherActivity;
import com.example.roome.utils.UsersImageConnector;
import com.example.roome.user_classes.ApartmentSearcherUser;
import com.example.roome.user_classes.RoommateAdditionalInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lorenzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;

/**
 * This fragment is the main fragment.
 * The relevant and potential roommates profiles are displayed in this fragment.
 * The roommate searcher user can swipe left/right to unlike/like a apartment searcher user profile
 * When the user clicks the card he can see additional info on the current profile.
 */
public class RoommateSearcherHome extends Fragment {

    /* Firebase */
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;

    /* Views references */
    private ImageView trashCanImage;
    private TextView nameTxt;
    private TextView ageTxt;


    /* For swipe */
    public static RoommateSearcherHome.MyAppAdapter myAppAdapter;
    public static RoommateSearcherHome.ViewHolder viewHolder;
    private SwipeFlingAdapterView flingContainer;

    /* Other class members */
    private ArrayList<String> relevantApartmentSearchersIds;
    private ArrayList<Integer> temp_img;
    //    private EditFiltersApartmentSearcher editFiltersDialog; //todo delete
    private RoommateAdditionalInfo bioDialog;
    public ApartmentSearcherUser currentApartmentSearcher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseReference = mFirebaseDatabase.getReference();
        boolean isFirstTime = MyPreferences.isFirstTime(getContext()); // todo
        //if we want to do something if its the first time?
        if (isFirstTime) {
            MyPreferences.setIsFirstTimeToFalse(getContext());
        }
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_roommate_searcher_home, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        trashCanImage = getView().findViewById(R.id.iv_trash_can_RS);
//        editFiltersImage = getView().findViewById(R.id.iv_edit_filters); //todo delete
//        editFiltersDialog = new EditFiltersApartmentSearcher(); //todo delete
        bioDialog = new RoommateAdditionalInfo();
        setClickListeners();
//        setFirebaseListeners();  //todo include this line
        retrieveRelevantApartmentSearchers();
        swipeOnCreate();
//        moreRoommates();
        super.onActivityCreated(savedInstanceState);

    }

    /**
     * create swipe adapter
     */
    private void swipeOnCreate() {
        flingContainer = getView().findViewById(R.id.frame_card_RS);
        myAppAdapter = new MyAppAdapter(relevantApartmentSearchersIds);
        flingContainer.setAdapter(myAppAdapter);
        setOnFlingListener();
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            /**
             * This function is responsible for handling item click
             * @param itemPosition - the item position in the container
             * @param dataObject
             */
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.fl_background_RS).setAlpha(0);
                myAppAdapter.notifyDataSetChanged();
                Bundle bundle = new Bundle();
                bundle.putString("apartmentId",
                        relevantApartmentSearchersIds.get(itemPosition));
                bioDialog.setArguments(bundle);
                bioDialog.show(getFragmentManager(),
                        "bio"); // //roommate's bio
            }
        });
    }

    /**
     * Setting fling listener
     */
    private void setOnFlingListener() {
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            /**
             * remove first object from the adapter
             */
            @Override
            public void removeFirstObjectInAdapter() {

            }

            /**
             * When swiping card to the left
             * @param dataObject
             */
            @Override
            public void onLeftCardExit(Object dataObject) {
                handlingLeftCardExit();
            }

            /**
             * When swiping card to the right
             * @param dataObject
             */
            @Override
            public void onRightCardExit(Object dataObject) {
                handlingRightCardExit();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            /**
             * This function responsible for animation when scrolling
             * @param scrollProgressPercent
             */
            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.fl_background_RS).setAlpha(0);
                view.findViewById(R.id.item_swipe_right_indicator_RS)
                        .setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator_RS)
                        .setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });
    }

    /**
     * Handling swiping card to the left
     */
    private void handlingLeftCardExit() {
        pressedNoToRoommate();
        myAppAdapter.notifyDataSetChanged();
        relevantApartmentSearchersIds.remove(0);
//        temp_img.remove(0);
        if (MyPreferences.isFirstUnlike(getContext())) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Regret deleting this?");
            dialog.setMessage("You can always go to the trash icon above to undo");

            dialog.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog deleteDialog = dialog.create();
            deleteDialog.show(); //showing
            // information about swiping left(unlike roommate)

            MyPreferences.setIsFirstUnlikeToFalse(getContext());
        }
    }

    /**
     * Handling swiping card to the right
     */
    private void handlingRightCardExit() {
        pressedYesToRoommate();
        relevantApartmentSearchersIds.remove(0);
//        temp_img.remove(0);
        myAppAdapter.notifyDataSetChanged();
        if (MyPreferences.isFirstLike(getContext())) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("It's a match!");
            dialog.setMessage("go to matches tab to see more info");

            dialog.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            MyPreferences.setIsFirstLikeToFalse(getContext());
        }
    }
//    /**
//     * fill image array with relevant images according to apartment users
//     */
//    private void fillTempImgArray() {
//        temp_img = new ArrayList<>();
//        for (String uid : relevantApartmentSearchersIds) {
//            temp_img.add(UsersImageConnector.getImageByUid(uid));
//        }
//    }

//    /**
//     * add to the relevantRelevantRoommateIds all the roommate ids that fits
//     * to the current apartment user
//     */
//    private void retrieveRelevantApartmentSearchers() {
//        relevantApartmentSearchersIds =
//                FirebaseMediate.getAptPrefList(ChoosingActivity.NOT_SEEN,
//                        MyPreferences.getUserUid(getContext()));
//        ArrayList<String> allMaybeUid = FirebaseMediate.getAptPrefList(ChoosingActivity.MAYBE_TO_HOUSE,
//                MyPreferences.getUserUid(getContext()));
//        // the relevant roommates are the ones that the user liked or chosen
//        // maybe
//        relevantApartmentSearchersIds.addAll(allMaybeUid);
//    }

    private void retrieveRelevantApartmentSearchers() {
        relevantApartmentSearchersIds = new ArrayList<>
                (MainActivityRoommateSearcher.getSpecificList(ChoosingActivity.NOT_SEEN));
    }

    /**
     * set onClickListeners to buttons/imageViews
     */
    private void setClickListeners() {
        trashCanImage.setOnClickListener(new View.OnClickListener() {

            /**
             * This function is responsible for displaying all the apartments
             * that the user unlike
             * @param view - The view
             */
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), TrashRoommateSearcherActivity.class);
                startActivity(i);
            }
        });
    }

    /**
     * get the roommate user id
     *
     * @return roommate user id
     */
    private String getUserUid() { //todo: check that is good for roommate ids
        return MyPreferences.getUserUid(getContext());
    }


//    /**
//     * refresh the list of roommates
//     */
//    private void refreshList() {
//        moreRoommates();
//    }

    /**
     * remove the apartment user from the current roommate user "haveNotSeen"
     * list
     *
     * @param apartmentUid - apartment id
     */
    private void removeFromHaveNotSeen(String apartmentUid) {
        MainActivityRoommateSearcher.removeValueFromList(ChoosingActivity.NOT_SEEN, apartmentUid);
//        FirebaseMediate.removeFromAptPrefList(ChoosingActivity.NOT_SEEN,
//                getUserUid(), roommateUid);
    }

    /**
     * called when user liked a roommate , without params
     * adds the roommate to the liked list
     */
    public void pressedYesToRoommate() { //todo: check that works
        String likedApartmentUserId =
                relevantApartmentSearchersIds.get(0); // the current roommate
        String myUid = getUserUid();
        removeFromHaveNotSeen(likedApartmentUserId);
        MainActivityRoommateSearcher.addValueToList(ChoosingActivity.MATCH, likedApartmentUserId);
        FirebaseMediate.addRoommateIdsToAptPrefList(ChoosingActivity.MATCH, likedApartmentUserId, myUid);

    }


    /**
     * called when user didn't like a roommate , without params
     * adds the apartment searcher user to the unliked list
     */
    public void pressedNoToRoommate() {
        String unlikedApartmentUserId =
                relevantApartmentSearchersIds.get(0);
        removeFromHaveNotSeen(unlikedApartmentUserId);
        MainActivityRoommateSearcher.addValueToList(ChoosingActivity.NO_TO_ROOMMATE, unlikedApartmentUserId);
    }

//    /**
//     * Called when there are more houses to display
//     */
//    private void moreRoommates() {
//        fillTempImgArray();
//    }

    //for swipe action

    /**
     * viewHolder class , adapter for roommate apartment image
     */
    public static class ViewHolder {
        public static FrameLayout background;
        public LinearLayout basicInfo;
        public ImageView cardImage;
    }

    /**
     * Adapter for relevant roommates
     */
    public class MyAppAdapter extends BaseAdapter {


        private ArrayList<String> parkingList;


        private MyAppAdapter(ArrayList<String> apps) {
            this.parkingList = apps;
        }

        /**
         * Setting the list
         *
         * @param parkingList - The list to set
         */
        public void setParkingList(ArrayList<String> parkingList) {
            this.parkingList = parkingList;
        }

        /**
         * Get the size of the list
         *
         * @return - The size of the list
         */
        @Override
        public int getCount() {
            return parkingList.size();
        }

        /**
         * Return the item according to position
         *
         * @param position - The position
         * @return - The item in that position
         */
        @Override
        public Object getItem(int position) {
            //We didn't implement this
            return position;
        }

        /**
         * Getting the image id
         *
         * @param position - position
         * @return - The id
         */
        @Override
        public long getItemId(int position) {
            //We didn't implement this
            return position;
        }

        /**
         * Getting the relevant view
         *
         * @param position    - position
         * @param convertView - view
         * @param parent      - parent
         * @return - The view
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;


            if (rowView == null) {

                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.card_item_rs, parent, false);
                // configure view holder
                viewHolder = new RoommateSearcherHome.ViewHolder();
                viewHolder.basicInfo =
                        (LinearLayout) rowView.findViewById(R.id.ll_basic_info_RS);
                //creating view holder for every roommate
                currentApartmentSearcher =
                        FirebaseMediate.getApartmentSearcherUserByUid(relevantApartmentSearchersIds.get(position));
                viewHolder.background =
                        (FrameLayout) rowView.findViewById(R.id.fl_background_RS);
                nameTxt = rowView.findViewById(R.id.tv_name);
                ageTxt = rowView.findViewById(R.id.tv_age);
                String fullName = currentApartmentSearcher.getFirstName()+ " " + currentApartmentSearcher.getLastName();
                nameTxt.setText(fullName);
                ageTxt.setText(Integer.toString(currentApartmentSearcher.getAge()));
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.iv_card_RS);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (RoommateSearcherHome.ViewHolder) convertView.getTag();
            }
            Glide.with(getContext()).load(UsersImageConnector.getImageByUid(relevantApartmentSearchersIds.get(position), UsersImageConnector.APARTMENT_USER)).into(viewHolder.cardImage);
            //todo: change pic to ari , look at apartmentSearcherHome.java

            return rowView;
        }
    }
}
