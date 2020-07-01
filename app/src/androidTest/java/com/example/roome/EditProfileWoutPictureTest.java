package com.example.roome;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.example.roome.general_activities.MainActivity;
import com.example.roome.testing.EspressoIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * This test checks that a user (searching for a room) updates his/her profile successfully
 * (without picture upload).
 * Actions:
 *          - (Rule) start MainActivity
 *          - enter signInFirstName as a first name, signInLastName as last name
 *          - pick searching a room to join
 *          - go to edit profile page, update the profile with info as given below
 *          - press save button
 *          - go to matches page, then come back to edit profile page
 *          - check that all the previously entered info is displayed accordingly
 *              =   end   =
 */
@LargeTest
public class EditProfileWoutPictureTest {

    // Sign in with:
    private String signInFirstName      = "TestFirstName";
    private String signInLastName       = "TestLastName";

    // Update profile with:
    private String updateFirstName      = "Spongebob";
    private String updateLastName       = "Squarepants";
    private String updateAge            = "66";
    private String updateGender         = "Female";
    private String updatePhoneNumber    = "0541234567";
    private String updateBio            = "Used to live in a pineapple under the sea!";


    @Rule
    public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());

    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    /////////////////////////////////////////// Tests //////////////////////////////////////////////

    @Test
    public void editProfileWoutPicture() {
        enterFirstLastName();
        pressSignInWoutGoogle();
        pressAptSearcher();
        goToEditProfile();
        editProfileWoutPictureUpload();
        pressSaveProfile();
        goToMatchesPageAndBackToEditProfile();
        confirmUpdateSucceeded();
        deleteAccount();
    }

    private void deleteAccount() {
        onView(withId(R.id.btn_delete_as_user)).perform(scrollTo(), click());
        // make sure we are on login page by asserting that login button is displayed
        onView(withId(R.id.btn_sign_without_google)).check(matches(isDisplayed()));
    }

    /////////////////////////////////////// Functions //////////////////////////////////////////////

    private void enterFirstLastName() {
        ViewInteraction appCompatEditText1 = onView(
                allOf(withId(R.id.et_first_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatEditText1.perform(replaceText(signInFirstName), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.et_last_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText(signInLastName), closeSoftKeyboard());
    }

    private void pressSignInWoutGoogle() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btn_sign_without_google), withText("Create account and sign in"),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                6),
                        isDisplayed()));
        appCompatButton.perform(click());
    }
    private void pressAptSearcher() {
        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.iv_choosing_button_apt),
                        childAtPosition(
                                allOf(withId(R.id.cl_choosing_activity),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatImageView.perform(click());
    }
    private void goToEditProfile() {
        ViewInteraction tabView = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabs_apartment),
                                0),
                        2),
                        isDisplayed()));
        tabView.perform(click());
    }
    private void editProfileWoutPictureUpload() {
        //////////////////////////////////// update first name /////////////////////////////////////
        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.et_enter_first_name), withText(signInFirstName)));
        appCompatEditText5.perform(scrollTo(), replaceText(updateFirstName));

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.et_enter_first_name), withText(updateFirstName), isDisplayed()));
        appCompatEditText6.perform(closeSoftKeyboard());

        //////////////////////////////////// update last name //////////////////////////////////////
        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.et_enter_last_name), withText(signInLastName)));
        appCompatEditText7.perform(scrollTo(), replaceText(updateLastName));

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.et_enter_last_name), withText(updateLastName), isDisplayed()));
        appCompatEditText8.perform(closeSoftKeyboard());

        /////////////////////////////////////// update age /////////////////////////////////////////
        onView(withId(R.id.et_enter_age)).perform(scrollTo(), replaceText(updateAge),
                closeSoftKeyboard());

        ///////////////////////////////////// update gender ////////////////////////////////////////
        if (updateGender.equals("Male")) {
            ViewInteraction appCompatRadioButton1 = onView(
                    allOf(withId(R.id.radio_btn_male), withText("Gentleman")));
            appCompatRadioButton1.perform(scrollTo(), click());
        }
        else if (updateGender.equals("Female")) {
            ViewInteraction appCompatRadioButton1 = onView(
                    allOf(withId(R.id.radio_btn_female), withText("Lady")));
            appCompatRadioButton1.perform(scrollTo(), click());
        }

        /////////////////////////////////// update phone number ////////////////////////////////////
        onView(withId(R.id.et_phone_number)).perform(scrollTo(), replaceText(updatePhoneNumber),
                closeSoftKeyboard());

        //////////////////////////////////////// update bio ////////////////////////////////////////
        onView(withId(R.id.et_bio)).perform(scrollTo(), replaceText(updateBio), closeSoftKeyboard());

    }
    private void pressSaveProfile() {
        ViewInteraction appCompatImageView2 = onView(
                allOf(withId(R.id.btn_save_profile_as), isDisplayed()));
        appCompatImageView2.perform(click());
    }
    private void goToMatchesPageAndBackToEditProfile() {
        ViewInteraction tabView2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabs_apartment),
                                0),
                        1),
                        isDisplayed()));
        tabView2.perform(click());

        ViewInteraction tabView3 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabs_apartment),
                                0),
                        2),
                        isDisplayed()));
        tabView3.perform(click());
    }

    private void confirmUpdateSucceeded() {
        //////////////////////////////////// check first name /////////////////////////////////////
        onView(allOf(withId(R.id.et_enter_first_name), withText(updateFirstName)))
                // Assert et_enter_first_name is displayed with updateFirstName
                .check(matches(isDisplayed()));

        ///////////////////////////////////// check last name //////////////////////////////////////
        onView(allOf(withId(R.id.et_enter_last_name), withText(updateLastName)))
                // Assert et_enter_last_name is displayed with updateLastName
                .check(matches(isDisplayed()));

        /////////////////////////////////////// check age //////////////////////////////////////////
        onView(allOf(withId(R.id.et_enter_age), withText(updateAge)))
                // Assert et_enter_age is displayed with updateAge
                .check(matches(isDisplayed()));

        ////////////////////////////////////// check gender ////////////////////////////////////////
        if (updateGender.equals("Male")) {
            // Assert radio_btn_male is displayed and checked,
            // while radio_btn_female is displayed and unchecked.
            onView(allOf(withId(R.id.radio_btn_male), withText("Gentleman")))
                    .check(matches(isChecked()));

            onView(allOf(withId(R.id.radio_btn_female), withText("Lady")))
                    .check(matches(isNotChecked()));
        }
        else if (updateGender.equals("Female")) {
            // Assert radio_btn_female is displayed and checked,
            // while radio_btn_male is displayed and unchecked.
            onView(allOf(withId(R.id.radio_btn_female), withText("Lady")))
                    .check(matches(isChecked()));

            onView(allOf(withId(R.id.radio_btn_male), withText("Gentleman")))
                    .check(matches(isNotChecked()));
        }

        /////////////////////////////////// check phone number ////////////////////////////////////
        onView(allOf(withId(R.id.et_phone_number), withText(updatePhoneNumber)))
                // Assert et_phone_number is displayed with updatePhoneNumber
                .check(matches(isDisplayed()));

        //////////////////////////////////////// check bio ////////////////////////////////////////
        onView(allOf(withId(R.id.et_bio), withText(updateBio)))
                // Assert et_bio is displayed with updateBio
                .check(matches(isDisplayed()));

    }

    /**
     * Match check functionality added automatically by Espresso.
     */
    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
