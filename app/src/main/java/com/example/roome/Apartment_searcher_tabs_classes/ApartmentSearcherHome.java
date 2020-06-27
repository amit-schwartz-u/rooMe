package com.example.roome.Apartment_searcher_tabs_classes;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.roome.ChoosingActivity;
import com.example.roome.FirebaseMediate;
import com.example.roome.MainActivityApartmentSearcher;
import com.example.roome.MyPreferences;
import com.example.roome.R;
import com.example.roome.TrashApartmentSearcherActivity;
import com.example.roome.UsersImageConnector;
import com.example.roome.user_classes.ApartmentAdditionalInfo;
import com.example.roome.user_classes.RoommateSearcherUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.lorenzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;

/**
 * This fragment is the main fragment.
 * The relevant roommate's houses are displayed in this fragment.
 * The apartment searcher user choose to swipe left/right (unlike/like) the
 * apartments.
 * When the user clicks the apartment he can see more info about the apartment.
 * Also , from this fragment the user can click on the filters icon to change
 * the filters
 */
public class ApartmentSearcherHome extends Fragment {

    /* Firebase */
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;

    /* Views references */
    private ImageView trashCanImage;
    private TextView locationText;
    private TextView peopleText;
    private TextView priceText;
    private ImageView editFiltersImage;

    /* For swipe */
    public static MyAppAdapter myAppAdapter;
    public static ViewHolder viewHolder;
    private SwipeFlingAdapterView flingContainer;

