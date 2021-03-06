package ch.epfl.sweng.favors.favors;

import android.content.Intent;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.uiautomator.UiObjectNotFoundException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sweng.favors.R;
import ch.epfl.sweng.favors.database.FakeDatabase;
import ch.epfl.sweng.favors.utils.ExecutionMode;
import ch.epfl.sweng.favors.utils.FragmentTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sweng.favors.favors.FavorPosterDetailView.OWNER_ID;
import static java.lang.Thread.sleep;


public class FavorPosterDetailViewTest {

    @Rule public FragmentTestRule<FavorPosterDetailView> mFragmentTestRule = new FragmentTestRule<>(FavorPosterDetailView.class);

    public static final String fakePosterId = "U2"; // Email of the profile we want to show

    @Before
    public void Before(){
        ExecutionMode.getInstance().setTest(true);
        FakeDatabase.getInstance().createBasicDatabase();
    }

    @Test
    public void titleIsCorrectlyDisplayed(){
        mFragmentTestRule.launchActivity(new Intent().putExtra(OWNER_ID, fakePosterId));
        onView(ViewMatchers.withId(R.id.posterTitle)).perform(scrollTo()).check(matches(isDisplayed()));

    }
    @Test
    public void profilePictureDisplayed(){
        mFragmentTestRule.launchActivity(new Intent().putExtra(OWNER_ID, fakePosterId));
        onView(withId(R.id.profilePic)).check(matches(isDisplayed()));
    }

    @Test
    public void posterDetailsAreOk(){
        mFragmentTestRule.launchActivity(new Intent().putExtra(OWNER_ID, fakePosterId));
        try {
            sleep(1000);
        } catch (InterruptedException ignored) {

        }
        onView(withId(R.id.posterFirstName)).check(matches(withText("Jeanne")));
        onView(withId(R.id.posterLastName)).check(matches(withText("Trousse")));
        onView(withId(R.id.gender)).check(matches(withText("F")));
        onView(withId(R.id.okButton)).perform(scrollTo());
        onView(withId(R.id.okButton)).check(matches(isDisplayed()));
    }

    @Test
    public void pictureCallback(){
        FavorPosterDetailView.pictureCallback.onPropertyChanged(null, 0);
    }

}
