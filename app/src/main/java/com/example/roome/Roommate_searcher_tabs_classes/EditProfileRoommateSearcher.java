package com.example.roome.Roommate_searcher_tabs_classes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.roome.AccountDeleter;
import com.example.roome.R;
import com.example.roome.general_activities.MyPreferences;
import com.example.roome.roommate_searcher_activities.MainActivityRoommateSearcher;
import com.example.roome.user_classes.Apartment;
import com.example.roome.user_classes.RoommateSearcherUser;
import com.example.roome.user_classes.User;
import com.example.roome.utils.FirebaseMediate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;

/**
 *
 */
public class EditProfileRoommateSearcher extends Fragment {
    private static final int GALLERY_REQUEST_CODE = 1;
    private Boolean isUserFirstNameValid;
    private Boolean isUserLastNameValid;
    private Boolean isUserAgeValid;
    private Boolean isUserPhoneValid;
    private Boolean changedPhoto = false;
    private Boolean isRentValid;
    private Boolean isMinRoommateAgeValid;
    private Boolean isMaxRoommateAgeValid;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText ageEditText;
    private EditText phoneNumberEditText;
    private EditText infoEditText;
    private RadioButton maleRadioButton;
    private Button addApartmentPhoto;
    private EditText rentEditText;
    private EditText minAgeEditText;
    private EditText maxAgeEditText;
    //things i care about (checkbox) variables
    private int[] checkBoxesValues = new int[]{R.id.check_box_pets_rm, R.id.check_box_kosher_rm,
            R.id.check_box_ac_rm, R.id.check_box_smoking_rm};

    // Firebase instance variables
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;
    private DatabaseReference userFirebaseDatabaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private RoommateSearcherUser roommateSearcherUser;

    private Uri apartmentImage;
    private boolean hasApartmentPic;
    private Apartment userApartment;

    private Spinner homeNeighborhood;

    /* date variables */
    private ImageView displayDate;
    private TextView chosenDate;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    /* number of roommates radio button variables */
    private RadioButton twoRoommatesRB;
    private RadioButton threeRoommatesRB;
    private RadioButton fourRoommatesRB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebaseFields();
        initializeDateFieldVariablesToFalse();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initializeFields();

