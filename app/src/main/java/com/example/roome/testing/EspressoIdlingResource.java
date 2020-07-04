// Source:
// By Catalin Ghita
// https://android.jlelse.eu/integrate-espresso-idling-resources-in-your-app-to-build-flexible-ui-tests-c779e24f5057


package com.example.roome.testing;

import androidx.test.espresso.IdlingResource;

public class EspressoIdlingResource {

    private static final String RESOURCE = "GLOBAL";

    private static SimpleCountingIdlingResource mCountingIdlingResource =
            new SimpleCountingIdlingResource(RESOURCE);

    public static void increment() {
        mCountingIdlingResource.increment();
    }

    public static void decrement() {
        mCountingIdlingResource.decrement();
    }

    public static IdlingResource getIdlingResource() {
        return mCountingIdlingResource;
    }
}
