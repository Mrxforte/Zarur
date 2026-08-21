# Implementation Plan - Explore Screens & Bottom Navigation

This plan outlines the steps to implement the "Explore" feature and set up the main app shell with a bottom navigation bar, following the provided designs.

## User Review Required

> [!IMPORTANT]
> The bottom navigation bar should only be visible on the main feature screens (Explore, Favorites, Message, Profile) and hidden on Intro, Onboarding, and Auth screens. I will implement this logic in `MainActivity`.

## Proposed Changes

### [Resources]

#### [NEW] [menu/bottom_nav_menu.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/menu/bottom_nav_menu.xml)
- Define menu items for Explore, Favorites, Message, and Profile with appropriate icons.

#### [MODIFY] [strings.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/values/strings.xml)
- Add labels for bottom navigation and explore screen elements.

### [Presentation Layer - Explore & Main Shell]

#### [NEW] [explore package](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/explore/)
- `ExploreFragment.kt`: Main explore screen with a static map image.
- `EnableLocationFragment.kt`: UI for prompting the user to enable location permissions.

#### [NEW] [feature packages]
- `presentation.favorites.FavoritesFragment.kt`: Placeholder for the favorites list.
- `presentation.message.MessageFragment.kt`: Placeholder for the messaging feature.
- `presentation.profile.ProfileFragment.kt`: Placeholder for the user profile.

#### [MODIFY] [activity_main.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/activity_main.xml)
- Wrap the `NavHostFragment` in a `ConstraintLayout`.
- Add `BottomNavigationView` anchored to the bottom.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/MainActivity.kt)
- Setup `BottomNavigationView` with `NavController`.
- Add a destination listener to hide/show the bottom nav based on the current screen.

### [Layouts]

#### [NEW] [fragment_explore.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/fragment_explore.xml)
- Implement the search/location header.
- Static map image placeholder (as requested).
- Property card overlay at the bottom.

#### [NEW] [fragment_enable_location.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/fragment_enable_location.xml)
- Implement the UI from the design: large location icon, "Enable Location" title, description, and buttons.

### [Navigation]

#### [MODIFY] [nav_graph.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/navigation/nav_graph.xml)
- Add all new fragments as destinations.
- Update `SignUpFragment` and `SignInFragment` to navigate to `ExploreFragment` (or `EnableLocationFragment`) upon success.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify compilation.

### Manual Verification
- Verify that the bottom nav appears only after logging in.
- Test navigation between the four main tabs.
- Verify the "Enable Location" UI matches the design.
