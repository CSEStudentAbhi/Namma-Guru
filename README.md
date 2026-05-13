# Nimma Guru

**Nimma Guru** is an Android application built to connect users with experts ("Gurus") for personalized sessions. It features a modern user interface and leverages Firebase for backend services. 

## 🚀 Features

* **User Authentication**: Secure Login and Registration using Firebase Auth.
* **Guru Profiles**: Discover and view detailed profiles of Gurus.
* **Session Management**: Browse and schedule sessions with Gurus using an integrated Calendar.
* **Search Functionality**: Easily search for specific Gurus or topics.
* **Appreciations**: Users can leave appreciations/reviews for Gurus.
* **Profile Management**: View and edit user profiles seamlessly.
* **Modern UI**: Built using a mix of Jetpack Compose, XML, and Material Design.

## 🔄 App Workflow (User Flow)

1. **Splash & Onboarding**: The app launches with a `SplashActivity`.
2. **Authentication**: Users must log in (`LoginActivity`) or register (`RegisterActivity`) via Firebase Authentication to access the platform.
3. **Home Dashboard (`HomeFragment`)**: After a successful login, users land on the home screen to see quick actions, upcoming sessions, and featured Gurus.
4. **Search & Discovery (`SearchFragment`)**: Users can search for specific Gurus or browse by expertise and category. 
5. **Scheduling (`CalendarFragment`)**: Users can view availability and schedule one-on-one sessions with a selected Guru.
6. **Profile Management (`ProfileFragment` & `EditProfileActivity`)**: Users can view their scheduled sessions, leave appreciations for Gurus, and update their personal details.

## 🛠️ Tech Stack

* **Language**: Kotlin
* **UI**: 
  * Jetpack Compose
  * View Binding
  * XML Layouts (ConstraintLayout, Material Components)
* **Architecture**: Navigation Component
* **Backend Services** (Firebase):
  * Firebase Authentication
  * Firebase Firestore (Database)
  * Firebase Storage (Media)
* **Image Loading**: Glide
* **Build System**: Gradle (Kotlin DSL)

## 📁 Project Structure

```
app/src/main/java/com/example/nimma_guru/
├── activities/       # UI entry points (Splash, Login, Register, EditProfile)
├── adapters/         # RecyclerView Adapters (GuruAdapter, SessionAdapter, AppreciationAdapter)
├── fragments/        # Main app screens (Home, Search, Calendar, Profile)
├── model/            # Data classes (User, Guru, Session, Appreciation)
└── ui/               # UI-related utilities and theme files
```

## ⚙️ Getting Started

### Prerequisites
* **Android Studio**: Android Studio Koala (or later) recommended.
* **JDK**: Java 11+
* **Firebase Configuration**: You will need a `google-services.json` file to connect to Firebase.

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository_url>
   ```
2. **Open the project in Android Studio.**
3. **Add Firebase Credentials:**
   * Go to the [Firebase Console](https://console.firebase.google.com/).
   * Create a new project (or use an existing one) and add an Android app.
   * Download the `google-services.json` file.
   * Place it in the `app/` directory of the project.
4. **Sync Project with Gradle Files.**
5. **Run the App:** Select your emulator or physical device and click Run.
