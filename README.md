<img width="1080" height="1762" alt="HOME PAGE 2_UniPath SA" src="https://github.com/user-attachments/assets/302b5cf3-8da2-44a5-94bf-33cc54c580d2" /># 🎓 UniPath SA

### Find your path. Build your future.


[![Android CI](https://github.com/AyandaM03/UniPath-SA/actions/workflows/build.yml/badge.svg)](https://github.com/AyandaM03/UniPath-SA/actions/workflows/build.yml)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Kotlin-purple)
![Backend](https://img.shields.io/badge/backend-Firebase-orange)

**▶️ Demo video:** [Watch UniPath SA on YouTube](https://youtu.be/5yWNZDnhybc?si=wVzVKQxtBfMF9xXZ)

**Quick links:** [Features](#feature-status) · [Design](#design-considerations) · [REST API](#rest-api-integration) · [Testing](#testing) · [GitHub & Actions](#github-and-github-actions) · [Release notes](#release-notes) · [Setup](#setup) · [AI use](#ai-use-statement)

---

## Project Overview

Choosing what to study after high school is hard. Learners have to compare universities, colleges, courses, admission requirements, bursaries and deadlines, often across many different websites.

**UniPath SA** is an Android app that brings these tasks into one place for South African high-school learners. It lets them calculate their Admission Point Score (APS), see which courses they qualify for, explore institutions and bursaries, save their favourites, and track their applications and documents.

The app was built for the IIE module *Open Source Coding (Intermediate) - OPSC6312* as a group project. It uses **Kotlin**, **XML layouts**, **Firebase** (Authentication, Firestore and Storage) and a **REST API** integrated with **Retrofit**.

---

## Purpose of the Application

UniPath SA helps learners to:

* Create an account and manage a personal learner profile.
* Enter their subject marks and calculate their APS.
* See the courses they qualify for, based on their APS.
* Explore courses, universities, colleges and bursaries stored in the cloud.
* Save institutions, courses and bursaries to a personal saved list.
* Track university applications and the documents they need.
* Follow their own preparation progress on a personalised dashboard.
* Use the app in light or dark mode.

---

## Target Users

The primary users are **South African learners in Grades 10-12 and matriculants** preparing for higher education. The app is also useful for learners who:

* are unsure which courses they qualify for,
* need help finding bursaries and funding, or
* want to organise several applications at once.

The design is therefore youthful, clear and simple, with large tap targets, readable text and consistent layouts.

---

## Feature Status

| Feature | Status |
| --- | --- |
| Register and log in with email and password (encrypted by Firebase Authentication) | ✅ Implemented |
| Google Sign-In (single sign-on) | ✅ Implemented |
| Password reset by email | ✅ Implemented |
| Learner profile (name, email, grade, school, phone) saved in Firestore | ✅ Implemented |
| Dashboard with greeting, preparation progress and quick stats | ✅ Implemented |
| APS calculator with REST API course matching | ✅ Implemented |
| Explore courses and bursaries (search and filters) | ✅ Implemented |
| Institutions list with logos, and details screen with campus image | ✅ Implemented |
| Saved items (bookmarks) | ✅ Implemented |
| Application tracker and application details | ✅ Implemented |
| Settings (dark mode, language) | ✅ Implemented |
| Splash screen and 3-page intro | ✅ Implemented |

---

## Main Features in Detail

### User Registration and Login

Users can create an account with their **full name, email, password and grade** (Grade 10, 11, 12 or Matriculant), or sign in with **Google**.

* Authentication is handled by **Firebase Authentication**. Passwords are hashed and stored by Firebase on its servers and **are never stored in Firestore or in the app**.
* The user's profile (full name, email, grade, created date) is saved in Firestore at `users/{uid}`.
* Learners can request a password reset email from the login screen.

<img width="1080" height="1913" alt="REGISTRATION_UniPath SA" src="https://github.com/user-attachments/assets/ffe87beb-89ff-48d3-b275-f70215bb247b" />
<img width="1080" height="2009" alt="LOGIN_UniPath SA" src="https://github.com/user-attachments/assets/2c00388f-0f0d-4769-8346-b44989c6465d" />

---

### Dashboard

After logging in, learners land on a personal dashboard:

* A greeting ("Hey, *name* 👋") and the slogan *Find your path. Build your future.*
* A **preparation progress** card that starts at 0% for a new learner and grows only when real tasks are completed (profile details, school, saved items and so on).
* Quick stats for the learner's APS, eligible courses, applications and saved items.
* A scrolling list of institutions with their logos, upcoming application deadlines, and recommended courses.

<img width="1080" height="2078" alt="HOME PAGE 1_UniPath SA" src="https://github.com/user-attachments/assets/f255c503-6b58-43e3-b00a-3f1e32b59412" />
<img width="1080" height="1762" alt="HOME PAGE 2_UniPath SA" src="https://github.com/user-attachments/assets/f2d946d0-84d8-4c51-b4f8-774ecd1d6826" />

---

### Learner Profile

Each learner has a profile linked to their Firebase account. It shows their name and email and lets them edit their **phone number and school**. The details are saved in Firestore under their unique user ID and load automatically the next time the profile is opened.

<img width="1080" height="1994" alt="USER PROFILE_UniPath SA" src="https://github.com/user-attachments/assets/e16f12fb-55c5-48e1-952b-360eaad0cc4a" />


---

### APS Calculator

* Learners can add or remove subjects and enter percentage marks.
* Each mark is converted to APS points using the South African Achievement Level Guide (7 points for 80-100%, down to 1 point for 0-29%).
* Marks outside 0-100 are rejected instead of crashing the calculation.
* After calculating, the app calls a **REST API** (see [REST API Integration](#rest-api-integration)) to fetch the courses and shows only the courses the learner's APS qualifies them for.

<img width="1080" height="2080" alt="APS_UniPath SA" src="https://github.com/user-attachments/assets/a72bfad1-93cd-47bd-a7a4-243e5c0a30cd" />


---

### Explore - Courses and Bursaries

* **Courses** can be filtered by category (for example IT, Commerce, Engineering, Law) and show the institution, APS requirement and duration.
* **Bursaries** show the provider, the amount, the field and the closing date.
* All course and bursary information is stored in **Firestore**, not hard-coded in the app.
* Tapping a card opens a details screen with a full description and a link to the official website.

<img width="1080" height="2032" alt="EXPLORE COURSES AND BURSARY_UniPath SA" src="https://github.com/user-attachments/assets/4a9f54ab-b2d2-4dcd-bed2-db2c53fdb3e2" />


---

### Institutions

* Browse universities and colleges (public and private) across South Africa.
* Each list item shows the institution's logo. The details screen shows a campus image fading into the content, the institution's description, whether it is free to apply, and a link to its website.
* If a campus image is not available, a clean gradient header with the logo is shown instead.
* Institutions can be saved with a **bookmark** button, and saved items appear on the Saved screen.

<img width="1080" height="2055" alt="INSTITUTIONS_UniPath SA" src="https://github.com/user-attachments/assets/80f01d9b-9e4f-4798-a4e9-a03de4a10ee4" />
<img width="1080" height="2340" alt="VIEW INSTITUTION_UniPath SA" src="https://github.com/user-attachments/assets/2b209bd6-c7cf-4dbe-83a3-fad86c66b7d1" />

---

### Application Tracker

Learners can add the applications they are working on (institution, course, deadline, status) and follow each one through its stages: Draft, Submitted, Under Review, Accepted or Rejected. Each application shows a document progress bar and its important dates.

<img width="1080" height="2070" alt="APPLICATION STATUS_UniPath SA" src="https://github.com/user-attachments/assets/7ce77079-15ce-46b2-a14a-38f2be7d9f6a" />
<img width="1080" height="2072" alt="APPLICATION TRACKER_UniPath SA" src="https://github.com/user-attachments/assets/f90eb09a-4324-4997-b8d5-882e4c055a73" />

---
### Documents

The documents screen lets learners keep the documents they need for applications (such as an ID copy, matric certificate and academic transcript) in Firebase Storage, linked to their account. *(Upload is currently being fixed.)*

<img width="1080" height="2060" alt="DOCUMENTS_UniPath SA" src="https://github.com/user-attachments/assets/6991ea15-0b79-40f8-bc98-8e10b4ac8a70" />

---

### Settings

* **Dark mode** - switches the theme between light and dark, and the choice is remembered.
* **Language** - choose between English, isiZulu, isiXhosa and Sesotho.
* Account options such as logging out.

<img width="1080" height="2081" alt="SETTINGS_UniPath SA" src="https://github.com/user-attachments/assets/2eb2b84b-0548-4088-b311-e380ff7861ee" />


---

## Design Considerations

UniPath SA aims to feel modern, friendly and easy to use for teenagers.

<img width="1080" height="2088" alt="NAVIGATION_UniPath SA" src="https://github.com/user-attachments/assets/9a4781f9-aa0e-4b69-87c9-196a9d9885e4" />


* **Navigation:** a left-side **navigation drawer** gives learners more screen space and is available from every main screen. Detail screens use a back arrow.
* **Colour:** blue and white lead the palette, with accent colours for success, deadlines and highlights. Status chips use colour to show the stage of an application at a glance.
* **Dark mode:** a deep navy theme with brighter accent colours, so text and icons stay readable.
* **Consistency:** one set of colours, fonts, rounded 16dp cards, button styles and spacing across all screens.
* **First impressions:** a white splash screen where the logo stands out, followed by a swipeable three-page introduction.
* **Feedback:** the app confirms actions (saving, logging in, errors) with clear messages and friendly empty states.
* **Accessibility:** readable text contrast, tap targets of at least 48dp, and multi-language support for South African languages.


<img width="1080" height="2013" alt="SPLASH PAGE UniPath SA" src="https://github.com/user-attachments/assets/6048ab2f-abce-41ed-9b9d-244668eecbc2" />

---

## Technologies Used

| Technology | Purpose |
| --- | --- |
| Kotlin | Main programming language |
| Android Studio | Development environment |
| XML | User-interface layouts |
| Firebase Authentication | Email/password and Google sign-in |
| Cloud Firestore | Cloud database |
| Firebase Storage | Storing learner documents and campus images |
| Retrofit + Gson | REST API calls and JSON parsing |
| OkHttp logging interceptor | Logging network calls while developing |
| Glide | Loading institution logos and images |
| Material Components | Cards, chips, buttons and the navigation drawer |
| JUnit | Unit testing |
| Git and GitHub | Version control |
| GitHub Actions | Continuous integration (tests and debug build) |
| Gradle (Kotlin DSL) | Build and dependency management |

---

## Firebase Architecture

```text
                       UniPath SA (Android app)
                                 |
          ┌──────────────────────┼──────────────────────┐
          ▼                      ▼                      ▼
   Firebase Auth          Cloud Firestore        Firebase Storage
 (email + Google)         (app data and          (documents and
          |                user profiles)          campus images)
          ▼                      |
   User accounts                 ▼
                         Firestore REST API
                      (via Retrofit, APS matching)
```

Firebase Authentication manages credentials. The app uses the signed-in user's unique ID (UID) to attach profile data, saved items, applications and documents to that user in Firestore. Passwords never appear in the database.

---

## Firestore Data Model

```text
Firestore
│
├── institutions   (shared, read-only for learners)
├── courses        (shared, read-only for learners)
├── bursaries      (shared, read-only for learners)
│
└── users
      └── {uid}
            ├── fullName, email, grade, createdAt
            ├── phone, school, aps, language
            ├── favourites     (saved institutions, courses, bursaries)
            ├── applications   (university, course, status, deadline, documents progress)
            ├── documents      (document type, status, file location)
            └── notifications  (title, message, read, createdAt)
```

**institutions:** `name`, `province`, `type` ("University" or "College"), `description`, `imageUrl` (logo), `website`, `freeToApply`, and an optional `heroImageUrl` (campus image).

**courses:** `name`, `university`, `apsRequired`, `duration`, `category`, `website`, `description`.

**bursaries:** `name`, `provider`, `amount`, `closingDate`, `category`, `website`, `description`.

<!-- VERIFY: confirm the actual field names in your Firestore documents (especially bursaries) and the name of the saved-items subcollection. -->

---

## REST API Integration

The **APS Calculator** talks directly to Firestore's **REST API** through **Retrofit**, separate from the Firestore SDK used on other screens.

**Endpoint:**

```text
GET https://firestore.googleapis.com/v1/projects/unipath-sa-3541/databases/(default)/documents/courses
```

Firestore returns its structured JSON format, which the app parses into its `Course` model. The courses are then filtered on the device so the learner only sees courses their APS qualifies them for. This satisfies the brief's requirement to use a REST API and integrate it into the app. The calls are logged with OkHttp's logging interceptor so the requests and responses can be inspected in Logcat.

<!-- VERIFY: make sure RetrofitClient.kt uses the project ID unipath-sa-3541 (no extra letters) and that Firestore rules allow public reads on "courses". -->

---

## Input Validation and Error Handling

The app is built so that invalid input does not crash it:

* **Registration:** full name, email, password (minimum length) and grade must be provided.
* **Login:** wrong password, unknown email or other Firebase errors are shown as readable messages.
* **APS calculator:** marks must be numbers between 0 and 100.
* **Network and database errors:** if a Firestore or REST call fails (for example with no internet), the app shows a message or an empty state instead of crashing.
* **Missing data:** missing or badly formatted fields in Firestore documents are handled with safe fallbacks.

---

## Security Considerations

* Passwords are handled only by Firebase Authentication and are never stored in Firestore.
* Firestore security rules allow anyone to **read** `institutions`, `courses` and `bursaries` (needed by the REST call), but allow a learner to read and write only **their own** `users/{uid}` data.
* Storage rules should likewise let learners access only their own folder.
* The `google-services.json` file contains project identifiers, not passwords. Facebook's App Secret and any other secrets must **never** be committed to the repository.

```text
match /institutions/{doc} { allow read: if true; }
match /courses/{doc}      { allow read: if true; }
match /bursaries/{doc}    { allow read: if true; }
match /users/{uid}/{document=**} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
}
```

<!-- VERIFY: confirm these are the rules you published in the Firebase console, and that Storage rules are set. -->

---

## Testing

### Manual testing (on a physical Android phone)

| Feature | Test |
| --- | --- |
| Registration | Create a valid account |
| Registration | Submit empty fields |
| Registration | Enter a password that is too short |
| Login | Log in with valid credentials |
| Login | Log in with invalid credentials |
| Login | Google Sign-In |
| APS Calculator | Calculate APS with valid marks |
| APS Calculator | Enter marks outside 0-100 or non-numeric input |
| APS Calculator | Confirm matching courses load through the REST API |
| Institutions | Load, search and filter institutions from Firestore |
| Saved items | Save and remove an item, then reopen the Saved screen |
| Explore | Filter courses and bursaries and open their details |
| Applications | Add, open and update an application |
| Profile | Save and reload phone and school details |
| Settings | Toggle dark mode and change the language |

### Automated unit tests

Unit tests live in `app/src/test/java/com/example/unipathsa/` and cover the app's business logic, such as APS calculation, preparation progress and translation key checks. Run them locally with:

```bash
./gradlew testDebugUnitTest
```

The test reports are generated in `app/build/reports/tests/`.

<!-- VERIFY: make sure the tests exercise the real app classes (in src/main), not copies. List the test classes here once final. -->

---

## GitHub and GitHub Actions

### Version control

The team used **GitHub** for source control and worked together using **feature branches** that were merged into `main` (for example `nav`, `bursaries`, `homepapge-and-nav` and `course-and-bursary-details`). Commits were made regularly, and the history shows how the app grew from the first prototype to this version. The final submission is tagged as `final-poe`.

Examples of milestones in the commit history:

```text
Initial Android project setup
Add Firebase Authentication (email/password and Google Sign-In)
Implement Splash, Login and Sign Up screens
Connect Institutions screen to Firestore
Implement APS calculator with REST API course matching
Add Explore (courses and bursaries) and details screens
Add Settings with dark mode and editable profile
Add navigation drawer, application tracker, documents and notifications
UI polish: design system, splash, intro flow, dark mode
```

### GitHub Actions (continuous integration)

The workflow is defined in `.github/workflows/build.yml` and runs on every **push** and **pull request**. It:

1. Checks out the code and sets up Java.
2. Runs the unit tests with the Gradle Wrapper.
3. Builds a **debug APK**.
4. Uploads the test reports and the APK as workflow **artifacts**.

This makes sure the app builds and passes its tests on a clean machine, not only on a developer's computer. You can see the results in the [Actions tab](https://github.com/AyandaM03/UniPath-SA/actions), and the badge at the top of this page shows the latest build status.

<!-- VERIFY: open .github/workflows/build.yml and check this list matches what the workflow really does. -->

---

## Release Notes

### Version 2.0 - Final PoE (November 2026)

**Innovative features added in this version are marked with ⭐.**

**New**
* ⭐ **Preparation progress tracker** on the dashboard that reflects only real, completed tasks.
* ⭐ **Application tracker** with status chips, deadlines and a document progress bar.
* ⭐ **Document manager** backed by Firebase Storage. *(upload being finalised)*
* ⭐ **Institution details with campus image and gradient fade**, with a logo fallback.
* ⭐ **Multi-language support:** English, isiZulu, isiXhosa and Sesotho. *(translation of all screens in progress)*
* Left-side **navigation drawer** replacing the bottom navigation bar.
* Splash screen with the new logo and a swipeable 3-page introduction.
* Bookmark-style **save** button and a Saved items screen.
* Notifications screen.
* New launcher icon and image assets.

**Improved**
* Complete visual redesign with a consistent colour palette, rounded cards and a richer dark mode.
* Dashboard redesigned with a greeting, slogan and stat tiles.
* More robust parsing of Firestore data so one bad document cannot crash a screen.
* Logging added through the app to trace data loading and errors.

**Fixed**
* Dark-mode text and icons that were hard to see.
* Crashes and missing names on some course and bursary cards.

### Version 1.0 - Prototype (Part 2)

* Email and Google authentication, learner profile and settings (dark mode).
* APS calculator with REST API course matching.
* Institutions, Explore (courses and bursaries) and favourites, with bottom navigation.


---

## Project Structure

```text
UniPath-SA
│
├── .github
│   └── workflows
│       └── build.yml
│
├── app
│   └── src
│       ├── main
│       │   ├── java/com/example/unipathsa
│       │   │   ├── SplashActivity.kt / IntroActivity.kt
│       │   │   ├── LoginActivity.kt / SignUpActivity.kt
│       │   │   ├── HomeActivity.kt / NavigationHelper.kt / PreparationCalculator.kt
│       │   │   ├── InstitutionsActivity.kt / InstitutionAdapter.kt
│       │   │   ├── ExploreActivity.kt / CourseDetailActivity.kt / BursaryDetailsActivity.kt
│       │   │   ├── ApsCalculatorActivity.kt
│       │   │   ├── ApplicationsActivity.kt / ApplicationDetailActivity.kt
│       │   │   ├── DocumentsActivity.kt / NotificationsActivity.kt
│       │   │   ├── ProfileActivity.kt / SettingsActivity.kt / SavedActivity.kt
│       │   │   ├── RetrofitClient.kt / FirestoreApi.kt / FirestoreModels.kt
│       │   │   ├── Institution.kt / Course.kt / Bursary.kt / ApplicationModel.kt
│       │   │   └── *Adapter.kt
│       │   └── res
│       │       ├── drawable / layout / menu / mipmap
│       │       └── values (strings in English, isiZulu, isiXhosa, Sesotho) / values-night
│       └── test/java/com/example/unipathsa   (unit tests)
│
├── gradle
├── build.gradle.kts / settings.gradle.kts
├── gradlew / gradlew.bat
├── .gitignore
└── README.md
```

---

## Setup

1. Clone the repository:

```bash
git clone https://github.com/AyandaM03/UniPath-SA.git
```

2. Open the project in **Android Studio**.
3. Make sure `google-services.json` is in the `app/` folder (the Firebase configuration for the project `unipath-sa-3541`). To use your own Firebase project, replace it with your own file and keep the package name `com.example.unipathsa`.
4. For **Google Sign-In** on your device, add your debug **SHA-1** fingerprint in the Firebase console (Project settings → Your apps). Get it by running `./gradlew signingReport`.
5. Sync Gradle, connect a phone (or start an emulator) and run the app.

### Firebase setup (if starting fresh)

1. Create a Firebase project and add the Android app `com.example.unipathsa`.
2. Enable **Authentication** with the Email/Password and Google providers.
3. Enable **Cloud Firestore** and create the `institutions`, `courses` and `bursaries` collections (see the data model above).
4. Enable **Storage** if you want to use the documents feature.
5. Publish security rules that match the [Security Considerations](#security-considerations) section.

---

## Known Limitations and Roadmap

* Offline mode with sync (Room) and push notifications (Firebase Cloud Messaging) are still being finished.
* Document upload is being fixed.
* Not every screen is translated into all four languages yet. Translations should be reviewed by fluent speakers.
* Facebook sign-in needs the Meta app configuration to be completed.
* Application fees and an auto-filled "application pack" for each institution are planned.
* Learners submit their applications on each institution's own website. UniPath SA helps them prepare and track.

---

## Credits

* **Institution logos** remain the property of the respective institutions and are used only to identify them.
* **Intro and campus images:** <!-- TODO: list each image, its source link, author and licence (for example Wikimedia Commons, Unsplash or Pexels). -->
* **Libraries:** Firebase, Retrofit, OkHttp, Gson, Glide and Material Components, used under their open-source licences.

---

## AI Use Statement

AI tools were used to support this project. All AI output was reviewed, tested on a physical Android phone and edited before use.

| Tool | How it was used |
| --- | --- |
| **Claude** (Anthropic) | Project planning, step-by-step Git and GitHub guidance and explaining Logcat errors |


**Verification:** every generated change was built and tested in Android Studio and on a physical phone. Translations are first drafts that must be checked by a fluent speaker. The team can explain the code in the demo video.

* Anthropic (2026) *Claude* [Large language model]. Available at: https://claude.ai [Accessed 5 November 2026].

---

## References

* Firebase (n.d.) *Firebase Authentication for Android*. Available at: https://firebase.google.com/docs/auth/android/start [Accessed 5 November 2026].
* Firebase (n.d.) *Cloud Firestore*. Available at: https://firebase.google.com/docs/firestore [Accessed 5 November 2026].
* Square (n.d.) *Retrofit*. Available at: https://square.github.io/retrofit/ [Accessed 5 November 2026].
* Bump Technologies (n.d.) *Glide*. Available at: https://bumptech.github.io/glide/ [Accessed 5 November 2026].
* GitHub (2025) *Automated build Android app with GitHub Action*. Available at: https://github.com/marketplace/actions/automated-build-android-app-with-github-action [Accessed 3 November 2025].


## Contributors

* Alexis Maphosa
* Ayanda MMagocoba

---

## Conclusion

UniPath SA gives South African high-school learners one place to understand their options after matric. It combines a learner profile, APS calculation, course and institution exploration, bursary discovery, saved items and application tracking, supported by Firebase cloud services, a REST API integration, input validation and professional version control with GitHub and GitHub Actions.
