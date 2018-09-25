package com.deguffroy.adrien.go4lunch;

import com.deguffroy.adrien.go4lunch.Activity.MainActivity;
import com.deguffroy.adrien.go4lunch.Fragments.MapFragment;
import com.deguffroy.adrien.go4lunch.Fragments.MatesFragment;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertNotNull;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment;

/**
 * Created by Adrien Deguffroy on 24/09/2018.
 */

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private MainActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mActivity = Robolectric.setupActivity(MainActivity.class);
    }

    @Test
    public void checkIfFirstFragmentIsDisplayed() {
        MatesFragment mapFragment = MatesFragment.newInstance();
        startFragment(mapFragment);
        assertNotNull(mapFragment);
    }
}
