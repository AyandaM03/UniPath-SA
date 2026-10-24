# 🎓 UniPathSA

# YouTube Link: https://youtube.com/shorts/2fFexSMggRc?si=NagNT32Wt2uSNeNo

### Your Path. Your Future. Your Choice.

UniPathSA is an Android application designed to assist South African high-school learners with the transition from secondary school to higher education. The application provides learners with tools and information to help them calculate their academic points, explore institutions, courses and bursaries, and manage their personal profile — all in one place.

The application was developed as part of a software development project with a focus on mobile application development, cloud services, database integration, REST API integration, user experience and version control.

---

## Project Overview

Choosing what to study after high school can be challenging for learners. There are many universities, colleges, courses, admission requirements, bursaries and deadlines to consider.

UniPathSA brings several of these functions together within a single Android application. Instead of requiring learners to manage information across multiple platforms, the application provides a centralised environment where they can calculate their APS, explore study opportunities and manage their profile.

The application makes use of cloud-based services (Firebase) to securely manage user accounts and store application data, and communicates directly with a REST API to retrieve course-matching data.

---

## Purpose of the Application

The main purpose of UniPathSA is to provide South African high-school learners with a user-friendly platform that supports their preparation for higher education.

The application aims to help learners:

* Create and manage a personal account.
* Maintain their learner profile (phone number, school).
* Enter their subject marks and calculate their Admission Point Score (APS).
* See which courses they qualify for, based on their calculated APS.
* Explore courses and programmes across categories (IT, Commerce, Engineering, Law).
* Explore higher-education institutions (universities and colleges, public and private).
* Explore available bursaries.
* Save institutions to a personal favourites list.
* Customise their experience with Dark Mode.

---

## Target Users

The primary target users of UniPathSA are **South African high-school learners**, particularly learners in Grades 10–12 and matriculants preparing for further education.

The application is designed with learners in mind by providing:

* Simple navigation via a bottom navigation bar.
* Clear information, presented in cards and lists.
* Easy-to-understand interfaces.
* Form validation on sign up, login and the APS calculator.
* A mobile-friendly, consistent design across light and dark mode.

---

## Main Features

### User Registration and Login

Users can create an account using their:

* Full name
* Email address
* Password
* Grade

Users can also sign in with an existing **Google account**.

Authentication is handled using **Firebase Authentication**. The application does not store user passwords directly — authentication credentials are managed entirely through Firebase Authentication.

Users can also request a password reset email from the login screen.
<img width="1200" height="2600" alt="3880" src="https://github.com/user-attachments/assets/6857eb7f-3908-4e7d-af58-61ad746faf7d" />
<img width="1200" height="2600" alt="3883" src="https://github.com/user-attachments/assets/4c8af974-c018-4764-991f-ac8cd2ce408a" />

---

### Learner Profile

Each registered learner has a personal profile associated with their Firebase Authentication account, displaying their name and email automatically.

Learners can also enter and edit:

* Phone number
* School / grade information

This information is saved to Firestore under the authenticated user's unique Firebase UID, and reloads automatically the next time the profile is opened.
<img width="1200" height="2600" alt="3884" src="https://github.com/user-attachments/assets/302eae0a-8523-40e2-a0db-5fb04c05c5db" />

---

### APS Calculator

UniPathSA includes an APS calculation feature designed to help learners understand their academic points.

