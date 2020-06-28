package com.example.roome;


import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;


/**
 * This class includes 2 tests.
 * Test #1 checks that when a user tries to sign in (without google) with invalid info, a pop-up
 * dialog appears with a information about input validity.
 * Test #2 checks that when a user enter valid information, the proccess completes successfully
 * and that the user is transferred to ChoosingActivity.
 *
 * Actions:
 *
 *          - (Rule) start MainActivity
 *
 *          Test #1:
 *              - enter invalid first and last name pairs as given in testInputs
 *              (after each pair entered)
 *              - click the sign in button
 *              - click ok on the pop-up dialog
 *              =   end   =
 *
 *          Test #2:
 *              - enter valid first and last name pairs as given in testInputs
 *              - click the sign in button
 *              - check that ChoosingActivity page is displayed
 *              =   end   =
 *
 */
@LargeTest
public class SignInTest {

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

    // Test #1
    @Test
    public void SignInWInvalidInputTest() {
        // Start input testing
        ArrayList<Pair<String, String>> testInputs = create_test_input_array();
        for (int i = 0; i < testInputs.size(); i++)
        {
            String first = testInputs.get(i).first;
            String last  = testInputs.get(i).second;

            clickOnFirstNameEditText();
            enterFirstName(first);
            enterLastName(last);
            clickWithoutGoogleSignInBtn();
            // if all goes well, on each input (which is invalid) a popup dialog with a msg will
            // appear, on which we will press the "ok" button to continue
            clickOkOnPopup();
        }
    }

    // Test #2
    @Test
    public void SignInWValidInputTest() {
        clickOnFirstNameEditText();
        enterFirstName("Spongebob");
        enterLastName("Squarepants");
        clickWithoutGoogleSignInBtn();
        // if all goes well, ChoosingActivity page is displayed
        checkChoosingActivityIsDisplayed();
        deleteAccount();
    }

    /////////////////////////////////////// Functions //////////////////////////////////////////////

    private void deleteAccount() {
        // choose apartment searcher
        onView(allOf(withId(R.id.iv_choosing_button_apt),
                childAtPosition(allOf(withId(R.id.cl_choosing_activity),
                        childAtPosition(withId(android.R.id.content), 0)), 3),
                isDisplayed())).perform(click());
        // go to edit profile page
        onView(allOf(childAtPosition(childAtPosition(withId(R.id.tabs_apartment),
                0), 2), isDisplayed())).perform(click());
        // click delete account
        onView(withId(R.id.btn_delete_as_user)).perform(scrollTo(), click());
        // make sure we are on login page by asserting that login button is displayed
        onView(withId(R.id.btn_sign_without_google)).check(matches(isDisplayed()));
    }

    private void checkChoosingActivityIsDisplayed() {
        onView(allOf(withId(R.id.iv_choosing_button_apt),
                        childAtPosition(
                                allOf(withId(R.id.cl_choosing_activity),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));

        onView(allOf(withId(R.id.iv_choosing_button_apt),
                childAtPosition(
                        allOf(withId(R.id.cl_choosing_activity),
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0)),
                        6),
                isDisplayed()));
    }

    private void clickOkOnPopup() {
        onView(withText("OK")).perform(scrollTo(), click());
    }

    private void clickWithoutGoogleSignInBtn() {
        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.btn_sign_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                6),
                        isDisplayed()));
        appCompatImageView.perform(click());
    }

    private void enterLastName(String last) {
        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.et_last_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText(last), closeSoftKeyboard());
    }

    private void enterFirstName(String first) {
        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.et_first_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText(first), closeSoftKeyboard());
    }

    private void clickOnFirstNameEditText() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.et_first_name_without_google),
                        childAtPosition(
                                allOf(withId(R.id.cl_sign_in),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatEditText.perform(click());
    }

    private ArrayList<Pair<String, String>> create_test_input_array() {
        ArrayList<Pair<String, String>> testInputs = new ArrayList<>();
        testInputs.add(new Pair<>("", ""));
        testInputs.add(new Pair<>(" ", ""));
        testInputs.add(new Pair<>("", " "));
        testInputs.add(new Pair<>(" ", " "));
        testInputs.add(new Pair<>("123", "456"));
        testInputs.add(new Pair<>("123", "abc"));
        testInputs.add(new Pair<>("abc", "456"));
        testInputs.add(new Pair<>("abc", "$7_"));

        return testInputs;
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
