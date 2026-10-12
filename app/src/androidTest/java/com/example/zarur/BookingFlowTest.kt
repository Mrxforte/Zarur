package com.example.zarur

import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.zarur.data.local.AppPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookingFlowTest {

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val appPreferences = AppPreferences(context)
        appPreferences.isLoggedIn = true
        appPreferences.isFirstTimeLaunch = false
    }

    @Test
    fun testCompleteBookingFlow() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val navHostFragment = activity.supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.bookRealEstateFragment)
            }

            // 1. Book Real Estate Screen
            onView(withId(R.id.btnContinue)).perform(click())

            // 2. Booking Info Screen
            onView(withId(R.id.btnContinue)).perform(click())

            // 3. Select Payment Screen
            onView(withId(R.id.btnContinue)).perform(click())

            // 4. Review Summary Screen
            onView(withId(R.id.btnConfirm)).perform(click())

            // 5. Enter PIN Screen
            onView(withText("1")).perform(click())
            onView(withText("2")).perform(click())
            onView(withText("3")).perform(click())
            onView(withText("4")).perform(click())
            onView(withId(R.id.btnContinue)).perform(click())

            // 6. Booking Status Screen
            onView(withId(R.id.btnViewReceipt)).perform(click())

            // 7. E-Receipt Screen
            onView(withId(R.id.btnReview)).perform(click())

            // 8. Leave Review Screen
            onView(withId(R.id.etReviewText)).perform(typeText("Great booking experience!"))
            onView(withId(R.id.btnSubmit)).check(matches(isDisplayed()))
        }
    }
}
