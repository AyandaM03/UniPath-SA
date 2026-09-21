# 🎓 UniPathSA

### Your Path. Your Future. Your Choice.

UniPathSA is an Android application designed to assist South African high-school learners with the transition from secondary school to higher education. The application provides learners with tools and information to help them explore study opportunities, understand their academic performance, discover institutions and courses, and keep track of their higher-education applications.

The application was developed as part of a software development project with a focus on mobile application development, cloud services, database integration, user experience, version control and automated software testing.

---

## Project Overview

Choosing what to study after high school can be challenging for learners. There are many universities, colleges, courses, admission requirements and application deadlines to consider.

UniPathSA brings several of these functions together within a single Android application. Instead of requiring learners to manage information across multiple platforms, the application provides a centralised environment where they can manage their academic information and explore potential study opportunities.

The application makes use of cloud-based services to securely manage user accounts and store application data.

---

## Purpose of the Application

The main purpose of UniPathSA is to provide South African high-school learners with a user-friendly platform that supports their preparation for higher education.

The application aims to help learners:

* Create and manage a personal account.
* Maintain their learner profile.
* Record their subjects and marks.
* Calculate their Admission Point Score (APS).
* Explore courses and programmes.
* Explore higher-education institutions.
* Save courses and institutions for later.
* Manage applications.
* Keep track of important application information.
* Manage relevant documents.
* Receive useful notifications and reminders.
* Track progress towards their higher-education goals.

---

#  Target Users

The primary target users of UniPathSA are **South African high-school learners**, particularly learners in Grades 10–12 and matriculants preparing for further education.

The application is designed with learners in mind by providing:

* Simple navigation.
* Clear information.
* Easy-to-understand interfaces.
* Form validation.
* Accessible actions.
* Organised information.
* A mobile-friendly design.

---

# Main Features

## User Registration and Login

Users can create an account using their:

* Full name
* Email address
* Password
* Grade

Authentication is handled using **Firebase Authentication**.

The application does not store user passwords directly in the Firestore database. Authentication credentials are managed through Firebase Authentication.

---

## Learner Profile

Each registered learner has a personal profile associated with their Firebase Authentication account.

Profile information can include:

* Full name
* Email address
* Grade
* School information
* Province
* Account information

User-specific information is associated with the authenticated user's unique Firebase UID.

---

##  Subjects and Marks

Learners can manage information about their school subjects and academic marks.

The subject functionality allows the application to store information such as:

* Subject name
* Percentage/mark
* Date of update

This information can be used by the application when calculating the learner's APS.

---

## APS Calculator

UniPathSA includes an APS calculation feature designed to help learners understand their academic points.

The calculator uses the learner's subject marks to calculate their APS according to the application's configured APS conversion logic.

The calculator also validates user input to prevent invalid values from causing errors.

For example, marks outside the expected range can be rejected rather than being processed incorrectly.

---

## Course Explorer

The Course Explorer allows learners to explore available study programmes.

Course information is stored in the Firestore database and may include information such as:

* Course name
* Institution
* Category
* Duration
* APS requirement

Examples of study areas include:

* Information Technology
* Engineering
* Commerce
* Law
* Business
* Other higher-education fields

The application retrieves course information from the online database rather than relying entirely on hard-coded information within the Android application.

---

## Institution Explorer

Learners can explore information about higher-education institutions.

Institution information stored in Firestore can include:

* Institution name
* Description
* Province
* Institution type
* Website
* Application information

This allows learners to investigate institutions from within the application.

---

## Saved Items

Learners can save useful courses or institutions for easier access later.

Saved information is associated with the individual learner's account so that different users can maintain their own saved items.

---

## Application Tracking

The application includes functionality for learners to keep track of their higher-education applications.

Application information can include:

* Institution
* Course
* Application status
* Deadline
* Notes

This allows learners to organise their application process within one application.

---

## Document Management

UniPathSA is designed to support the management of documents that may be required during the application process.

Examples can include:

* Identification documents
* School results
* Certificates
* Other application-related documents

Actual files can be stored using Firebase Storage, while information about the files can be maintained in Firestore.

---

## Notifications

The application includes notification functionality for information that may be relevant to the learner.

Notifications can be associated with information such as:

* Application updates
* Deadlines
* Reminders
* Other important events

Where configured, cloud notification services can be used to support notifications.

---

# User Interface and Design Considerations

The UniPathSA interface was designed around the needs of high-school learners.

