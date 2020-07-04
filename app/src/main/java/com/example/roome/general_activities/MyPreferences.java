package com.example.roome.general_activities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class is responsible for saving app state and date to preferences file.
 */
public class MyPreferences {
    static final String MY_PREFERENCES = "myPreferences";
    static final String IS_FIRST_TIME = "isFirstTime";
    static final String IS_ROOMMATE_SEARCHER = "isRoommateSearcher";
    static final String USER_UID = "userFirebaseUid";
    static final String MANUAL_FIRST_NAME = "FIRSTNAME";
    static final String MANUAL_LAST_NAME = "LASTNAME";
    private static final String IS_FIRST_LIKE = "FIRST_LIKE";
    private static final String IS_FIRST_UNLIKE = "FIRST_UNLIKE";


    public static void resetData(Context context)
    {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putBoolean(IS_FIRST_TIME, true);
        preferencesEditor.putBoolean(IS_FIRST_LIKE, true);
        preferencesEditor.putBoolean(IS_FIRST_UNLIKE, true);
        preferencesEditor.putString(USER_UID, null);
        preferencesEditor.apply();
    }
    /**
     * getter for the field isFirstTime in MyPreferences.
     *
     * @param context - app context
     * @return true if it's users first time using the app.
     */
    public static boolean isFirstTime(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        return reader.getBoolean(IS_FIRST_TIME, true);
    }

    /**
     * getter for user first name.
     *
     * @param context - app context.
     * @return user first name.
     */
    public static String getManualFirstName(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        return reader.getString(MANUAL_FIRST_NAME, "null");
    }

    /**
     * getter for user last name.
     *
     * @param context - app context
     * @return user last name.
     */
    public static String getManualLastName(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        return reader.getString(MANUAL_LAST_NAME, "null");
    }

    /**
     * This method saves the user last name in my preferences.
     *
     * @param context   - app context.
     * @param firstName - user first name.
     */
    static void setManualFirstName(Context context, String firstName) {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putString(MANUAL_FIRST_NAME, firstName);
        preferencesEditor.apply();
    }

    /**
     * This method saves the user last name in my preferences.
     *
     * @param context  - app context
     * @param lastName - user last name
     */
    static void setManualLastName(Context context, String lastName) {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putString(MANUAL_LAST_NAME, lastName);
        preferencesEditor.apply();
    }

    /**
     * getter for user firebase id.
     *
     * @param context - app context
     * @return user firebase id.
     */
    public static String getUserUid(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        return reader.getString(USER_UID, null);
    }

    /**
     * setter for user firebase id.
     *
     * @param context - app context
     * @param userUid - user firebase id.
     */
    static void setUserUid(Context context, String userUid) {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putString(USER_UID, userUid);
        preferencesEditor.apply();
    }

    /**
     * This method sets the field isFirstTime to false.
     *
     * @param context - app context
     */
    public static void setIsFirstTimeToFalse(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putBoolean(IS_FIRST_TIME, false);
        preferencesEditor.apply();
    }

    /**
     * This method sets isRoommateSearcher to false.
     *
     * @param context - app context
     */
    static void setIsRoommateSearcherToFalse(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putBoolean(IS_ROOMMATE_SEARCHER, false);
        preferencesEditor.apply();
    }

    /**
     * getter for isRoommateSearcher
     *
     * @param context - app context
     * @return true if roommate searcher, otherwise false.
     */
    static boolean isRoommateSearcher(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        return reader.getBoolean(IS_ROOMMATE_SEARCHER, true);
    }

    /**
     * getter for FIRST_LIKE.
     *
     * @param context - app context
     * @return true if first like. otherwise false.
     */
    public static boolean isFirstLike(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        return reader.getBoolean(IS_FIRST_LIKE, true);
    }

    /**
     * this method sets IS_FIRST_LIKE to false.
     *
     * @param context - app context
     */
    public static void setIsFirstLikeToFalse(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putBoolean(IS_FIRST_LIKE, false);
        preferencesEditor.apply();
    }

    /**
     * this method sets IS_FIRST_UNLIKE to false.
     *
     * @param context - app context
     */
    public static void setIsFirstUnlikeToFalse(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putBoolean(IS_FIRST_UNLIKE, false);
        preferencesEditor.apply();
    }

    public static void setLastNumberOfMatches(Context context, int size) {
        final SharedPreferences reader = getSharedPreferences(context);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putInt("matchesListSize", size);
        preferencesEditor.apply();
    }

    public static int getLastNumberOfMatches(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        return reader.getInt("matchesListSize", 0);
    }

    /**
     * getter for IS_FIRST_UNLIKE.
     *
     * @param context - app context
     * @return true if first unlike. otherwise false.
     */
    public static boolean isFirstUnlike(Context context) {
        final SharedPreferences reader = getSharedPreferences(context);
        return reader.getBoolean(IS_FIRST_UNLIKE, true);
    }

    /**
     * returns the app SharedPreferences.
     *
     * @param context - app context
     * @return the shared preference
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
    }
}
