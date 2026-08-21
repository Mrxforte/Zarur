# Implementation Plan - Profile & Settings UI

This plan outlines the pixel-perfect UI implementation of the Profile and Settings screens, following the provided designs.

## Proposed Changes

### [Assets & Resources]

#### [NEW] Profile Icons
- Create vector drawables for: `ic_booking`, `ic_payment`, `ic_profile_edit`, `ic_notification`, `ic_security`, `ic_language`, `ic_dark_mode`, `ic_help`, `ic_invite`, `ic_logout`.

#### [MODIFY] [strings.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/values/strings.xml)
- Add labels for all profile menu items and settings screens.

### [Presentation Layer - Profile]

#### [MODIFY] [fragment_profile.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/fragment_profile.xml)
- Implement the main profile dashboard:
    - Profile picture with edit overlay.
    - User name ("Andrew Ainsley").
    - Scrollable list of options with icons and navigation arrows.
    - Toggle switch for "Dark Mode".

#### [NEW] [EditProfileFragment.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/profile/EditProfileFragment.kt) & [fragment_edit_profile.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/fragment_edit_profile.xml)
- Form UI with inputs for Full Name, Nickname, Date of Birth, Email, Phone Number, Gender, etc.

#### [NEW] [MyBookingFragment.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/profile/MyBookingFragment.kt) & [fragment_my_booking.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/fragment_my_booking.xml)
- Tabs for "Active" and "Completed".
- List of booking cards with property details and status buttons.

#### [NEW] [Settings Fragments]
- `NotificationSettingsFragment`: List of toggle switches.
- `SecuritySettingsFragment`: Toggle switches and "Change PIN/Password" buttons.
- `LanguageSettingsFragment`: Radio group list of languages.
- `InviteFriendsFragment`: Contact list with "Invite" buttons.
- `HelpCenterFragment`: FAQ and Contact Us sections.

#### [NEW] [LogoutBottomSheet.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/profile/LogoutBottomSheet.kt)
- A modal bottom sheet for logout confirmation.

### [Navigation]

#### [MODIFY] [nav_graph.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/navigation/nav_graph.xml)
- Add all new profile-related destinations and actions.

## Verification Plan

### Manual Verification
- Navigate to "Profile" from bottom nav.
- Click each menu item and verify navigation to the correct sub-screen.
- Verify that the UI matches the Figma design (colors, spacing, typography).
- Toggle Dark Mode from the profile screen and verify system-wide theme change (UI-only placeholder if logic is excluded, or real toggle if allowed).
