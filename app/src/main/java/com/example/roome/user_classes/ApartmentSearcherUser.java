package com.example.roome.user_classes;

import java.util.ArrayList;

public class ApartmentSearcherUser extends User {

    //--------------------profile info---------------------
    private String bio; //where the user can write about himself

    //---------------------------------------------filters------------------------------------------
    private ArrayList<String> optionalNeighborhoods;
    private int minRent;
    private int maxRent;
    private String earliestEntryDate;//todo: make sure to represent it as MM/YYYY
    private String latestEntryDate;
    private int minNumDesiredRoommates; //todo:decide if we want it to be exclusive or inclusive
    private int maxNumDesiredRoommates;


    public ApartmentSearcherUser(String firstName, String lastName, String phone, int age) {
        super(firstName, lastName, phone, age);
    }


    //------------------------------------------Seters---------------------------------------------
    private void setOptionalNeighborhoods(ArrayList<String> optionalNeighborhoods) {
        this.optionalNeighborhoods = optionalNeighborhoods;
    }
}
