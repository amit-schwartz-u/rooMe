package com.example.roome.user_classes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.roome.utils.FirebaseMediate;
import com.example.roome.R;
import com.example.roome.utils.UsersImageConnector;

public class RoommateAdditionalInfo extends DialogFragment {
    private ImageView exitOption, personImg;
    private TextView additionalInfo, name, age;

    ApartmentSearcherUser apartmentSearcherUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        apartmentSearcherUser = FirebaseMediate.getApartmentSearcherUserByUid(bundle.getString("apartmentId")); // todo
        return inflater.inflate(R.layout.fragment_roommate_additional_info, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        exitOption = getView().findViewById(R.id.iv_btn_x);
        exitOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        additionalInfo = getView().findViewById(R.id.tv_additional_person_info);
        additionalInfo.setText(apartmentSearcherUser.getBio());
        name = getView().findViewById(R.id.tv_name_extra);
        String fullName = apartmentSearcherUser.getFirstName()+ " " + apartmentSearcherUser.getLastName();
        name.setText(fullName);
        age = getView().findViewById(R.id.tv_age_extra);
        age.setText(Integer.toString(apartmentSearcherUser.getAge()));
        personImg = getView().findViewById(R.id.iv_person_img);
        personImg.setImageResource(UsersImageConnector.getImageByUid(getArguments().getString(
                "apartmentId"),UsersImageConnector.APARTMENT_USER));
        super.onActivityCreated(savedInstanceState);
    }


    /**
     * Starts the filter dialog and sets its size (height, width)
     */
    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }
}
