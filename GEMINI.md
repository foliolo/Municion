# GEMINI.md

## Project Overview

This is an Android application named "Munición" (Ammunition), written in Kotlin. Its main purpose is to help users manage their ammunition, firearm licenses, and related activities. The application is built using modern Android development practices.

### Key Technologies & Architecture:

*   **Language:** Kotlin
*   **UI:** The app uses a hybrid approach with both the traditional View system (ViewBinding) and the modern Jetpack Compose toolkit. It utilizes Material Components for a consistent design.
*   **Architecture:** The app appears to follow a modern Android architecture, likely a variation of MVVM (Model-View-ViewModel), given the use of ViewModel, LiveData/Flow, and a repository pattern.
*   **Database:** Room is used for local data persistence.
*   **Dependency Injection:** Hilt is used for dependency management, simplifying the process of providing dependencies to different parts of the app.
*   **Asynchronous Operations:** Kotlin Coroutines are used for managing background threads and asynchronous tasks. WorkManager is also included for deferrable background work.
*   **Navigation:** The app uses the Jetpack Navigation component to manage navigation between different screens.
*   **Backend:** Firebase is integrated, specifically for Crashlytics and Firebase Cloud Messaging (FCM) for push notifications.
*   **Image Loading:** The Coil library is used for loading images.
*   **Data Storage:** DataStore is used for simple key-value data storage.
*   **Security:** The app includes biometric authentication features.

## Building and Running

This is a standard Android project and can be built and run using Android Studio or the Gradle wrapper.

### From Android Studio:

1.  Open the project in Android Studio.
2.  Let Gradle sync and download the necessary dependencies.
3.  Select a run configuration (usually the `app` module).
4.  Choose a device or emulator.
5.  Click the "Run" button.

### From the Command Line:

*   **Build a debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
*   **Install on a connected device:**
    ```bash
    ./gradlew installDebug
    ```
*   **Run tests:**
    ```bash
    ./gradlew test
    ```
*   **Run instrumented tests:**
    ```bash
    ./gradlew connectedAndroidTest
    ```

## Development Conventions

*   **Language:** The project is written entirely in Kotlin.
*   **Null Safety:** Kotlin's null safety features are likely used throughout the codebase.
*   **Code Style:** The code likely follows the standard Kotlin coding conventions.
*   **Testing:** The project is set up for both unit testing (JUnit, MockK) and instrumented testing (Espresso). Developers are expected to write tests for new features and bug fixes.
*   **Dependency Management:** Dependencies are managed centrally in the `build.gradle.kts` files. Hilt is used for dependency injection, so new dependencies should be provided through Hilt modules where appropriate.
*   **UI Development:** The project uses both XML layouts with ViewBinding and Jetpack Compose. Developers should be familiar with both approaches. New features might be implemented using Jetpack Compose, while existing screens might still use the View system.
*   **Language & Documentation:** All code comments, documentation (KDoc), and explanations within the code must be written in English. Maintain this standard for all new code and when refactoring existing code.