    /* Other class members */
    private ArrayList<String> relevantRoommateSearchersIds;
    private ArrayList<Integer> temp_img;
    private EditFiltersApartmentSearcher editFiltersDialog;
    private ApartmentAdditionalInfo additionalInfoDialog;
    public RoommateSearcherUser currentRoommateSearcher;





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
        return inflater.inflate(R.layout.activity_apartment_searcher_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        trashCanImage = getView().findViewById(R.id.iv_trash_can);
        editFiltersImage = getView().findViewById(R.id.iv_edit_filters);
        editFiltersDialog = new EditFiltersApartmentSearcher();
        additionalInfoDialog = new ApartmentAdditionalInfo();
        setClickListeners();
//        setFirebaseListeners();  //todo include this line
        retrieveRelevantRoommateSearchers();
        swipeOnCreate();
        moreHouses();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * create swipe adapter
     */
    private void swipeOnCreate() {
        flingContainer = getView().findViewById(R.id.frame_card);
        myAppAdapter = new MyAppAdapter(relevantRoommateSearchersIds);
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
                view.findViewById(R.id.fl_background).setAlpha(0);
                myAppAdapter.notifyDataSetChanged();
                Bundle bundle = new Bundle();
                bundle.putString("roommateId",
                        relevantRoommateSearchersIds.get(itemPosition));
                additionalInfoDialog.setArguments(bundle);
                additionalInfoDialog.show(getFragmentManager(),
                        "additionalInfo"); // showing additional info about
                // the apartment
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
                view.findViewById(R.id.fl_background).setAlpha(0);
                view.findViewById(R.id.item_swipe_right_indicator)
                        .setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator)
                        .setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });
    }

    /**
     * Handling swiping card to the left
     */
    private void handlingLeftCardExit() {
        pressedNoToApartment();
        myAppAdapter.notifyDataSetChanged();
        temp_img.remove(0);
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
            deleteDialog.show();//showing
            // information about swiping left(unlike apartment)
            MyPreferences.setIsFirstUnlikeToFalse(getContext());
        }
    }

    /**
     * Handling swiping card to the right
     */
    private void handlingRightCardExit() {
        pressedYesToApartment();
        temp_img.remove(0);
        myAppAdapter.notifyDataSetChanged();
        if (MyPreferences.isFirstLike(getContext())) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Want to see more apartments like this one?");
            dialog.setMessage("edit your filters and profile!");

            dialog.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog deleteDialog = dialog.create();
            deleteDialog.show(); //showing
            // information about swiping right(like apartment)
            MyPreferences.setIsFirstLikeToFalse(getContext());
        }
    }
    /**
     * fill image array with relevant images according to roommate users
     */
    private void fillTempImgArray() {
        temp_img = new ArrayList<>();
        for (String uid : relevantRoommateSearchersIds) {
            temp_img.add(UsersImageConnector.getImageByUid(uid,
                    UsersImageConnector.ROOMMATE_USER));
        }
    }


    private void retrieveRelevantRoommateSearchers() {
//        relevantRoommateSearchersIds =new ArrayList<>
//                (MainActivityApartmentSearcher.getSpecificList(ChoosingActivity.NOT_SEEN));
        relevantRoommateSearchersIds =
                MainActivityApartmentSearcher.getSpecificList(ChoosingActivity.NOT_SEEN);
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
                Intent i = new Intent(getContext(), TrashApartmentSearcherActivity.class);
                startActivity(i);
            }
        });
        editFiltersImage.setOnClickListener(new View.OnClickListener() {
            /**
             * Opening the edit filters dialog
             * @param view - The view
             */
            @Override
            public void onClick(View view) {
                editFiltersDialog.show(getFragmentManager(), "EditFilters");
            }
        });
    }

    /**
     * get the apartment user id
     *
     * @return apartment user id
     */
    private String getUserUid() {
        return MyPreferences.getUserUid(getContext());
    }

    /**
     * check if the apartment user and roommate user have a match according to
     * filters
     *
     * @param aptUid      - apartment id
     * @param roommateUid - roommate id
     * @return true if there's a match , otherwise false
     */
    private boolean isMatch(String aptUid, String roommateUid) {
        // We didn't implement this
        return true;
    }

    /**
     * set fireBase listeners
     */
    private void setFirebaseListeners() {
        firebaseDatabaseReference.child("users").child("RoommateSearcherUser").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                onChildChanged(dataSnapshot, s);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String roommateKey = dataSnapshot.getKey();
                String aUserKey = getUserUid();
                String inWhichList =
                        FirebaseMediate.RoommateInApartmentSearcherPrefsList(aUserKey, roommateKey);
                if (inWhichList.equals(ChoosingActivity.NOT_IN_LISTS)) {
                    if (isMatch(aUserKey, roommateKey)) {
                        //if theres a match - add to the havent seen list of
                        // AptUser
                        FirebaseMediate.addRoommateIdsToAptPrefList(ChoosingActivity.NOT_SEEN, aUserKey, roommateKey);
                    }
                } else {
                    if (!isMatch(aUserKey, roommateKey)) {
                        //if there is no match -> remove roommatekey from apt prefs list
                        FirebaseMediate.removeFromAptPrefList(inWhichList,
                                aUserKey, roommateKey);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String roommateKey = dataSnapshot.getKey();
                String aUserKey = getUserUid();
                String inWhichList =
                        FirebaseMediate.RoommateInApartmentSearcherPrefsList(aUserKey, roommateKey);
                if (!inWhichList.equals(ChoosingActivity.NOT_IN_LISTS)) {
                    //remove from the list
                    FirebaseMediate.removeFromAptPrefList(inWhichList,
                            aUserKey, roommateKey);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); //todo maybe delete this
        firebaseDatabaseReference.child("preferences").child(
                "ApartmentSearcherUser").child(getUserUid()).child(ChoosingActivity.NOT_SEEN).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                };
                relevantRoommateSearchersIds = dataSnapshot.getValue(t);
                if (relevantRoommateSearchersIds == null) {
                    relevantRoommateSearchersIds = new ArrayList<>();
                }
                myAppAdapter.setParkingList(relevantRoommateSearchersIds);
                refreshList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//todo maybe delete this
    }

    /**
     * refresh the list of houses
     */
    private void refreshList() {
        moreHouses();
    }

    /**
     * remove the roommate user from the current apartment user "haveNotSeen"
     * list
     *
     * @param roommateUid - roommate id
     */
    private void removeFromHaveNotSeen(String roommateUid) {
        MainActivityApartmentSearcher.removeValueFromList(ChoosingActivity.NOT_SEEN,roommateUid);
//        FirebaseMediate.removeFromAptPrefList(ChoosingActivity.NOT_SEEN,
//                getUserUid(), roommateUid);
    }

    /**
     * called when user liked an apartment , without params
     * adds the roommate to the liked list
     */
    public void pressedYesToApartment() {
        String likedRoommateId =
                relevantRoommateSearchersIds.get(0); // the current roommate
        String myUid = getUserUid();
        removeFromHaveNotSeen(likedRoommateId);
//        FirebaseMediate.addRoommateIdsToAptPrefList(ChoosingActivity.YES_TO_HOUSE,
//                myUid, likedRoommateId);
        FirebaseMediate.addAptIdToRmtPrefList(ChoosingActivity.NOT_SEEN,
                likedRoommateId, myUid);
    }


    /**
     * called when user didn't like an apartment , without params
     * adds the roommate searcher user to the unliked list
     */
    public void pressedNoToApartment() {
        String unlikedRoommateId =
                relevantRoommateSearchersIds.get(0);
        removeFromHaveNotSeen(unlikedRoommateId);
        MainActivityApartmentSearcher.addValueToList(ChoosingActivity.NO_TO_HOUSE,unlikedRoommateId);
    }

    /**
     * Called when there are more houses to display
     */
    private void moreHouses() {
        fillTempImgArray();
    }

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
         * @param parkingList - The list to set
         */
        public void setParkingList(ArrayList<String> parkingList) {
            this.parkingList = parkingList;
        }

        /**
         * Get the size of the list
         * @return - The size of the list
         */
        @Override
        public int getCount() {
            return parkingList.size();
        }

        /**
         *  Return the item according to position
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
         * @param position - position
         * @param convertView - view
         * @param parent - parent
         * @return - The view
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;


            if (rowView == null) {

                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.card_item_apartment, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.basicInfo =
                        (LinearLayout) rowView.findViewById(R.id.ll_basic_info);
                //creating view holder for every roommate
                String roommateId = relevantRoommateSearchersIds.get(position);
                currentRoommateSearcher =
                        FirebaseMediate.getRoommateSearcherUserByUid(roommateId);
                peopleText = rowView.findViewById(R.id.tv_people);
                locationText = rowView.findViewById(R.id.tv_location);
                priceText = rowView.findViewById(R.id.tv_price);
                peopleText.setText(Integer.toString(currentRoommateSearcher.getApartment().getNumberOfRoommates()));
                locationText.setText(currentRoommateSearcher.getApartment().getNeighborhood());
                priceText.setText(Integer.toString((int) (currentRoommateSearcher.getApartment().getRent())));
                viewHolder.background =
                        (FrameLayout) rowView.findViewById(R.id.fl_background);
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.iv_card);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ApartmentSearcherHome.ViewHolder) convertView.getTag();
            }
//            Glide.with(getContext()).load(temp_img.get(position)).into(viewHolder.cardImage);
            Glide.with(getContext()).load(UsersImageConnector.getImageByUid(relevantRoommateSearchersIds.get(position),UsersImageConnector.ROOMMATE_USER)).into(viewHolder.cardImage);

            return rowView;
        }
    }
}

