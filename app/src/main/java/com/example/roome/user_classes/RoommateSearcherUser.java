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

    //------------------------------------------Getters---------------------------------------------

    public ArrayList<Image> getApartmentImages() {return apartmentImages;}

    public ArrayList<Image> getRoommatesImages() {return roommatesImages;}

    public String getNeighborhood() {return neighborhood;}

    public int getEntryDate() {return entryDate;}

    public String getAdditionalInfo() {return additionalInfo;}

    public int getNumberOfRoommates() {return numberOfRoommates;}

    public String getGenderPreference() {return genderPreference;}

    public double getRent() {return rent;}


    //------------------------------------------Setters---------------------------------------------
    public void setApartmentImages(ArrayList<Image> apartmentImages) {
        this.apartmentImages = apartmentImages;
    }

    public void setRoommatesImages(ArrayList<Image> roommatesImages) {
        this.roommatesImages = roommatesImages;
    }

    public void setNeighborhood(String neighborhood) {this.neighborhood = neighborhood;}

    public void setEntryDate(int entryDate) {this.entryDate = entryDate;}

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public void setNumberOfRoommates(int numberOfRoommates) {
        this.numberOfRoommates = numberOfRoommates;
    }

    public void setGenderPreference(String genderPreference) {
        this.genderPreference = genderPreference;
    }

    public void setRent(double rent) {this.rent = rent;}
    
}