The design aims to provide a balance between a modern appearance and ease of use.

### Design principles include:

**Simplicity**

The interface avoids unnecessary complexity and presents important information in a clear way.

**Consistency**

Buttons, cards, text styles, navigation and spacing follow consistent design patterns throughout the application.

**Readability**

Text and information are presented using clear typography and appropriate spacing.

**Visual hierarchy**

Important actions and information are visually prioritised so that users can understand what to do next.

**Accessibility**

Forms and navigation are designed to be straightforward for users who may not have extensive technical experience.

**Responsive interaction**

The application provides feedback when users perform actions such as logging in, registering, saving information or entering invalid data.

---

# Technologies Used

| Technology              | Purpose                                     |
| ----------------------- | ------------------------------------------- |
| Kotlin                  | Primary programming language                |
| Android Studio          | Android application development             |
| XML                     | Android user-interface layouts              |
| Firebase Authentication | User authentication                         |
| Firebase Firestore      | Cloud database                              |
| Firebase Storage        | File/document storage                       |
| Git                     | Version control                             |
| GitHub                  | Source-code hosting                         |
| GitHub Actions          | Continuous integration and automated builds |
| Gradle                  | Project build and dependency management     |

---

# Firebase Architecture

UniPathSA uses Firebase as its cloud backend.

The main Firebase services used by the application are:

```text
                    UniPathSA
                       |
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
   Firebase        Firestore       Firebase
 Authentication    Database        Storage
        │              │              │
        ▼              ▼              ▼
    User Login      App Data       Documents
    & Accounts
```

---

# Firebase Authentication

Firebase Authentication is responsible for managing user authentication.

When a learner creates an account, Firebase Authentication manages the authentication credentials.

The application obtains the authenticated user's unique ID and uses this ID when associating user-specific information with Firestore.

This creates a separation between authentication credentials and application profile information.

### Authentication data

Firebase Authentication manages information such as:

* User UID
* Email address
* Authentication provider
* Account creation information
* Sign-in information

Passwords are not stored directly in the application's Firestore documents.

---

# Firestore Database

Cloud Firestore is used to store application data.

The database contains shared information as well as user-specific information.

The main collections include:

```text
Firestore
│
├── courses
│
├── institutions
│
└── users
      │
      └── {userId}
            │
            ├── subjects
            ├── applications
            ├── savedItems
            ├── documents
            ├── notifications
            └── achievements
```

### Courses

The `courses` collection stores course information used by the Course Explorer.

Example fields include:

```text
name
university
category
duration
apsRequired
```

### Institutions

The `institutions` collection stores information about higher-education institutions.

Example fields include:

```text
name
description
province
type
website
freeToApply
imageUrl
```

### Users

The `users` collection stores learner profile information.

Example fields include:

```text
fullName
email
grade
createdAt
```

Additional learner-specific information can be organised using subcollections.

---

# API and Cloud Services

The application communicates with cloud-based services to retrieve and store information.

Firebase provides cloud-based APIs that allow the Android application to interact with authentication, Firestore and storage services.

The application therefore does not rely solely on locally stored information. Data can be retrieved from the online backend when required.

Where external API services are configured, they should be documented here together with their purpose and the type of data returned.

---

# Input Validation and Error Handling

A major consideration during development was ensuring that invalid user input does not cause the application to crash.

The application validates information before processing it.

Examples include:

### Registration validation

The application checks that:

* Full name has been entered.
* Email has been entered.
* Password has been entered.
* Password meets the minimum length.
* A grade has been selected.

### APS validation

The application checks that marks are valid before calculating the APS.

For example, invalid values such as:

```text
-10
150
ABC
```

should not be accepted as valid marks.

### Authentication errors

The application handles authentication failures and displays an appropriate message instead of terminating unexpectedly.

### Database errors

If a Firestore operation fails, the application provides feedback to the user rather than silently failing or crashing.

---

#  Testing

Testing was conducted to ensure that the main functionality of the application operates correctly.

Testing focuses on both successful and unsuccessful user interactions.

## Functional testing

Examples include:

| Feature      | Test                                 |
| ------------ | ------------------------------------ |
| Registration | Create a valid account               |
| Registration | Submit empty fields                  |
| Registration | Enter an invalid password            |
| Login        | Login using valid credentials        |
| Login        | Login using invalid credentials      |
| APS          | Calculate APS using valid marks      |
| APS          | Enter invalid marks                  |
| Courses      | Retrieve courses from Firestore      |
| Institutions | Retrieve institutions from Firestore |
| Applications | Create an application                |
| Saved Items  | Save an item                         |
| Profile      | Retrieve user information            |

