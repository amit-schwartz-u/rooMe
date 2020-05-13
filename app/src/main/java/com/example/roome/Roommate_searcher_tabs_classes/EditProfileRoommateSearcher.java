package com.example.roome.Roommate_searcher_tabs_classes;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.roome.EditProfileAlertDialog;
import com.example.roome.FirebaseMediate;
import com.example.roome.MainActivityRoommateSearcher;
import com.example.roome.MyPreferences;
import com.example.roome.R;
import com.example.roome.user_classes.Apartment;
import com.example.roome.user_classes.RoommateSearcherUser;
import com.example.roome.user_classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText ageEditText;
    private EditText phoneNumberEditText;
    private EditText infoEditText;
    private RadioButton maleRadioButton;
    private Button addApartmentPhoto;
    private EditText rentEditText;

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
    private DatePickerDialog.OnDateSetListener dateSetListener;

    /* number of roommates radio button variables */
    private RadioButton twoRoommatesRB;
    private RadioButton threeRoommatesRB;
    private RadioButton fourRoommatesRB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roommateSearcherUser = new RoommateSearcherUser();
        userApartment = new Apartment(false, null, null, 2, 0);

        initializeFirebaseFields();
        initializeDateFieldVariablesToFalse();
        initializeFields();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ImageView saveProfileButton = getView().findViewById(R.id.btn_save_roommate_searcher_profile);
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserInputValid()) {
                    saveUserDataToDataBase();
                    Intent i = new Intent(EditProfileRoommateSearcher.this.getActivity(), MainActivityRoommateSearcher.class);
                    startActivity(i);
                } else {
                    Intent intent = new Intent(EditProfileRoommateSearcher.this.getActivity(), EditProfileAlertDialog.class);
                    startActivity(intent);
                }
            }
        });
        addApartmentPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadApartmentPhotoOnClick();
            }
        });
        validateUserInput();
        super.onActivityCreated(savedInstanceState);
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

    void initializeFirebaseFields() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseReference = firebaseDatabase.getReference();
        userFirebaseDatabaseReference = firebaseDatabaseReference.child("users").child("RoommateSearcherUser").child(MyPreferences.getUserUid(getContext()));
        addValueEventListenerToFirebaseUserReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void initializeFields() {
        View activityView = getView();
        homeNeighborhood = activityView.findViewById(R.id.spinner_neighborhood);
        displayDate = activityView.findViewById(R.id.iv_choose_apartment_entry_date);
        rentEditText = activityView.findViewById(R.id.et_apartment_rent);
        twoRoommatesRB = activityView.findViewById(R.id.radio_btn_num_of_roommates_2);
        threeRoommatesRB = activityView.findViewById(R.id.radio_btn_num_of_roommates_3);
        fourRoommatesRB = activityView.findViewById(R.id.radio_btn_num_of_roommates_4);
        firstNameEditText = activityView.findViewById(R.id.et_enter_first_name);
        lastNameEditText = activityView.findViewById(R.id.et_enter_last_name);
        ageEditText = activityView.findViewById(R.id.et_enter_age);
        maleRadioButton = activityView.findViewById(R.id.radio_btn_male);
        phoneNumberEditText = activityView.findViewById(R.id.et_phone_number);
        infoEditText = activityView.findViewById(R.id.et_apartment_info);
        addApartmentPhoto = activityView.findViewById(R.id.btn_add_photos);
    }

    void addValueEventListenerToFirebaseUserReference() {
        firebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
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
    }

    private void setFieldsToValuesStoredInFirebase() {
        rentEditText.setText(Double.toString(userApartment.getRent()));

        setNumRoommatesRB();

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
        handleNeighborhood();
        handleApartmentEntryDate();
        handleNumberOfRoommates();
        handleApartmentRent();
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
        return isUserFirstNameValid && isUserLastNameValid && isUserAgeValid && isUserPhoneValid && isRentValid;
    }

    public void uploadApartmentPhotoOnClick() {
        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    apartmentImage = data.getData();
                    hasApartmentPic = true;
                    FirebaseMediate.uploadPhotoToStorage(apartmentImage, EditProfileRoommateSearcher.this.getActivity(), getContext(), "Roommate Searcher User", "Apartment");
            }
    }

    /**
     * Handles the event where the user chooses a neighborhood
     */
    private void handleNeighborhood() {
        ArrayAdapter<String> neighborhoodAdapter = new ArrayAdapter<>(EditProfileRoommateSearcher.this.getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.locations));
        neighborhoodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        homeNeighborhood.setAdapter(neighborhoodAdapter);
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
                userApartment.setEntryDate(dialog.toString());
            }
        });
        final TextView chosenDate = getView().findViewById(R.id.tv_show_here_entry_date);
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
                userApartment.setEntryDate(date);

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
                    Double rent = Double.parseDouble(rentEditText.getText().toString());
                    if (rent <= Apartment.MAX_RENT && rent >= Apartment.MIN_RENT) {
                        userApartment.setRent(rent);
                        isRentValid = true;
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
                    return;
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

}