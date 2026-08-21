# Zarur - Real Estate App

Zarur is a modern Android real estate application designed with a high-fidelity user experience, featuring an immersive onboarding flow and a robust authentication system.

## Features

- **Immersive Splash Screen**: A bold, branded entry point with the corporate identity.
- **One-Time Intro Flow**: A visually stunning welcome screen shown only on the first launch.
- **Interactive Onboarding**: A step-by-step walkthrough of the app's key value propositions with elegant dash indicators.
- **Modern Authentication**:
  - Sign In and Sign Up screens with a sleek "curved card" design.
  - Password recovery (Forgot Password) and Change Password flows.
  - Social Login integration (Google, Facebook, X).
- **Explore (Beta)**: A map-based discovery interface (currently with static placeholders) for finding properties near the user's location.
- **Bottom Navigation**: Seamless transition between Explore, Favorites, Messages, and Profile sections.
- **Dark Mode Support**: Full support for system-wide dark theme with dynamic color adaptation.

## Tech Stack

- **Kotlin**: Primary programming language.
- **Jetpack Navigation**: For seamless fragment-based navigation.
- **Hilt**: Dependency Injection for a clean and testable architecture.
- **Material 3**: Utilizing modern design components and dynamic theming.
- **Clean Architecture**: Organized into Data, Domain, and Presentation layers for better maintainability.
- **ViewPager2**: Powering the onboarding walkthrough.
- **SharedPreferences**: Managing app state and one-time launch flags.

## Project Structure

- `data`: Implementation of repositories and local storage.
- `domain`: Core business models and repository interfaces.
- `presentation`: UI components (Fragments, Adapters) organized by feature (Auth, Intro, Onboarding, Explore).

## Getting Started

1. Clone the repository.
2. Open in Android Studio.
3. Sync Gradle and run the `:app` module.

---
Built with ❤️ for a premium real estate discovery experience.
