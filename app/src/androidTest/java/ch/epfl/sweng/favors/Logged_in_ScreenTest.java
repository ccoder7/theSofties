package ch.epfl.sweng.favors;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class Logged_in_ScreenTest {
    @Test
    public void firstTest(){
        assertEquals(true, true);
    }

}