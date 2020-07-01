package com.example.roome.Apartment_searcher_tabs_classes;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.roome.utils.FirebaseMediate;
import com.example.roome.apartment_searcher_activities.MainActivityApartmentSearcher;
import com.example.roome.general_activities.MyPreferences;
import com.example.roome.R;
import com.example.roome.user_classes.ApartmentSearcherUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * This class represents the filter fragment of the Apartment Searcher user.
 * It allows the user to choose his filters in order to show him apartments relevant to his
 * preferences, such as cost range, number of roommates, locations etc.
 */
public class EditFiltersApartmentSearcher extends DialogFragment {

    /* location preferences variables */
    private ImageView chooseLocations;
    private TextView chosenLocations;
    private String[] locations;
    private boolean[] checkedLocations;
    private ArrayList<Integer> userLocations = new ArrayList<>();

    //things i care about (checkbox) variables
    private int[] checkBoxesValues = new int[]{R.id.check_box_pets, R.id.check_box_kosher,
            R.id.check_box_ac, R.id.check_box_smoking};

    /* date variables */
    private ImageView displayDate;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    /* number of roommates radio button variables */
    private RadioButton twoRoommatesMax;
    private RadioButton threeRoommatesMax;
    private RadioButton fourRoommatesMax;

    /* cost range variables */
    private EditText minCostEditText;
    private EditText maxCostEditText;
    private boolean validCostRange = true;

    /* age range variables */
    private EditText minAgeEditText;
    private EditText maxAgeEditText;
    private boolean validAgeRange = true;

    /* Firebase instance variables */
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;
    private ApartmentSearcherUser asUser;

