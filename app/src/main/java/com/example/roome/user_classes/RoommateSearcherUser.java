package com.example.roome.user_classes;

import android.media.Image;

import java.util.ArrayList;

public class RoommateSearcherUser extends User {

    //--------------------profile info---------------------
    private ArrayList<Image> apartmentImages;
    private ArrayList<Image> roommatesImages;
    private String neighborhood;
    private int entryDate; //todo: make sure to represent it as MM/YYYY
    private String additionalInfo; //such as bio
    private int numberOfRoommates;

    //--------------------filters---------------------
    private String genderPreference;
    private double rent;

    public RoommateSearcherUser(String firstName, String lastName, String phone, int age) {
        super(firstName, lastName, phone, age);
    }
}