---

# Automated Testing

Automated tests are used to test important application functionality.

The purpose of automated testing is to identify errors early and ensure that changes made to the project do not unintentionally break existing functionality.

Tests are executed using Gradle and can also be executed through GitHub Actions.

---

# Version Control with GitHub

GitHub is used as the version-control and source-code hosting platform for UniPathSA.

The repository contains the Android Studio project and supporting documentation.

Git was used throughout development to track changes to the application.

Development changes were committed regularly rather than only creating a single commit at the end of the project.

Examples of development commits include:

```text
Initial Android project setup
Add Firebase Authentication
Implement user registration
Connect Firestore user profiles
Add course explorer
Connect institutions to Firestore
Implement APS calculator
Add application tracking
Improve input validation
Improve application interface
Add automated tests
Configure GitHub Actions
Update project documentation
```

This provides a development history and makes it possible to identify changes made throughout the project.

---

# GitHub Actions

GitHub Actions is used to automate testing and building of the Android application.


# Continuous Integration

Continuous Integration allows changes to be automatically tested after being pushed to GitHub.

For UniPathSA, the GitHub Actions workflow is intended to:

1. Retrieve the latest project files.
2. Configure the required development environment.
3. Run automated tests.
4. Build the Android application.
5. Report whether the workflow succeeded or failed.

This helps identify problems earlier during development.

---

# Application Screenshots

Screenshots of the completed application are included below to demonstrate the user interface and main functionality.

## Login

here

## Registration

here

## Dashboard

here

## Profile

here

## Subjects and Marks

here

## APS Calculator

here

## Course Explorer

here

## Institutions

here

## Application Tracking

here


---

# Project Structure

The project follows the standard Android Studio project structure.

```text
UniPathSA
│
├── .github
│   └── workflows
│       └── build.yml
│
├── app
│   └── src
│       └── main
│           ├── java
│           │   └── com.example.unipathsa
│           │
│           └── res
│               ├── drawable
│               ├── layout
│               ├── mipmap
│               └── values
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

#  Data and Security Considerations

Security was considered when designing the application's cloud architecture.

User passwords are handled through Firebase Authentication rather than being stored directly in Firestore.

User-specific application information is associated with the authenticated user's UID.

Firestore security rules should be configured so that users can only access information that they are authorised to access.

Sensitive information should not be hard-coded into the Android application or committed to the GitHub repository.

---




# Future Improvements

Possible future improvements to UniPathSA include:

* More comprehensive course information.
* Additional institution information.
* Improved application deadline tracking.
* More advanced course recommendations.
* Additional bursary information.
* Push notifications for important deadlines.
* Expanded document management.
* Additional automated tests.
* Improved accessibility features.
* More detailed learner progress tracking.
* Integration with additional verified external services.

---

# Educational Value

The development of UniPathSA provided practical experience in several areas of software development, including:

* Android application development.
* Kotlin programming.
* XML interface development.
* Cloud database development.
* Authentication.
* API and cloud-service integration.
* Input validation.
* Software testing.
* Git version control.
* GitHub repository management.
* Continuous Integration.
* GitHub Actions.
* Technical documentation.

The project also provided experience in considering the needs of a specific user group when designing a software solution.

---

# Project Status

**Project:** UniPathSA
**Platform:** Android
**Language:** Kotlin
**Database:** Firebase Cloud Firestore
**Authentication:** Firebase Authentication
**Cloud Storage:** Firebase Storage
**Version Control:** Git / GitHub
**CI/CD:** GitHub Actions

---


# Conclusion

UniPathSA provides a centralised mobile platform designed to support South African high-school learners as they prepare for higher education. The application combines learner information, academic results, APS calculation, course exploration, institution information and application management into one platform.

The project also demonstrates the use of cloud-based authentication and database services, input validation, automated testing, GitHub version control and GitHub Actions.

Through the development of UniPathSA, the project demonstrates how mobile application development, cloud computing, database management, software testing and version control can be combined to create a functional software solution.

---



If you want, I can next give you the **exact GitHub-ready version with badges, a professional UniPathSA header, table of contents, screenshots section, Firebase architecture diagram, and GitHub Actions badge** so it looks like a polished real software project rather than a plain assignment README.