    /**
     * The method initializes the firebase variables
     * @param savedInstanceState the saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseReference = firebaseDatabase.getReference();

    }

    /**
     * Inflate the layout for this fragment
     * @param inflater the inflater
     * @param container the container
     * @param savedInstanceState the saved instance state
     * @return view object for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_filter_apartment_searcher_dialog, container, false);
    }

    /**
     * The method calls all necessary methods in charge of initializing the fragment with all
     * relevant data stored in the firebase and handling change in them made by the user. The
     * method also allows to save updated data to the firebase.
     * @param savedInstanceState the saved instance state
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //save filters button
        handleSaveButton();

        //cost range
        handleMinCost();
        handleMaxCost();

        //location selection
        handleChosenLocation();

        //entry date selection
        handleDate();

        //max num roommates selection
        handleMaxRoommates();

        //roommates' age selection
        handleMinAge();
        handleMaxAge();

        //Things i care about selection
        addOnClickToCheckBoxes();

        super.onActivityCreated(savedInstanceState);
        asUser = FirebaseMediate.getApartmentSearcherUserByUid(MyPreferences.getUserUid(getContext()));
        setFiltersValuesFromDataBase();
    }

    /**
     * The following method handles the event that the user clicks the save button.
     * It checks validity of the data and if its indeed valid updates relevant changed data
     * in firebase.
     */
    public void handleSaveButton(){
        ImageView saveButton = getView().findViewById(R.id.btn_save_filters_as);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validCostRange && validAgeRange) {
                    firebaseDatabaseReference.child("users").child("ApartmentSearcherUser").
                            child(MyPreferences.getUserUid(getContext())).setValue(asUser);
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                    setSavedFiltersToLists();
                    getDialog().dismiss();
                } else {
                    Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * The method handles the event of the user editing the min cost
     */
    public void handleMinCost(){
        minCostEditText = getView().findViewById(R.id.et_min_cost_val);
        minCostEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            /**
             * The method checks the validity of the entered min cost.
             * @param s editable
             */
            @Override
            public void afterTextChanged(Editable s) {
                int minInputLen = minCostEditText.getText().toString().length();
                int maxInputLen = maxCostEditText.getText().toString().length();
                if (minInputLen != 0 && maxInputLen != 0) {
                    int minCostInput = Integer.parseInt(minCostEditText.getText().toString());
                    if (minCostInput > Integer.parseInt(maxCostEditText.getText().toString())) {
                        minCostEditText.setError("Min is bigger than max!");
                        validCostRange = false;
                    } else {
                        validCostRange = true;
                        asUser.setMinRent(minCostInput);
                    }
                }

            }
        });
    }

    /**
     * The method handles the event of the user editing the max cost
     */
    public void handleMaxCost(){
        maxCostEditText = getView().findViewById(R.id.et_max_cost_val);
        maxCostEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            /**
             * Checks validity of the max cost chosen
             * @param s charSequence
             * @param start int
             * @param before int
             * @param count int
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int minInputLen = minCostEditText.getText().toString().length();
                int maxInputLen = maxCostEditText.getText().toString().length();
                if (minInputLen != 0 && maxInputLen != 0) {
                    int maxCostInput = Integer.parseInt(maxCostEditText.getText().toString());
                    if (maxCostInput >= Integer.parseInt(minCostEditText.getText().toString())) {
                        validCostRange = true;
                        asUser.setMaxRent(maxCostInput);
                    } else {
                        validCostRange = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        maxCostEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            /**
             * The method checks for validity of the data (e.g max isn't smaller than min)
             * @param v the view
             * @param hasFocus boolean if there's a current focus on he object
             */
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int minInputLen = minCostEditText.getText().toString().length();
                    int maxInputLen = maxCostEditText.getText().toString().length();
                    if (minInputLen != 0 && maxInputLen != 0) {
                        int maxCostInput = Integer.parseInt(maxCostEditText.getText().toString());
                        if (maxCostInput < Integer.parseInt(minCostEditText.getText().toString())) {
                            validCostRange = false;
                            maxCostEditText.setError("Max is smaller than min!");
                        } else {
                            validCostRange = true;
                            asUser.setMaxRent(maxCostInput);
                        }
                    }
                }
            }
        });
    }

    /**
     * handles the OK button in the date dialog
     * @param alertDialogBuilder AlertDialog.Builder
     */
    private void locationsOKButton(AlertDialog.Builder alertDialogBuilder){
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    /**
                     * handles the event when user selects OK button - updates the text
                     * as the chosen locations
                     * @param dialogInterface the dialog interface
                     * @param which int
                     */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String text = toStringUserLocations();
                        chosenLocations.setText(text);
                        asUser.setOptionalNeighborhoods(userLocations);
                    }
                });
    }

    /**
     * handles the Dismiss button in the date dialog
     * @param alertDialogBuilder AlertDialog.Builder
     */
    private void locationsDismissButton(AlertDialog.Builder alertDialogBuilder){
        alertDialogBuilder.setNegativeButton("Dismiss",
                new DialogInterface.OnClickListener() {
                    /**
                     * Handles the event the user clicks the Dismiss button
                     * @param dialogInterface the dialog interface
                     * @param i int
                     */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }

    /**
     * handles the Clear All button in the date dialog
     * @param alertDialogBuilder AlertDialog.Builder
     */
    private void locationsClearAllButton(AlertDialog.Builder alertDialogBuilder){
        alertDialogBuilder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            /**
             * Handles the event where the user clicks the Clear All button
             * @param dialogInterface the dialog interface
             * @param which int
             */
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                for (int i = 0; i < checkedLocations.length; i++) {
                    checkedLocations[i] = false;
                    userLocations.clear();
                    chosenLocations.setText("");
                }
            }
        });
    }

    /**
     * The method handles the location view in the event the user picks/unpicks locations
     */
    public void handleChosenLocation(){
        chooseLocations = getView().findViewById(R.id.btn_choose_locations);
        chosenLocations = getView().findViewById(R.id.tv_picked_locations);
        locations = getResources().getStringArray(R.array.locations);
        checkedLocations = new boolean[locations.length];

        chooseLocations.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the event where the user clicks on the pick location option
             * @param view the view
             */
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Locations in Jerusalem");
                setCheckedLocationsToTrue();
                alertDialogBuilder.setMultiChoiceItems(locations, checkedLocations,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            /**
                             * Updates list of locations user picked
                             * @param dialogInterface the dialog interface
                             * @param position the position
                             * @param isChecked boolean if is checked
                             */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position,
                                        boolean isChecked) {
                        if (isChecked) {
                            if (!userLocations.contains(position)) {
                                userLocations.add(position);
                            }
                        } else {
                            for (int i = 0; i < userLocations.size(); i++) {
                                if (userLocations.get(i) == position) {
                                    userLocations.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                });
                alertDialogBuilder.setCancelable(false);
                locationsOKButton(alertDialogBuilder);
                locationsDismissButton(alertDialogBuilder);
                locationsClearAllButton(alertDialogBuilder);
                AlertDialog mDialog = alertDialogBuilder.create();
                mDialog.show();
            }
        });
    }

    /**
     * Handles the event where the user chooses an entry date
     */
    public void handleDate(){
        displayDate = getView().findViewById(R.id.iv_choose_entry_date);
        displayDate.setOnClickListener(new View.OnClickListener() {
            /**
             * Gets the date chosen from the user
             * @param view the view
             */
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                asUser.setEarliestEntryDate(dialog.toString());
            }
        });
        final TextView chosenDate = getView().findViewById(R.id.tv_click_here_entry_date);
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            /**
             * Sets the chosen date as text
             * @param datePicker the date picker
             * @param year the year chosen
             * @param month the month chosen
             * @param day the day chosen
             */
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;

                chosenDate.setText(date);
                asUser.setEarliestEntryDate(date);

            }
        };
    }

    /**
     * The method handles the event where the user picks the max number of roommates in an apartment
     */
    public void handleMaxRoommates(){
        twoRoommatesMax = getView().findViewById(R.id.radio_btn_as_2);
        twoRoommatesMax.setOnClickListener(new View.OnClickListener() {
            /**
             * sets the picked number to 2
             * @param v the view
             */
            @Override
            public void onClick(View v) {
                asUser.setMaxNumDesiredRoommates(2);
                twoRoommatesMax.setChecked(true);
            }
        });

        threeRoommatesMax = getView().findViewById(R.id.radio_btn_as_3);
        threeRoommatesMax.setOnClickListener(new View.OnClickListener() {
            /**
             * sets the picked number to 3
             * @param v the view
             */
            @Override
            public void onClick(View v) {
                asUser.setMaxNumDesiredRoommates(3);
                threeRoommatesMax.setChecked(true);
            }
        });

        fourRoommatesMax = getView().findViewById(R.id.radio_btn_as_4);
        fourRoommatesMax.setOnClickListener(new View.OnClickListener() {
            /**
             * sets the picked number to 4
             * @param v the view
             */
            @Override
            public void onClick(View v) {
                asUser.setMaxNumDesiredRoommates(4);
                fourRoommatesMax.setChecked(true);
            }
        });
    }

    /**
     * Handles the event where the user picks a min Age (checks validity, sets values)
     */
    public void handleMinAge(){
        minAgeEditText = getView().findViewById(R.id.et_min_age_val);
        minAgeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            /**
             * Checks the min age selected is valid
             * @param s editable
             */
            @Override
            public void afterTextChanged(Editable s) {
                int minInputLen = minAgeEditText.getText().toString().length();
                int maxInputLen = maxAgeEditText.getText().toString().length();
                if (minInputLen != 0 && maxInputLen != 0) {
                    int minAgeInput = Integer.parseInt(minAgeEditText.getText().toString());
                    if (minAgeInput > Integer.parseInt(maxAgeEditText.getText().toString())) {
                        minAgeEditText.setError("Min is bigger than max!");
                        validAgeRange = false;
                    } else {
                        validAgeRange = true;
                        asUser.setMinAgeRequired(minAgeInput);
                    }
                }
            }
        });
    }

    /**
     * Handles the event where the user chooses a mx age (checks validity and sets value)
     */
    public void handleMaxAge(){
        maxAgeEditText = getView().findViewById(R.id.et_max_age_val);
        maxAgeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            /**
             * Checks validity of the chosen max value
             * @param s charsequence
             * @param start int
             * @param before int
             * @param count int
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int minInputLen = minAgeEditText.getText().toString().length();
                int maxInputLen = maxAgeEditText.getText().toString().length();
                if (minInputLen != 0 && maxInputLen != 0) {
                    int maxAgeInput = Integer.parseInt(maxAgeEditText.getText().toString());
                    if (maxAgeInput >= Integer.parseInt(minAgeEditText.getText().toString())) {
                        validAgeRange = true;
                        asUser.setMaxAgeRequired(maxAgeInput);
                    } else {
                        validAgeRange = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        maxAgeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            /**
             * Chceks validity of user input (max isn't smaller then min)
             * @param v the view
             * @param hasFocus boolean if is on focus
             */
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int minInputLen = minAgeEditText.getText().toString().length();
                    int maxInputLen = maxAgeEditText.getText().toString().length();
                    if (minInputLen != 0 && maxInputLen != 0) {
                        int maxAgeInput = Integer.parseInt(maxAgeEditText.getText().toString());
                        if (maxAgeInput < Integer.parseInt(minAgeEditText.getText().toString())) {
                            validAgeRange = false;
                            maxAgeEditText.setError("Max is smaller than min!");
                        } else {
                            validAgeRange = true;
                            asUser.setMaxAgeRequired(maxAgeInput);

                        }
                    }
                }
            }
        });
    }



    /**
     * Sets the checked locations (location user chose) to true in the checkedLocations list
     */
    private void setCheckedLocationsToTrue() {
        if (asUser.getOptionalNeighborhoods() != null) {
            for (int index : asUser.getOptionalNeighborhoods()) {
                checkedLocations[index] = true;
            }
        }
    }

    /**
     * This method returns a string from the user location list.
     */
    private String toStringUserLocations() {
        String text = "";
        for (int i = 0; i < userLocations.size(); i++) {
            text = text + locations[userLocations.get(i)];
            if (i != userLocations.size() - 1) {
                text = text + ", ";
            }
        }
        return text;
    }

    /**
     * This method calls the method to filters out the the irrelevant roommate users from with
     * a specified list.
     */
    private void setSavedFiltersToLists() {
        MainActivityApartmentSearcher.filterOutRoommatesFromList(asUser);
    }

    /**
     * This method returns the relevant Users list from Firebase data base.
     * @param listName - the name of the list in firebase
     * @return the relevant Users list from Firebase data base.
     */
    private ArrayList<String> getAptPrefListFromFirebase(String listName) {
        return FirebaseMediate.getAptPrefList(listName, MyPreferences.getUserUid(getContext()));
    }

    /**
     * This method sets the relevant list in Firebase.
     * @param listName - the name of the list in firebase
     * @param updatedIdsList - the updated list to set to.
     */
    private void setUsersListInFirebase(String listName, ArrayList<String> updatedIdsList) {
        FirebaseMediate.setAptPrefList(listName, MyPreferences.getUserUid(getContext()), updatedIdsList);
    }

    /**
     * Sets the max number of roommates radio button to the value chosen by the user
     */
    private void setMaxNumRoommatesRB() {
        int chosenNum = asUser.getMaxNumDesiredRoommates();
        switch (chosenNum) {
            case 2:
                twoRoommatesMax.setChecked(true);
                break;
            case 3:
                threeRoommatesMax.setChecked(true);
                break;
            case 4:
                fourRoommatesMax.setChecked(true);
                break;
        }
    }

    /**
     * Sets the user's chosen rent range values from the firebase to the edit text
     */
    private void setCostRangeValsFB() {
        int min = asUser.getMinRent();
        int max = asUser.getMaxRent();
        if (max != 0) {
            maxCostEditText.setText(Integer.toString(max));
        }
        minCostEditText.setText(Integer.toString(min));
    }

    /**
     * Sets the user's chosen age range values from firebase to the edit text
     */
    private void setAgeRangeValsFB() {
        int min = asUser.getMinAgeRequired();
        int max = asUser.getMaxAgeRequired();
        if (max != 0) {
            maxAgeEditText.setText(Integer.toString(max));
        }
        minAgeEditText.setText(Integer.toString(min));
    }

    /**
     * This method sets the filters to the users preferences values stored in database.
     */
    private void setFiltersValuesFromDataBase() {
        setCostRangeValsFB();
        setMaxNumRoommatesRB();
        setAgeRangeValsFB();
        setCheckBoxesToUserPreferences();
        final TextView chosenDate = getView().findViewById(R.id.tv_click_here_entry_date);
        chosenDate.setText(asUser.getEarliestEntryDate());
        if (asUser.getOptionalNeighborhoods() != null) {
            userLocations = asUser.getOptionalNeighborhoods();
        }
        String text = toStringUserLocations();
        chosenLocations.setText(text);
    }

    /**
     * This method sets the check boxes to user preferences
     */
    private void setCheckBoxesToUserPreferences() {
        for (int checkBoxValue : checkBoxesValues) {
            if (isCheckedForUser(checkBoxValue)) {
                CheckBox checkBox = getView().findViewById(checkBoxValue);
                checkBox.setChecked(true);
            }
        }
    }

    /**
     * Starts the filter dialog and sets its size (height, width)
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    /**
     * This method adds onClick listeners to the check boxes
     */
    private void addOnClickToCheckBoxes() {
        for (int checkbox : checkBoxesValues) {
            addOnClickListenerToCheckBox(checkbox);
        }
    }

    /**
     * This method adds a onClick listener
     * @param checkBoxValue - the int value of the check box to add the listener to.
     */
    private void addOnClickListenerToCheckBox(final int checkBoxValue) {
        final CheckBox checkBox = getView().findViewById(checkBoxValue);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    setAsUserFilterValue(true, checkBoxValue);
                } else {
                    setAsUserFilterValue(false, checkBoxValue);
                }
            }
        });
    }

    /**
     * This method sets a user field to true if check box is checked otherwise false
     * @param flag          - true if check box is checked otherwise false
     * @param checkBoxValue - the check box to change the value.
     */
    private void setAsUserFilterValue(boolean flag, int checkBoxValue) {
        switch (checkBoxValue) {
            case R.id.check_box_pets:
                asUser.setHasNoPets(flag);
                break;
            case R.id.check_box_kosher:
                asUser.setKosherImportance(flag);
                break;
            case R.id.check_box_smoking:
                asUser.setSmokingFree(flag);
                break;
            case R.id.check_box_ac:
                asUser.setHasAC(flag);
                break;
        }
    }

    /**
     * This method returns true if the filter is important for the user, otherwise false.
     * @param checkBoxValue - the int value of the check box.
     * @return true if the filter is important for the user (he checked the box before), otherwise false.
     */
    private boolean isCheckedForUser(int checkBoxValue) {
        switch (checkBoxValue) {
            case R.id.check_box_pets:
                return asUser.isHasNoPets();
            case R.id.check_box_kosher:
                return asUser.getKosherImportance();
            case R.id.check_box_smoking:
                return asUser.isSmokingFree();
            case R.id.check_box_ac:
                return asUser.isHasAC();
            default:
                return false;
        }
    }
}