* Learners can dynamically add or remove any number of subjects.
* Each subject's percentage mark is converted into APS points using the standard South African Achievement Level Guide (7 points for 80–100%, down to 1 point for 0–29%).
* The calculator validates input, rejecting marks outside the 0–100 range.
* Once calculated, the app calls a **REST API** (Firestore's REST endpoint, accessed via Retrofit — not the Firestore SDK) to fetch all available courses, and filters them to show only the courses the learner's score qualifies them for.
<img width="1200" height="2600" alt="3885" src="https://github.com/user-attachments/assets/9b5ef445-9a19-42fb-babc-60e59c2e6a02" />

---

### Explore — Courses & Bursaries

The Explore screen lets learners browse two categories of opportunities:

**Courses** — university and college programmes, filterable by category (IT, Commerce, Engineering, Law), each showing the offering institution, APS requirement and duration.

**Bursaries** — funding opportunities, filterable by the same categories, showing the provider, amount covered and closing date.

Course and bursary information is stored in Firestore rather than hard-coded into the app. Tapping any course or bursary opens a details screen with a full description and a button linking to the provider's official website.
<img width="1200" height="2600" alt="3888" src="https://github.com/user-attachments/assets/65b565cd-a7d0-48d6-97e6-61d906e14128" />
<img width="1200" height="2600" alt="3887" src="https://github.com/user-attachments/assets/89c47b73-6ee8-4ae7-94e7-6ead8cb94ed4" />
<img width="1200" height="2600" alt="3892" src="https://github.com/user-attachments/assets/cd4e2d91-2e1a-447f-9177-9fc1aa272e00" />
<img width="1200" height="2600" alt="3893" src="https://github.com/user-attachments/assets/fcd45f02-77a0-4521-8249-c901dd9730d2" />

---

### Institutions

Learners can explore Universities and Colleges (public and private) across South Africa.

* Search by name or province.
* Filter between "Universities" and "Colleges".
* Tap any institution to view a full details screen, including a description and a link to the institution's official website.
* Save institutions as favourites (tap the star icon) — favourites are stored per-user in Firestore and viewable from the Profile screen.
<img width="1200" height="2600" alt="3889" src="https://github.com/user-attachments/assets/81931e2e-7add-4ec9-978c-1926317a784a" />
<img width="1200" height="2600" alt="3890" src="https://github.com/user-attachments/assets/9f33f234-542c-4a36-b4d4-652690cb176b" />

---

### Settings

* **Dark Mode** — toggles the app's theme between light and dark, and the preference persists between sessions.
* **Notifications** — a preference toggle (the underlying notification-sending feature is a placeholder for now).
<img width="1200" height="2600" alt="3891" src="https://github.com/user-attachments/assets/edf1147c-e3dc-49af-8392-dda9ed838853" />

---

## User Interface and Design Considerations

The UniPathSA interface was designed around the needs of high-school learners, aiming for a balance between a modern appearance and ease of use.

<img width="1200" height="2600" alt="3882" src="https://github.com/user-attachments/assets/03934d31-c62d-4a38-995a-71b1b7d65811" />

**Design principles include:**

**Simplicity** — the interface avoids unnecessary complexity and presents important information clearly.

**Consistency** — buttons, cards, text styles, navigation and spacing follow consistent design patterns throughout the application, including a defined light/dark colour palette.

**Readability** — text and information are presented using clear typography and appropriate spacing, with colours chosen to remain legible in both light and dark mode.

**Visual hierarchy** — important actions (such as "Add Marks" and "Calculate APS") are visually prioritised so that users understand what to do next.

**Responsive interaction** — the application provides feedback when users perform actions such as logging in, registering, saving information or entering invalid data.

---

## Technologies Used

| Technology | Purpose |
| --- | --- |
| Kotlin | Primary programming language |
| Android Studio | Android application development |
| XML | Android user-interface layouts |
| Firebase Authentication | User authentication (Email/Password + Google Sign-In) |
| Firebase Firestore | Cloud database |
| Retrofit + Firestore REST API | Direct REST API integration for course matching |
| Glide | Image loading |
| Material Components | UI widgets (cards, chips, switches, bottom navigation) |
| Git | Version control |
| GitHub | Source-code hosting |
| Gradle | Project build and dependency management |

---

## Firebase Architecture

UniPathSA uses Firebase as its cloud backend.

```text
                    UniPathSA
                       |
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
   Firebase                       Firestore
 Authentication                   Database
        │                             │
        ▼                             ▼
    User Login                   App Data
    & Accounts          (institutions, courses,
                          bursaries, user profiles
                          and favourites)
```

---

## Firebase Authentication

Firebase Authentication is responsible for managing user authentication.

When a learner creates an account, Firebase Authentication manages the authentication credentials. The application obtains the authenticated user's unique ID (UID) and uses this ID when associating user-specific information (profile details, favourites) with Firestore.

This creates a separation between authentication credentials and application profile information — passwords are never stored directly in the application's Firestore documents.

---

## Firestore Database

Cloud Firestore is used to store application data. The database contains shared information as well as user-specific information.

```text
Firestore
│
├── institutions
│
├── courses
│
├── bursaries
│
└── users
      │
      └── {userId}
            │
            ├── phone, school       (profile fields)
            │
            └── favourites
                  │
                  └── {institutionId}
```

### institutions
```text
name
province
type            ("University" or "College")
description
imageUrl
website
freeToApply
```

### courses
```text
name
university
apsRequired
duration
category
website
description
```

### bursaries
```text
name
provider
amount
closingDate
category
website
description
```

### users
```text
phone
school
```
with a `favourites` subcollection containing the full institution documents the user has saved.

---

## REST API Integration

In addition to using the Firebase Firestore SDK for most of the app's data (Institutions, Explore, user profile), the **APS Calculator screen communicates directly with Firestore's REST API** using Retrofit, rather than the SDK.

**Endpoint used:**

The response (Firestore's structured JSON format) is parsed into the app's `Course` model, and the results are filtered client-side to show only courses matching the student's calculated APS score. This satisfies the project requirement of creating/using a REST API and integrating it meaningfully into the app, separate from the Firebase SDK usage elsewhere.

---

## Input Validation and Error Handling

A major consideration during development was ensuring that invalid user input does not cause the application to crash.

**Registration validation** — the application checks that full name, email and password have been entered, that the password meets the minimum length, and that a grade has been selected.

**APS validation** — the calculator only accepts marks between 0 and 100; anything outside this range, or non-numeric input, is ignored rather than crashing the calculation.

**Authentication errors** — login/sign-up failures (wrong password, existing email, weak password, etc.) are caught and shown to the user as a message rather than crashing the app.

**Network/database errors** — if a Firestore or REST API call fails (e.g. no internet), the application shows a message to the user instead of failing silently or crashing.

---

## Testing

Testing was conducted to ensure that the main functionality of the application operates correctly, covering both successful and unsuccessful user interactions.

| Feature | Test |
| --- | --- |
| Registration | Create a valid account |
| Registration | Submit empty fields |
| Registration | Enter a password under 6 characters |
| Login | Login using valid credentials |
| Login | Login using invalid credentials |
| Login | Google Sign-In flow |
| APS Calculator | Calculate APS using valid marks |
| APS Calculator | Enter invalid marks (out of range / non-numeric) |
| APS Calculator | Confirm matching courses load via REST API |
| Institutions | Retrieve and filter institutions from Firestore |
| Institutions | Save and remove a favourite |
| Explore | Filter courses/bursaries by category |
| Profile | Save and reload phone/school details |
| Settings | Toggle Dark Mode and confirm it persists |

---

## Version Control with GitHub

GitHub was used as the version-control and source-code hosting platform for UniPathSA throughout development, with changes committed regularly by both team members using feature branches merged into `main` (e.g. `nav`, `bursaries`, `course-and-bursary-details`).

Examples of development milestones tracked through commits include:

```text
Initial Android project setup
Add Firebase Authentication (Email/Password + Google Sign-In)
Implement Splash, Login and Sign Up screens
Add bottom navigation
Connect Institutions screen to Firestore
Implement APS Calculator with REST API course matching
Add Explore screen (Courses and Bursaries tabs)
Add Course and Bursary details screens
Add Settings screen with Dark Mode
Add editable Profile fields and Saved Institutions
Improve input validation and dark mode styling
```

---

## Project Structure

```text
UniPathSA
│
├── app
│   └── src
│       └── main
│           ├── java
│           │   └── com.example.unipathsa
│           │       ├── LoginActivity.kt / SignUpActivity.kt / SplashActivity.kt
│           │       ├── HomeActivity.kt
│           │       ├── InstitutionsActivity.kt / InstitutionDetailsActivity.kt
│           │       ├── ExploreActivity.kt / CourseDetailsActivity.kt / BursaryDetailsActivity.kt
│           │       ├── ApsCalculatorActivity.kt
│           │       ├── ProfileActivity.kt / SettingsActivity.kt / SavedActivity.kt
│           │       ├── BottomNavHelper.kt
│           │       ├── RetrofitClient.kt / FirestoreApi.kt / FirestoreModels.kt
│           │       ├── Institution.kt / Course.kt / Bursary.kt
│           │       └── *Adapter.kt
│           │
│           └── res
│               ├── drawable
│               ├── layout
│               ├── menu
│               ├── mipmap
│               ├── values
│               └── values-night
│
├── gradle
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

---

## Data and Security Considerations

Security was considered when designing the application's cloud architecture.

* User passwords are handled entirely through Firebase Authentication rather than being stored in Firestore.
* User-specific information (profile details, favourites) is associated with the authenticated user's UID.
* Firestore is currently running in test mode for development; production security rules restricting access to a user's own data would be a required next step before any real deployment.
* `google-services.json` (containing Firebase API keys) is excluded from the repository via `.gitignore`.

---

## Setup

1. Clone the repository:
```bash
   git clone https://github.com/AyandaM03/UniPath-SA.git
```
2. Open the project in **Android Studio**.
3. Add your own `google-services.json` file to the `app/` directory (not included in this repo — request it from a project member, or set up your own Firebase project).
4. Sync Gradle and run.

### Firebase Setup (if starting fresh)
1. Create a Firebase project.
2. Enable **Authentication** → Email/Password and Google sign-in providers.
3. Enable **Cloud Firestore** (test mode for development).
4. Create three collections: `institutions`, `courses`, `bursaries` (see data model above).

---

## Future Improvements

Possible future improvements to UniPathSA include:

* Application/deadline tracking for institutions a learner has applied to.
* Document upload and management (e.g. via Firebase Storage) for ID and results.
* Push notifications for bursary and application deadlines.
* More comprehensive course and institution data.
* Automated UI testing.
* Production-ready Firestore security rules.

---

## Educational Value

The development of UniPathSA provided practical experience in several areas of software development, including:

* Android application development and Kotlin programming.
* XML interface development and Material Design.
* Cloud database development with Firebase Firestore.
* Authentication (Firebase Auth + Google Sign-In).
* REST API integration alongside SDK usage.
* Input validation and error handling.
* Git version control and collaborative branching/merging.
* Technical documentation.

---

## Project Status

**Project:** UniPathSA
**Platform:** Android
**Language:** Kotlin
**Database:** Firebase Cloud Firestore
**Authentication:** Firebase Authentication
**Version Control:** Git / GitHub

---

## Contributors

- Alexis Maphosa
- Ayanda MMagocoba
---

## Conclusion

UniPathSA provides a centralised mobile platform designed to support South African high-school learners as they prepare for higher education. The application combines learner profile information, APS calculation, course and institution exploration, bursary discovery and a favourites system into one platform.

The project also demonstrates the use of cloud-based authentication and database services, direct REST API integration, input validation and GitHub version control.

# AI Used

**https://www.anthropic.com/**
**https://claude.ai/login**
**https://narrateai.app/app/demo**

