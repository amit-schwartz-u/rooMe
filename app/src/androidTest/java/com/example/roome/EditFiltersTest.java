package com.example.roome;


import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

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
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class EditFiltersTest {
    public static final String  signin_first_name       = "TestFirstName";
    public static final String  signin_last_name        = "TestLastName";

    public static final int     filters_location        = 0;    // checkbox 0 = Rehavia
    public static final String  filters_min_age         = "20";
    public static final String  filters_max_age         = "30";
    public static final String  filters_num_roommates   = "3";

    @Rule
    public ActivityTestRule<MainActivity> mActTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());

    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void EFTest() throws InterruptedException {
        signInAndGoToApartmentSearcherPage();
        // enter edit filters and choose filters
        onView(withId(R.id.iv_edit_filters)).perform(click());          // click edit filters
        editFilters();
        onView(withId(R.id.btn_save_filters_as)).perform(click());      // click save filters

        // enter edit filters and assert that the information entered was saved and thus displayed
        onView(withId(R.id.iv_edit_filters)).perform(click());          // click edit filters
        compareInfoToPreviouslyEntered();
        onView(withId(R.id.btn_save_filters_as)).perform(click());      // click save filters
        deleteAccount();
    }

    private void deleteAccount() {
        // go to edit profile page
        onView(allOf(childAtPosition(childAtPosition(withId(R.id.tabs_apartment),
                0), 2), isDisplayed())).perform(click());
        // click delete account
        onView(withId(R.id.btn_delete_as_user)).perform(scrollTo(), click());
        // make sure we are on login page by asserting that login button is displayed
        onView(withId(R.id.btn_sign_without_google)).check(matches(isDisplayed()));
    }

    private void compareInfoToPreviouslyEntered() {
        checkPriceRange();
        checkLocation();
        checkEntryDate();
        checkNumRoommates();
        checkAgeRange();
        checkAdditionalFilters();
    }

    private void editFilters() {
        enterPriceRange();
        chooseLocation();
        chooseEntryDate();
        chooseNumRoommates();
        enterAgeRange();
        chooseAdditionalFilters();
    }

    private void enterPriceRange() {
        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.et_min_cost_val),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                1)));
        appCompatEditText3.perform(scrollTo(), replaceText("1000"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.et_min_cost_val),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.et_max_cost_val),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                2)));
        appCompatEditText7.perform(scrollTo(), replaceText("5000"));
    }

    private void chooseLocation() {
        ViewInteraction appCompatImageView4 = onView(
                allOf(withId(R.id.btn_choose_locations),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                5)));
        appCompatImageView4.perform(scrollTo(), click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                        childAtPosition(
                                withId(R.id.contentPanel),
                                0)))
                .atPosition(0);
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());
    }

    private void chooseEntryDate() {
        ViewInteraction appCompatImageView5 = onView(
                allOf(withId(R.id.iv_choose_entry_date),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                8)));
        appCompatImageView5.perform(scrollTo(), click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());
    }

    private void chooseNumRoommates() {
        ViewInteraction appCompatRadioButton = onView(
                allOf(withId(R.id.radio_btn_as_3), withText(filters_num_roommates),
                        childAtPosition(
                                allOf(withId(R.id.radioGroup_max_rm_as),
                                        childAtPosition(
                                                withId(R.id.cl_filters),
                                                11)),
                                1)));
        appCompatRadioButton.perform(scrollTo(), click());
    }

    private void enterAgeRange() {
        ViewInteraction appCompatEditText13 = onView(
                allOf(withId(R.id.et_min_age_val),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                14)));
        appCompatEditText13.perform(scrollTo(), replaceText(filters_min_age));

        ViewInteraction appCompatEditText14 = onView(
                allOf(withId(R.id.et_min_age_val), withText(filters_min_age),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                14),
                        isDisplayed()));
        appCompatEditText14.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText17 = onView(
                allOf(withId(R.id.et_max_age_val),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                16)));
        appCompatEditText17.perform(scrollTo(), replaceText(filters_max_age));

        ViewInteraction appCompatEditText18 = onView(
                allOf(withId(R.id.et_max_age_val), withText(filters_max_age),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                16),
                        isDisplayed()));
        appCompatEditText18.perform(closeSoftKeyboard());
    }

    private void chooseAdditionalFilters() {
        ViewInteraction appCompatCheckBox = onView(
                allOf(withId(R.id.check_box_smoking), withText("Smoking free"),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                19)));
        appCompatCheckBox.perform(scrollTo(), click());

        ViewInteraction appCompatCheckBox2 = onView(
                allOf(withId(R.id.check_box_ac), withText("AC"),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                24)));
        appCompatCheckBox2.perform(scrollTo(), click());

        ViewInteraction appCompatCheckBox3 = onView(
                allOf(withId(R.id.check_box_pets), withText("No pets"),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                22)));
        appCompatCheckBox3.perform(scrollTo(), click());

        ViewInteraction appCompatCheckBox4 = onView(
                allOf(withId(R.id.check_box_kosher), withText("Kosher"),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                26)));
        appCompatCheckBox4.perform(scrollTo(), click());
    }

    private void signInAndGoToApartmentSearcherPage() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.et_first_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatEditText.perform(replaceText(signin_first_name), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.et_last_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText(signin_last_name), closeSoftKeyboard());

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

    private void checkPriceRange() {
        onView(allOf(withId(R.id.et_min_cost_val), withText("1000"))).perform(scrollTo()).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.et_max_cost_val), withText("5000"))).perform(scrollTo()).check(matches(isDisplayed()));
    }

    private void checkLocation() {
        ViewInteraction appCompatButton = onView(allOf(withId(R.id.btn_choose_locations),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                5))).perform(click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(allOf(withId(R.id.select_dialog_listview),
                        childAtPosition(
                                withId(R.id.contentPanel),
                                0)))
                .atPosition(filters_location);
        appCompatCheckedTextView.check(matches(isChecked()));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());
    }

    private void checkEntryDate() { // todo will be updated with Carmel's issue fix
//        ViewInteraction appCompatImageView5 = onView(
//                allOf(withId(R.id.iv_choose_entry_date),
//                        childAtPosition(
//                                allOf(withId(R.id.cl_filters),
//                                        childAtPosition(
//                                                withId(R.id.sv_filters_as),
//                                                0)),
//                                8)));
//        appCompatImageView5.perform(scrollTo(), click());
//
//        ViewInteraction appCompatButton3 = onView(
//                allOf(withId(android.R.id.button1), withText("OK"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withClassName(is("android.widget.LinearLayout")),
//                                        0),
//                                2),
//                        isDisplayed()));
//        appCompatButton3.perform(click());
    }

    private void checkNumRoommates() {
        ViewInteraction appCompatRadioButton = onView(
                allOf(withId(R.id.radio_btn_as_3), withText(filters_num_roommates),
                        childAtPosition(
                                allOf(withId(R.id.radioGroup_max_rm_as),
                                        childAtPosition(
                                                withId(R.id.cl_filters),
                                                11)),
                                1)));
        appCompatRadioButton.perform(scrollTo()).check(matches(isChecked()));
    }

    private void checkAgeRange() {
        ViewInteraction appCompatEditText13 = onView(
                allOf(withId(R.id.et_min_age_val), withText(filters_min_age),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                14)));
        appCompatEditText13.perform(scrollTo());
        appCompatEditText13.check(matches(isDisplayed()));

        ViewInteraction appCompatEditText17 = onView(
                allOf(withId(R.id.et_max_age_val), withText(filters_max_age),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                16)));
        appCompatEditText17.perform(scrollTo()).check(matches(isDisplayed()));
    }

    private void checkAdditionalFilters() {
        ViewInteraction appCompatCheckBox = onView(
                allOf(withId(R.id.check_box_smoking), withText("Smoking free"),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                19)));
        appCompatCheckBox.perform(scrollTo()).check(matches(isChecked()));

        ViewInteraction appCompatCheckBox2 = onView(
                allOf(withId(R.id.check_box_ac), withText("AC"),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                24)));
        appCompatCheckBox2.perform(scrollTo()).check(matches(isChecked()));

        ViewInteraction appCompatCheckBox3 = onView(
                allOf(withId(R.id.check_box_pets), withText("No pets"),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                22)));
        appCompatCheckBox3.perform(scrollTo()).check(matches(isChecked()));

        ViewInteraction appCompatCheckBox4 = onView(
                allOf(withId(R.id.check_box_kosher), withText("Kosher"),
                        childAtPosition(
                                allOf(withId(R.id.cl_filters),
                                        childAtPosition(
                                                withId(R.id.sv_filters_as),
                                                0)),
                                26)));
        appCompatCheckBox4.perform(scrollTo()).check(matches(isChecked()));
    }

    /**
     * This function performs a click action. It was added to substitute Espresso's function.
     * Now, we are using our custom GeneralClickAction class which does not assert that a view,
     * which we want to click on, is 90% visible. (The problem with 90% visibility arose in this
     * test when trying to click "save" after editing filters)
     */
    public static ViewAction click() {
        return actionWithAssertions(
                new com.example.roome.GeneralClickAction(
                        Tap.SINGLE,
                        GeneralLocation.VISIBLE_CENTER,
                        Press.FINGER,
                        InputDevice.SOURCE_UNKNOWN,
                        MotionEvent.BUTTON_PRIMARY));
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