        ImageView saveProfileButton = getView().findViewById(R.id.btn_save_roommate_searcher_profile);
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserInputValid()) {
                    saveUserDataToDataBase();
                    Intent i = new Intent(EditProfileRoommateSearcher.this.getActivity(), MainActivityRoommateSearcher.class);
                    startActivity(i);
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("Oops!");
                    dialog.setMessage("we can see that you have some unfilled or invalid fields. please take a look again before saving");

                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    AlertDialog deleteDialog = dialog.create();
                    deleteDialog.show();
                }
            }
        });
        Button deleteUserAccount = getView().findViewById(R.id.btn_delete_rs_user);
        deleteUserAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
            }
        });
        addApartmentPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadApartmentPhotoOnClick();
            }
        });
        super.onActivityCreated(savedInstanceState);
        roommateSearcherUser = FirebaseMediate.getRoommateSearcherUserByUid(MyPreferences.getUserUid(getContext()));
        if (roommateSearcherUser != null){
            userApartment = roommateSearcherUser.getApartment();
        }
        else {
            userApartment = new Apartment(false, null, null, 2, 0);
        }
        setFieldsToValuesStoredInFirebase();
    }

    /**
     * Inflates the layout for this fragment
     *
     * @param inflater           fragment inflater
     * @param container          fragment container
     * @param savedInstanceState the saved instance state
     * @return the view for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_edit_profile_roommate_searcher, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setFieldsToValuesStoredInFirebase();
        validateUserInput();
    }

    void initializeFirebaseFields() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseReference = firebaseDatabase.getReference();
        userFirebaseDatabaseReference = firebaseDatabaseReference.child("users").child("RoommateSearcherUser").child(MyPreferences.getUserUid(getContext()));
        addValueEventListenerToFirebaseUserReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void initializeFields() {
        homeNeighborhood = getView().findViewById(R.id.spinner_neighborhood);
        displayDate = getView().findViewById(R.id.iv_choose_apartment_entry_date);
        chosenDate = getView().findViewById(R.id.tv_show_here_entry_date);
        rentEditText = getView().findViewById(R.id.et_apartment_rent);
        minAgeEditText = getView().findViewById(R.id.et_rs_min_age_val);
        maxAgeEditText = getView().findViewById(R.id.et_rs_max_age_val);
        twoRoommatesRB = getView().findViewById(R.id.radio_btn_num_of_roommates_2);
        threeRoommatesRB = getView().findViewById(R.id.radio_btn_num_of_roommates_3);
        fourRoommatesRB = getView().findViewById(R.id.radio_btn_num_of_roommates_4);
        addOnClickToCheckBoxes();
        firstNameEditText = getView().findViewById(R.id.et_enter_first_name);
        lastNameEditText = getView().findViewById(R.id.et_enter_last_name);
        ageEditText = getView().findViewById(R.id.et_enter_age);
        maleRadioButton = getView().findViewById(R.id.radio_btn_male);
        phoneNumberEditText = getView().findViewById(R.id.et_phone_number);
        infoEditText = getView().findViewById(R.id.et_apartment_info);
        addApartmentPhoto = getView().findViewById(R.id.btn_add_photos);
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
                    setApartmentCheckboxValues(true, checkBoxValue);
                } else {
                    setApartmentCheckboxValues(false, checkBoxValue);
                }
            }
        });
    }

    void addValueEventListenerToFirebaseUserReference() {
        userFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roommateSearcherUser = FirebaseMediate.getRoommateSearcherUserByUid(MyPreferences.getUserUid(getContext()));
                userApartment = roommateSearcherUser.getApartment();
                setFieldsToValuesStoredInFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void initializeDateFieldVariablesToFalse() {
        isUserFirstNameValid = false;
        isUserLastNameValid = false;
        isUserAgeValid = false;
        isUserPhoneValid = false;
        isRentValid = false;
        isMinRoommateAgeValid = false;
        isMaxRoommateAgeValid = false;
    }

    private void setFieldsToValuesStoredInFirebase() {
        setApartmentNeighborhood();
        chosenDate.setText(userApartment.getEntryDate());
        setNumRoommatesRB();
        rentEditText.setText(Double.toString(userApartment.getRent()));
        minAgeEditText.setText(Integer.toString(userApartment.getMinRoommatesAge()));
        maxAgeEditText.setText(Integer.toString(userApartment.getMaxRoommatesAge()));
        setCheckBoxesToUserPreferences();

        firstNameEditText.setText(roommateSearcherUser.getFirstName());

        lastNameEditText.setText(roommateSearcherUser.getLastName());

        if (roommateSearcherUser.getAge() >= User.MINIMUM_AGE) {
            ageEditText.setText(Integer.toString(roommateSearcherUser.getAge()));
            isUserAgeValid = true;
        }

        RadioButton femaleRadioButton = getView().findViewById(R.id.radio_btn_female);
        if (("MALE").equals(roommateSearcherUser.getGender())) {
            maleRadioButton.setChecked(true);
        } else {
            femaleRadioButton.setChecked(true);
        }

        phoneNumberEditText.setText(roommateSearcherUser.getPhoneNumber());
        if (roommateSearcherUser.getPhoneNumber() != null && roommateSearcherUser.getPhoneNumber().length() == User.PHONE_NUMBER_LENGTH) {
            isUserPhoneValid = true;
        }

        infoEditText.setText(roommateSearcherUser.getInfo());

        isUserFirstNameValid = true;
        isUserLastNameValid = true;
    }

    /**
     * This method save users input data to data base.
     */
    void saveUserDataToDataBase() {
        roommateSearcherUser.setInfo(infoEditText.getText().toString());
        userFirebaseDatabaseReference.setValue(roommateSearcherUser);
        userFirebaseDatabaseReference.child("apartment").setValue(userApartment);
    }

    /**
     * validating relevant fields filled by the user
     */
    private void validateUserInput() {
        handleApartmentEntryDate();
        handleNumberOfRoommates();
        handleApartmentRent();
        handleApartmentRoommatesAges();
        validateUserFirstName();
        validateUserLastName();
        validateAge();
        validateGender();
        validatePhoneNumber();
    }

    /**
     * Returns a boolean if all of the user's input is valid
     */
    private boolean isUserInputValid() {
        return isUserFirstNameValid && isUserLastNameValid && isUserAgeValid && isUserPhoneValid && isRentValid && isMinRoommateAgeValid && isMaxRoommateAgeValid;
    }

    public void uploadApartmentPhotoOnClick() {
//        //Create an Intent with action as ACTION_PICK
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        // Sets the type as image/*. This ensures only components of type image are selected
//        intent.setType("image/*");
//        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
//        String[] mimeTypes = {"image/jpeg", "image/png"};
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
//        // Launching the Intent
//        startActivityForResult(intent, GALLERY_REQUEST_CODE);

        CropImage.activity()
                .setCropMenuCropButtonTitle("finish cropping")
                .start(getContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    apartmentImage = result.getUri();
                    saveNewApartmentImage();
            }
    }

    private void saveNewApartmentImage() {
        hasApartmentPic = true;
        ImageView questionImage = getView().findViewById(R.id.image_preview);
        FirebaseMediate.uploadPhotoToStorage(apartmentImage, EditProfileRoommateSearcher.this.getActivity(), getContext(), "Roommate Searcher User", "Apartment");
        questionImage.setImageURI(apartmentImage);
    }

    /**
     * Handles the event where the user chooses a neighborhood
     */
    private void setApartmentNeighborhood() {
        final ArrayAdapter<String> neighborhoodAdapter = new ArrayAdapter<>(EditProfileRoommateSearcher.this.getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.locations));
        neighborhoodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        homeNeighborhood.setAdapter(neighborhoodAdapter);
        homeNeighborhood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                homeNeighborhood.setSelection(position);
                userApartment.setNeighborhood(neighborhoodAdapter.getItem(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        String neighborhood = userApartment.getNeighborhood();
        if (neighborhood != null) {
            int spinnerPosition = neighborhoodAdapter.getPosition(neighborhood);
            homeNeighborhood.setSelection(spinnerPosition);
        }
    }

    /**
     * Handles the event where the user chooses an entry date
     */
    public void handleApartmentEntryDate() {
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
                        EditProfileRoommateSearcher.this.getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
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
                Calendar calender = Calendar.getInstance();
                if(year < calender.get(Calendar.YEAR)){
                    Context context = getContext();
                    CharSequence text = "Forget about the past, look at the future!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                else {
                    String date = day + "/" + month + "/" + year;
                    chosenDate.setText(date);
                    userApartment.setEntryDate(date);
                }
            }
        };
    }

    /**
     * The method handles the event where the user picks the max number of roommates in an apartment
     */
    public void handleNumberOfRoommates() {
        twoRoommatesRB.setOnClickListener(new View.OnClickListener() {
            /**
             * sets the picked number to 2
             * @param v the view
             */
            @Override
            public void onClick(View v) {
                userApartment.setNumberOfRoommates(2);
                twoRoommatesRB.setChecked(true);
            }
        });

        threeRoommatesRB.setOnClickListener(new View.OnClickListener() {
            /**
             * sets the picked number to 3
             * @param v the view
             */
            @Override
            public void onClick(View v) {
                userApartment.setNumberOfRoommates(3);
                threeRoommatesRB.setChecked(true);
            }
        });

        fourRoommatesRB.setOnClickListener(new View.OnClickListener() {
            /**
             * sets the picked number to 4
             * @param v the view
             */
            @Override
            public void onClick(View v) {
                userApartment.setNumberOfRoommates(4);
                fourRoommatesRB.setChecked(true);
            }
        });
    }

    /**
     * This method handles the apartment rent.
     */
    private void handleApartmentRent() {
        rentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isRentValid = false;
                int inputLength = rentEditText.getText().toString().length();
                if (inputLength > 6) {
                    rentEditText.setError("Maximum Limit Reached!");
                    return;
                }
                if (inputLength != 0) {
                    String inputText = rentEditText.getText().toString();
                    if(inputText.equals("-")){
                        Context context = getContext();
                        CharSequence text = "You can't enter a negative number!\nGetting payed for renting an apartment?! Come on...";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        rentEditText.setText("");
                    }
                    else if(inputText.equals("+")){
                        rentEditText.setText("");
                    }
                    else {
                        Double rent = Double.parseDouble(inputText);
                        if (rent <= Apartment.MAX_RENT && rent >= Apartment.MIN_RENT) {
                            userApartment.setRent(rent);
                            isRentValid = true;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        rentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int inputLength = rentEditText.getText().toString().length();
                    if (inputLength == 0) {
                        rentEditText.setError("Rent is required!");
                        return;
                    }
                    if (inputLength > 6) {
                        rentEditText.setError("Maximum Limit Reached!");
                        return;
                    }
                    Double rent = Double.parseDouble(rentEditText.getText().toString());
                    if (rent > Apartment.MAX_RENT) {
                        rentEditText.setError("reached maximum rent price!");
                    } else if (rent < Apartment.MIN_RENT) {
                        rentEditText.setError("rent cant be negative");
                    } else {
                        userApartment.setRent(rent);
                        isRentValid = true;
                    }
                }
            }
        });
    }

    /**
     * This method handles the apartment roommates min - max age range.
     */
    private void handleApartmentRoommatesAges() {
        minAgeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isMinRoommateAgeValid = false;
                int inputLength = minAgeEditText.getText().toString().length();
                if (inputLength > 2) {
                    minAgeEditText.setError("Maximum Limit Reached!");
                    return;
                }
                if (inputLength != 0) {
                    int minAge = Integer.parseInt(minAgeEditText.getText().toString());
                    if (minAge < Apartment.MAX_ROOMMATE_AGE && minAge >= Apartment.MIN_ROOMMATE_AGE) {
                        userApartment.setMinRoommatesAge(minAge);
                        isMinRoommateAgeValid = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        minAgeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int inputLength = minAgeEditText.getText().toString().length();
                    if (inputLength == 0) {
                        minAgeEditText.setError("Min age is required!");
                        return;
                    }
                    if (inputLength > 2) {
                        minAgeEditText.setError("Maximum Limit Reached!");
                        return;
                    }
                    int minAge = Integer.parseInt(minAgeEditText.getText().toString());
                    if (minAge > Apartment.MAX_ROOMMATE_AGE) {
                        minAgeEditText.setError("reached maximum Age!");
                    } else if (minAge < Apartment.MIN_ROOMMATE_AGE) {
                        minAgeEditText.setError("min age cant be negative");
                    } else {
                        userApartment.setMinRoommatesAge(minAge);
                        isMinRoommateAgeValid = true;
                    }
                }
            }
        });

        maxAgeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isMaxRoommateAgeValid = false;
                int inputLength = maxAgeEditText.getText().toString().length();
                if (inputLength > 2) {
                    maxAgeEditText.setError("Maximum Limit Reached!");
                    return;
                }
                if (inputLength != 0) {
                    int maxAge = Integer.parseInt(maxAgeEditText.getText().toString());
                    if (maxAge < Apartment.MAX_ROOMMATE_AGE && maxAge >= Apartment.MIN_ROOMMATE_AGE) {
                        userApartment.setMaxRoommatesAge(maxAge);
                        isMaxRoommateAgeValid = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        maxAgeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int inputLength = maxAgeEditText.getText().toString().length();
                    if (inputLength == 0) {
                        maxAgeEditText.setError("Max age is required!");
                        return;
                    }
                    if (inputLength > 2) {
                        maxAgeEditText.setError("Maximum Limit Reached!");
                        return;
                    }
                    int maxAge = Integer.parseInt(maxAgeEditText.getText().toString());
                    if (maxAge > Apartment.MAX_ROOMMATE_AGE) {
                        maxAgeEditText.setError("reached maximum Age!");
                    } else if (maxAge < Apartment.MIN_ROOMMATE_AGE) {
                        maxAgeEditText.setError("max age cant be negative");
                    } else {
                        userApartment.setMaxRoommatesAge(maxAge);
                        isMaxRoommateAgeValid = true;
                    }
                }
            }
        });
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
     * This method returns true if the filter is important for the user, otherwise false.
     * @param checkBoxValue - the int value of the check box.
     * @return true if the filter is important for the user (he checked the box before), otherwise false.
     */
    private boolean isCheckedForUser(int checkBoxValue) {
        switch (checkBoxValue) {
            case R.id.check_box_pets_rm:
                return userApartment.isHasNoPets();
            case R.id.check_box_kosher_rm:
                return roommateSearcherUser.getKosherImportance();
            case R.id.check_box_smoking_rm:
                return userApartment.isSmokingFree();
            case R.id.check_box_ac_rm:
                return userApartment.isHasAC();
            default:
                return false;
        }
    }

    /**
     * validate the entered name.
     */
    private void validateUserFirstName() {
        firstNameEditText.setText(roommateSearcherUser.getFirstName());
        checkIfValidFirstName();
        firstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isUserFirstNameValid = false;
                int inputLength = firstNameEditText.getText().toString().length();
                if (inputLength >= User.NAME_MAXIMUM_LENGTH) {
                    firstNameEditText.setError("Maximum Limit Reached!");
                } else if (inputLength == 0) {
                    firstNameEditText.setError("First name is required!");
                } else {
                    roommateSearcherUser.setFirstName(firstNameEditText.getText().toString());
                    isUserFirstNameValid = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void checkIfValidFirstName() {
        int inputLength = firstNameEditText.getText().toString().length();
        if (inputLength < User.NAME_MAXIMUM_LENGTH && inputLength > 0) {
            isUserFirstNameValid = true;
        }
    }

    /**
     * validate the entered name.
     */
    private void validateUserLastName() {
        lastNameEditText.setText(roommateSearcherUser.getLastName());
        checkIfValidLastName();
        lastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isUserLastNameValid = false;
                int inputLength = lastNameEditText.getText().toString().length();
                if (inputLength >= User.NAME_MAXIMUM_LENGTH) {
                    lastNameEditText.setError("Maximum Limit Reached!");
                    return;
                } else if (inputLength == 0) {
                    lastNameEditText.setError("Last name is required!");
                } else {
                    roommateSearcherUser.setLastName(lastNameEditText.getText().toString());
                    isUserLastNameValid = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void checkIfValidLastName() {
        int inputLength = lastNameEditText.getText().toString().length();
        if (inputLength < User.NAME_MAXIMUM_LENGTH && inputLength > 0) {
            isUserLastNameValid = true;
        }
    }

    /**
     * validating the age entered. Age has to be between 6 and 120.
     */
    private void validateAge() {
        if (roommateSearcherUser.getAge() >= User.MINIMUM_AGE) {
            ageEditText.setText(Integer.toString(roommateSearcherUser.getAge()));
        }
        checkIfValidAge();
        ageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isUserAgeValid = false;
                int inputLength = ageEditText.getText().toString().length();
                if (inputLength > User.MAXIMUM_AGE_LENGTH) {
                    ageEditText.setError("Maximum Limit Reached!");
                    return;
                }
                if (inputLength != 0) {
                    int curAge = Integer.parseInt(ageEditText.getText().toString());
                    if (curAge <= User.MAXIMUM_AGE && curAge >= User.MINIMUM_AGE) {
                        roommateSearcherUser.setAge(Integer.parseInt(ageEditText.getText().toString()));
                        isUserAgeValid = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int inputLength = ageEditText.getText().toString().length();
                    if (inputLength == 0) {
                        ageEditText.setError("Age is required!");
                        return;
                    }
                    if (inputLength > User.MAXIMUM_AGE_LENGTH) {
                        ageEditText.setError("Maximum Limit Reached!");
                        return;
                    }
                    int curAge = Integer.parseInt(ageEditText.getText().toString());
                    if (curAge > User.MAXIMUM_AGE) {
                        ageEditText.setError("Age is too old!");
                    } else if (curAge < User.MINIMUM_AGE) {
                        ageEditText.setError("Age is too young!");
                    } else {
                        roommateSearcherUser.setAge(Integer.parseInt(ageEditText.getText().toString()));
                        isUserAgeValid = true;
                    }
                }
            }
        });
    }

    private void checkIfValidAge() {
        int inputLength = ageEditText.getText().toString().length();
        if (inputLength != 0) {
            int curAge = Integer.parseInt(ageEditText.getText().toString());
            if (curAge <= User.MAXIMUM_AGE && curAge >= User.MINIMUM_AGE) {
                isUserAgeValid = true;
            }
        }
    }

    /**
     * validating the Gender entered.
     */
    private void validateGender() {
        RadioButton femaleRadioButton = getView().findViewById(R.id.radio_btn_female);
        if (("MALE").equals(roommateSearcherUser.getGender())) {
            maleRadioButton.setChecked(true);
        } else {
            femaleRadioButton.setChecked(true);
        }
        maleRadioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                roommateSearcherUser.setGender("MALE");
                maleRadioButton.setChecked(true);
            }
        });
        femaleRadioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                roommateSearcherUser.setGender("FEMALE");
            }
        });
    }

    /**
     * validating the PhoneNumber entered.
     */
    private void validatePhoneNumber() {
        phoneNumberEditText.setText(roommateSearcherUser.getPhoneNumber());
        checkIfValidPhoneNumber();
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int inputLength = phoneNumberEditText.getText().toString().length();
                if (inputLength == User.PHONE_NUMBER_LENGTH) {
                    roommateSearcherUser.setPhoneNumber(phoneNumberEditText.getText().toString());
                    isUserPhoneValid = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        phoneNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int inputLength = phoneNumberEditText.getText().toString().length();
                    if (inputLength == 0) {
                        phoneNumberEditText.setError("Phone number is required!");
                        return;
                    }
                    if (inputLength != User.PHONE_NUMBER_LENGTH) {
                        phoneNumberEditText.setError("Invalid phone number");
                        return;
                    }
                    roommateSearcherUser.setPhoneNumber(phoneNumberEditText.getText().toString());
                    isUserPhoneValid = true;
                }
            }
        });

    }

    private void checkIfValidPhoneNumber() {
        int inputLength = phoneNumberEditText.getText().toString().length();
        if (inputLength == User.PHONE_NUMBER_LENGTH) {
            isUserPhoneValid = true;
        }
    }

    /**
     * Sets number of roommates radio button to the value from data base
     */
    private void setNumRoommatesRB() {
        int numberOfRoommatesInApartment = userApartment.getNumberOfRoommates();
        switch (numberOfRoommatesInApartment) {
            case 2:
                twoRoommatesRB.setChecked(true);
                break;
            case 3:
                threeRoommatesRB.setChecked(true);
                break;
            case 4:
                fourRoommatesRB.setChecked(true);
                break;
        }
    }

    /**
     * This method sets a user field to true if check box is checked otherwise false
     * @param flag          - true if check box is checked otherwise false
     * @param checkBoxValue - the check box to change the value.
     */
    private void setApartmentCheckboxValues(boolean flag, int checkBoxValue) {
        switch (checkBoxValue) {
            case R.id.check_box_pets_rm:
                userApartment.setHasNoPets(flag);
                break;
            case R.id.check_box_kosher_rm:
                roommateSearcherUser.setKosherImportance(flag);
                break;
            case R.id.check_box_smoking_rm:
                userApartment.setSmokingFree(flag);
                break;
            case R.id.check_box_ac_rm:
                userApartment.setHasAC(flag);
                break;
        }
    }

    public void showSignOutDialog() {
        AccountDeleter deleter = new AccountDeleter(getContext(),getActivity());
        deleter.showSignOutDialog();
    }
}