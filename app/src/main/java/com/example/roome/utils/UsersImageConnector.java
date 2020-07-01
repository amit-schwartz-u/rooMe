package com.example.roome.utils;

import com.example.roome.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class binds between roommateId to the house drawable ,
 * We didn't implement the import / upload images to the firebase.
 */
public final class UsersImageConnector {
    private static ArrayList<Integer> apartmentImages;
    private static ArrayList<Integer> usersImages;
    public static String ROOMMATE_USER = "roommate user";
    public static String APARTMENT_USER = "apartment user";

    /**
     * Initialize the houses/people images
     */
    public static void initDrawablesImg() {
        apartmentImages = new ArrayList<>(Arrays.asList(R.drawable.house1,
                R.drawable.house2,
                R.drawable.house3, R.drawable.house4,
                R.drawable.house5,
                R.drawable.house6, R.drawable.house7));
        usersImages = new ArrayList<>(Arrays.asList(R.drawable.person1,
                R.drawable.person2,
                R.drawable.person3, R.drawable.person4,
                R.drawable.person5,
                R.drawable.person6, R.drawable.person7));

    }


    /**
     * Get the house drawable according to Id string
     *
     * @param userUid - roommate Id
     * @return - The drawable
     */
    public static int getImageByUid(String userUid, String typeOfUser) {
        int indForUser = getUserIntRepresentation(userUid);
        if (typeOfUser.equals(UsersImageConnector.APARTMENT_USER)) {
            int ind = indForUser % usersImages.size();
            return usersImages.get(ind);
        }
        if (typeOfUser.equals(UsersImageConnector.ROOMMATE_USER)) {
            int ind = indForUser % apartmentImages.size();
            return apartmentImages.get(ind);
        }
        return -1;
    }

    /**
     * get the user id , return some int the represents the user
     *
     * @param userUid - id of user
     * @return int representing the user
     */
    private static int getUserIntRepresentation(String userUid) {
        char character = userUid.charAt(userUid.length() - 1);
        int ascii = (int) character;
        return ascii;
    }

}
