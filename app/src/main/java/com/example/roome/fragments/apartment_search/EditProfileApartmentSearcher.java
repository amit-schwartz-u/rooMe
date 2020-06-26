package com.example.roome.fragments.apartment_search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.roome.activities.ChoosingActivity;
import com.example.roome.utilities.FirebaseMediate;
import com.example.roome.activities.MainActivity;
import com.example.roome.utilities.MyPreferences;
import com.example.roome.R;
import com.example.roome.activities.SendEmailActivity;
import com.example.roome.models.ApartmentSearcherUser;
import com.example.roome.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;

/**
 * This class represents the profile fragment in the app. The Apartment Searcher user can edit his
 * personal info and add contact information. The main purpose of this page is that the Roommate
 * Searcher users will know more information about the people interested in their apartments.
 */
public class EditProfileApartmentSearcher extends Fragment {
    private static final int GALLERY_REQUEST_CODE = 1;

    /* input validation variables */
    private Boolean isUserFirstNameValid = false;
    private Boolean isUserLastNameValid = false;
    private Boolean isUserAgeValid = false;
    private Boolean isUserPhoneValid = false;
    private Boolean changedPhoto = false;

    /* profile user info */
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText ageEditText;
    private EditText phoneNumberEditText;
    private EditText bioEditText;
    private RadioButton maleRadioButton;

    /* Firebase instance variables */
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ApartmentSearcherUser asUser;

    /* profile pic variables */
    ImageView profilePic;
    ImageView addProfilePic;
    final long ONE_MEGABYTE = 1024 * 1024;
    private Uri selectedImage;

    ImageView phoneInfoButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseReference = firebaseDatabase.getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
//        firebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                asUser = FirebaseMediate.getApartmentSearcherUserByUid(MyPreferences.getUserUid(getContext()));
//                setInfo();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
//        asUser = new ApartmentSearcherUser();

