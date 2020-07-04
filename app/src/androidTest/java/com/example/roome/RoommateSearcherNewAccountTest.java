package com.example.roome;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.roome.general_activities.MainActivity;
import com.example.roome.testing.EspressoIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RoommateSearcherNewAccountTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());

    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void apDetailsTest() {
        getToAccountInitialization();

        chooseNeighborhood();
        chooseEntryDate();
        chooseNumRoomates();
        chooseRent();
        chooseAge();
        chooseGender();
        enterPhoneNumber();
        enterInfo();

        pressSave();            // here we want to see that some fields are still not filled
        pressOKonPopup();       // press ok on popup to get back to editing
        enterMyAge();           // last field to fill
        pressSave();
        deleteAccount();

    }

    private void deleteAccount() {
        // go to edit profile page
        onView(allOf(childAtPosition(childAtPosition(withId(R.id.tabs_roomate),
                0), 2), isDisplayed())).perform(click());
        // click delete account
        onView(withId(R.id.btn_delete_rs_user)).perform(scrollTo(), click());
        // make sure we are on login page by asserting that login button is displayed
        onView(withId(R.id.btn_sign_without_google)).check(matches(isDisplayed()));
    }

    private void pressOKonPopup() {
        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton4.perform(scrollTo(), click());
    }

    private void enterMyAge() {
        onView(withId(R.id.et_enter_age)).perform(scrollTo(),
                replaceText("25"), closeSoftKeyboard());
    }

    private void enterInfo() {
        onView(withId(R.id.et_apartment_info)).perform(scrollTo(),
                replaceText("c"), closeSoftKeyboard());
    }

    private void enterPhoneNumber() {
        onView(withId(R.id.et_phone_number)).perform(scrollTo(),
                replaceText("0556565888"), closeSoftKeyboard());
    }

    private void chooseGender() {
        ViewInteraction appCompatRadioButton2 = onView(
                allOf(withId(R.id.radio_btn_male), withText("Gentleman"),
                        childAtPosition(withId(R.id.radio_group_choose_gender), 2)));
        appCompatRadioButton2.perform(scrollTo(), click());
    }

    private void pressSave() {
        ViewInteraction appCompatImageView4 = onView(
                allOf(withId(R.id.btn_save_roommate_searcher_profile),
                        childAtPosition(
                                allOf(withId(R.id.cl_roommate_set_profile),
                                        childAtPosition(
                                                withId(R.id.viewpager_roomate),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatImageView4.perform(click());
    }

    private void chooseRent() {
        onView(withId(R.id.et_apartment_rent)).perform(scrollTo(),
                replaceText("2000"));

        onView(allOf(withId(R.id.et_apartment_rent), withText("2000"), isDisplayed()))
                .perform(closeSoftKeyboard());
    }

    private void chooseNumRoomates() {
        onView(allOf(withId(R.id.radio_btn_num_of_roommates_3), withText("3")))
                .perform(scrollTo(), click());
    }

    private void chooseEntryDate() {
        onView(allOf(withId(R.id.iv_choose_apartment_entry_date))).perform(scrollTo(), click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton2.perform(click());
    }

    private void chooseNeighborhood() {
        onView(withId(R.id.spinner_neighborhood)).perform(scrollTo(), click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(4);
        appCompatCheckedTextView.perform(click());
    }

    private void chooseAge() {
        ////////////////////////////////////// CHANGE MIN AGE ///////////////////../////////////////
        onView((withId(R.id.et_rs_min_age_val))).perform(scrollTo(), replaceText("20"));
        ////////////////////////////////////// CHANGE MAX AGE //////////////////////////////////////
        onView(withId(R.id.et_rs_max_age_val)).perform(scrollTo(), replaceText("30"));
    }

    private void getToAccountInitialization() {
        // ENTER FIRST NAME //
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.et_first_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("Joseph"), closeSoftKeyboard());
        // ENTER LAST NAME //
        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.et_last_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("Tribbiani"), closeSoftKeyboard());
        // CLICK SIGN IN BUTTON //
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
        // CLICK AND CHOOSE ROOMMATE SEARCHER //
        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.iv_choosing_button_roommate),
                        childAtPosition(
                                allOf(withId(R.id.cl_choosing_activity),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                6),
                        isDisplayed()));
        appCompatImageView.perform(click());
    }

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
