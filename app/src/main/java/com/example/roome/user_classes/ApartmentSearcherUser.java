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


    public ApartmentSearcherUser(String firstName, String lastName, int age) {
        super(firstName, lastName);
    }

    public ApartmentSearcherUser() {
        super();
    }

    //------------------------------------------Getters---------------------------------------------

    public String getBio() {return bio;}

    public ArrayList<String> getOptionalNeighborhoods() {return optionalNeighborhoods;}

    public int getMinRent() {return minRent;}

    public int getMaxRent() {return maxRent;}

    public String getEarliestEntryDate() {return earliestEntryDate;}

    public String getLatestEntryDate() {return latestEntryDate;}

    public int getMinNumDesiredRoommates() {return minNumDesiredRoommates;}

    public int getMaxNumDesiredRoommates() {return maxNumDesiredRoommates;}


    //------------------------------------------Seters---------------------------------------------
    private void setOptionalNeighborhoods(ArrayList<String> optionalNeighborhoods) {
        this.optionalNeighborhoods = optionalNeighborhoods;
    }

    public void setBio(String bio) {this.bio = bio;}

    public void setMinRent(int minRent) {this.minRent = minRent;}

    public void setMaxRent(int maxRent) {this.maxRent = maxRent;}

    public void setEarliestEntryDate(String earliestEntryDate) {
        this.earliestEntryDate = earliestEntryDate;
    }

    public void setLatestEntryDate(String latestEntryDate) {
        this.latestEntryDate = latestEntryDate;
    }

    public void setMinNumDesiredRoommates(int minNumDesiredRoommates) {
        this.minNumDesiredRoommates = minNumDesiredRoommates;
    }

    public void setMaxNumDesiredRoommates(int maxNumDesiredRoommates) {
        this.maxNumDesiredRoommates = maxNumDesiredRoommates;
    }
}