        super.onCreate(savedInstanceState);
    }

    /**
     * Inflates the layout for this fragment
     * @param inflater fragment inflater
     * @param container fragment container
     * @param savedInstanceState the saved instance state
     * @return the view for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_edit_profile_apartment_searcher, container, false);
    }

    /**
     * this method calls all methods necessary for initializing the fragment with all necessary data.
     * Also allows to save updated data to the firebase.
     * @param savedInstanceState the saved instance state
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        addProfilePic = getView().findViewById(R.id.ib_add_photo);
        profilePic = getView().findViewById(R.id.iv_missingPhoto);
        addProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhotoOnClickAS();
            }
        });

        asUser =
                FirebaseMediate.getApartmentSearcherUserByUid(MyPreferences.getUserUid(getContext()));

        ImageView saveProfileButton = getView().findViewById(R.id.btn_save_profile_as);
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserInputValid()) {
                    //save user data to DB
                    if (changedPhoto) { //save image to DB only if it's a new one
                        FirebaseMediate.uploadPhotoToStorage(selectedImage, getActivity(), getContext(), "Apartment Searcher User", "Profile Pic");
                        changedPhoto = false;
                    }
                    asUser.setBio(bioEditText.getText().toString());
                    firebaseDatabaseReference.child("users").child("ApartmentSearcherUser").child(MyPreferences.getUserUid(getContext())).setValue(asUser);
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                } else {
                    StringBuilder wrongParams = returnWrongParams();

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("Oops!");
                    dialog.setMessage("we can see that you the following fields are invalid:\n\n"
                            +wrongParams
                            +"Please take a look again before saving");

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
        Button deleteUserAccount =
                getView().findViewById(R.id.btn_delete_as_user);
        deleteUserAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo pop up dialog - are you sure to delete (y/n)?
                String aptUid = MyPreferences.getUserUid(getContext());
                FirebaseMediate.deleteAptUserFromApp(aptUid);
                for (String roommateId :
                        FirebaseMediate.getAllRoommateSearcherKeys())
                {
                    FirebaseMediate.addAptIdToRmtPrefList(ChoosingActivity.DELETE_USERS,roommateId,aptUid);
                }
                MyPreferences.resetData(getContext());
                Intent i =  new Intent(getActivity(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });
        addRedStarToTextView(R.id.tv_age,"Age");
        addRedStarToTextView(R.id.tv_phoneNumber,"Phone number");
        validateUserInput();
        phoneInfoButton = getView().findViewById(R.id.iv_phone_info_btn);
        phoneInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage("Your number will be shown only after a match");

                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog deleteDialog = dialog.create();
                deleteDialog.show();
                }
        });
        setInfo();
        setContactUsButton();
        super.onActivityCreated(savedInstanceState);
    }

    private StringBuilder returnWrongParams() {
        StringBuilder wrongParams = new StringBuilder("");
        if (!isUserFirstNameValid)
        {
            wrongParams.append("* First name is Invalid *\n");
        }
        if (!isUserLastNameValid)
        {
            wrongParams.append("* Last name is Invalid *\n");
        }
        if (!isUserAgeValid)
        {
            wrongParams.append("* Age is Invalid *\n");
        }
        if (!isUserPhoneValid)
        {
            wrongParams.append("* Phone is Invalid *\n");
        }
        wrongParams.append("\n");
        return wrongParams;
    }

    /**
     * Sets a "Contact Us" button which opens an email-sending activity
     */
    private void setContactUsButton() {
        Button contactUsButton = getView().findViewById(R.id.contact_us_button);
        contactUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SendEmailActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Sets the user's profile info in the fragment from the firebase saved data.
     */
    private void setInfo() {
        firstNameEditText = getView().findViewById(R.id.et_enter_first_name);
        firstNameEditText.setText(asUser.getFirstName());

        lastNameEditText = getView().findViewById(R.id.et_enter_last_name);
        lastNameEditText.setText(asUser.getLastName());

        ageEditText = getView().findViewById(R.id.et_enter_age);
        if (asUser.getAge() >= User.MINIMUM_AGE) {
            ageEditText.setText(Integer.toString(asUser.getAge()));
            isUserAgeValid = true;
        }

        maleRadioButton = getView().findViewById(R.id.radio_btn_male);
        RadioButton femaleRadioButton = getView().findViewById(R.id.radio_btn_female);
        if (("MALE").equals(asUser.getGender())) {
            maleRadioButton.setChecked(true);
        } else {
            femaleRadioButton.setChecked(true);
        }

        phoneNumberEditText = getView().findViewById(R.id.et_phone_number);
        phoneNumberEditText.setText(asUser.getPhoneNumber());
        if (asUser.getPhoneNumber() != null && asUser.getPhoneNumber().length() == User.PHONE_NUMBER_LENGTH) {
            isUserPhoneValid = true;
        }

        bioEditText = getView().findViewById(R.id.et_bio);
        bioEditText.setText(asUser.getBio());

        if (asUser.getHasProfilePic()) {
            //upload user's profile image from storage
            storageReference.child("Images").child("Apartment Searcher User").
                    child(MyPreferences.getUserUid(getContext())).child("Profile Pic").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    profilePic.setImageBitmap(bmp);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d("EditProfileAS", "No Such file or Path found!!");
                }
            });
        }
        isUserFirstNameValid = true;
        isUserLastNameValid = true;
    }

    /**
     * validating relevant fields filled by the user
     */
    private void validateUserInput() {
        validateUserFirstName();
        validateUserLastName();
        validateAge();
        validateGender();
        validatePhoneNumber();
    }

    /**
     * Returns true if all of the user's input is valid
     */
    private boolean isUserInputValid() {
        return isUserFirstNameValid && isUserLastNameValid && isUserAgeValid && isUserPhoneValid;
    }

    /**
     * this method opens the gallery to choose image. Activates when user clicks on an upload photo
     * button.
     */
    public void uploadPhotoOnClickAS() {
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

    /**
     * updates the activity view after choosing an image from gallery
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == GALLERY_REQUEST_CODE) {
                //data.getData returns the content URI for the selected Image
                selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver()
                            , selectedImage);
                    profilePic.setImageBitmap(bitmap);
                    asUser.setHasProfilePic(true);
                    changedPhoto = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
    }

    /**
     * validate the entered name.
     */
    private void validateUserFirstName() {
        firstNameEditText = getView().findViewById(R.id.et_enter_first_name);
        firstNameEditText.setText(asUser.getFirstName());
        setIsUserFirstNameValidToTrueIfValidFirstName();
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
                    asUser.setFirstName(firstNameEditText.getText().toString());
                    isUserFirstNameValid = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * set isUserFirstNameValid to true if user's first name is valid
     */
    private void setIsUserFirstNameValidToTrueIfValidFirstName() {
        int inputLength = firstNameEditText.getText().toString().length();
        if (inputLength < User.NAME_MAXIMUM_LENGTH && inputLength > 0) {
            isUserFirstNameValid = true;
        }
    }

    /**
     * validate the entered name.
     */
    private void validateUserLastName() {
        lastNameEditText = getView().findViewById(R.id.et_enter_last_name);
        lastNameEditText.setText(asUser.getLastName());
        setIisUserLastNameValidIfValidLastName();
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
                    asUser.setLastName(lastNameEditText.getText().toString());
                    isUserLastNameValid = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * set isUserLastNameValid to true if user's last name valid
     */
    private void setIisUserLastNameValidIfValidLastName() {
        int inputLength = lastNameEditText.getText().toString().length();
        if (inputLength < User.NAME_MAXIMUM_LENGTH && inputLength > 0) {
            isUserLastNameValid = true;
        }
    }

    /**
     * validating the age entered. Age has to be between 6 and 120.
     */
    private void validateAge() {
        ageEditText = getView().findViewById(R.id.et_enter_age);
        if (asUser.getAge() >= User.MINIMUM_AGE) {
            ageEditText.setText(Integer.toString(asUser.getAge()));
        }
        setIsUserAgeValidToTrueIfValidAge();
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
                        asUser.setAge(Integer.parseInt(ageEditText.getText().toString()));
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
                        asUser.setAge(Integer.parseInt(ageEditText.getText().toString()));
                        isUserAgeValid = true;
                    }
                }
            }
        });
    }

    /**
     * set isUserAgeValid to true If user's age is valid
     */
    private void setIsUserAgeValidToTrueIfValidAge() {
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
        maleRadioButton = getView().findViewById(R.id.radio_btn_male);
        RadioButton femaleRadioButton = getView().findViewById(R.id.radio_btn_female);
        if (("MALE").equals(asUser.getGender())) {
            maleRadioButton.setChecked(true);
        } else {
            femaleRadioButton.setChecked(true);
        }
        maleRadioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                asUser.setGender("MALE");
                maleRadioButton.setChecked(true);
            }
        });
        femaleRadioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                asUser.setGender("FEMALE");
            }
        });
    }

    /**
     * validating the PhoneNumber entered.
     */
    private void validatePhoneNumber() {
        phoneNumberEditText = getView().findViewById(R.id.et_phone_number);
        phoneNumberEditText.setText(asUser.getPhoneNumber());
        setIsUserPhoneValidToTrueIfPhoneNumberValid();
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int inputLength = phoneNumberEditText.getText().toString().length();
                if (inputLength == User.PHONE_NUMBER_LENGTH) {
                    asUser.setPhoneNumber(phoneNumberEditText.getText().toString());
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
                    asUser.setPhoneNumber(phoneNumberEditText.getText().toString());
                    isUserPhoneValid = true;
                }
            }
        });
    }

    /**
     * set isUserPhoneValid to true if phone number is valid.
     */
    private void setIsUserPhoneValidToTrueIfPhoneNumberValid() {
        int inputLength = phoneNumberEditText.getText().toString().length();
        if (inputLength == User.PHONE_NUMBER_LENGTH) {
            isUserPhoneValid = true;
        }
    }

    /**
     * This method returns a SpannableStringBuilder with the text and red star.
     * @param text - the text to show in the text view.
     * @return SpannableStringBuilder.
     */
    @NonNull
    private SpannableStringBuilder setStarToLabel(String text) {
        String simple = text;
        String colored = "*";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(simple);
        int start = builder.length();
        builder.append(colored);
        int end = builder.length();
        builder.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    /**
     * This method adds a red star to a text view filed.
     * @param textView - the text view to add the red star to.
     * @param text - the text to show in the text view.
     */
    @NonNull
    private void addRedStarToTextView(int textView, String text) {
        TextView tv = getView().findViewById(textView);
        SpannableStringBuilder builder1 = setStarToLabel(text);
        tv.setText(builder1);
    }

}