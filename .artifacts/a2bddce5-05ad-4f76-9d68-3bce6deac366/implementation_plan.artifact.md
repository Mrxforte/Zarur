# Implementation Plan - Property Details & Booking Flow

Implement a pixel-perfect Property Details screen and the complete Booking workflow with dummy data, matching the Figma designs.

## User Review Required

> [!IMPORTANT]
> The booking flow consists of approximately 10 separate screens. I will implement them as individual fragments within a new `booking` package to maintain a clean navigation structure.

## Proposed Changes

### [New Package: details]

#### [NEW] [PropertyDetailFragment.kt](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/details/PropertyDetailFragment.kt)
- Manages the property details UI, including facilities, gallery, and reviews.

#### [NEW] [fragment_property_detail.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/fragment_property_detail.xml)
- Complex layout using `CoordinatorLayout` and `NestedScrollView`.
- Hero image with back and favorite buttons.
- Content sections: Info, Owner, Overview, Facilities, Gallery, Location, Reviews.
- Floating bottom bar with price and "Booking Now" button.

### [New Package: booking]

#### [NEW] [Booking Fragments](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/java/com/example/zarur/presentation/booking/)
- `BookRealEstateFragment.kt`: Date selection (Calendar).
- `BookingInfoFragment.kt`: User details form.
- `SelectPaymentFragment.kt`: Payment method selection.
- `ReviewSummaryFragment.kt`: Booking summary and total.
- `BookingPinFragment.kt`: Security PIN entry.
- `BookingStatusFragment.kt`: Success/Failure feedback.
- `EReceiptFragment.kt`: Digital receipt with barcode.
- `LeaveReviewFragment.kt`: Rating and comment screen.

#### [NEW] [Booking Layouts](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/layout/)
- `fragment_book_real_estate.xml`
- `fragment_booking_info.xml`
- `fragment_select_payment.xml`
- `fragment_review_summary.xml`
- `fragment_booking_pin.xml`
- `fragment_booking_status.xml` (Reusable for success/fail)
- `fragment_e_receipt.xml`
- `fragment_leave_review.xml`

### [Navigation]

#### [MODIFY] [nav_graph.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/navigation/nav_graph.xml)
- Add all new fragments and define the linear navigation flow from Details -> Date -> Info -> Payment -> Summary -> PIN -> Status -> E-Receipt -> Review.

### [Resources]

#### [MODIFY] [strings.xml](file:///C:/Users/Azamat/AndroidStudioProjects/Zarur/app/src/main/res/values/strings.xml)
- Add all necessary labels and button text for the new screens.

## Verification Plan

### Manual Verification
- Deploy to an emulator/device.
- Navigate to a Property Detail screen (e.g., from Home or Favorites).
- Verify the layout matches Figma screens (Info, Gallery, etc.).
- Click "Booking Now" and proceed through the entire flow:
    - Select dates and click Continue.
    - Fill/Verify info and click Continue.
    - Select payment and click Continue.
    - Review summary and click "Confirm Payment".
    - Enter dummy PIN.
    - Verify Success screen and "View E-Receipt".
    - Verify E-Receipt layout.
    - Navigate to "Leave a Review" and verify the UI.
