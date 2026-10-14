# Placement Preparation and Quiz System

A Java-based mini project designed to help students prepare for placement interviews and technical assessments. The system presents multiple-choice questions in different topics, evaluates answers instantly, explains the correct answer, and stores the student's quiz history during the current session.

The project is built using Core Java and the JDK HTTP server, keeping it lightweight, easy to run, and suitable for learning object-oriented programming concepts in a practical way.

## Overview

The Placement Preparation and Quiz System is a simple educational application that helps students practice important placement-related topics such as:

- Core Java
- Object-Oriented Programming (OOP)
- Basic technical interview concepts

It provides a user-friendly quiz experience where students can:

- choose a quiz topic,
- answer MCQs,
- receive instant feedback,
- understand the explanation for the correct answer,
- review their previous quiz results.

This project is ideal for beginners who want to build a meaningful Java application that demonstrates real-world software design and problem-solving.

## Features

- Topic-based quiz selection
- Multiple-choice question system
- Instant answer checking
- Correct answer explanations
- Score and percentage calculation
- Quiz history tracking
- Console application support
- Web-based interface using Java HTTP server
- Clean object-oriented design

## Technology Stack

- Java
- JDK 17 or newer
- Java HTTP Server (`jdk.httpserver`)
- HTML, CSS, JavaScript for frontend
- Object-Oriented Programming principles

## Project Structure

```text
Placement-Preparation-and-Quiz-System/
├── .gitignore
├── .vscode/
├── README.md
├── src/
│   └── placementprep/
│       ├── Main.java
│       ├── model/
│       │   ├── Question.java
│       │   ├── QuizResult.java
│       │   ├── Student.java
│       │   └── User.java
│       ├── quiz/
│       │   └── QuizSession.java
│       ├── service/
│       │   └── QuizCatalog.java
│       └── web/
│           └── PlacementPrepServer.java
└── web/
    ├── index.html
    ├── app.js
    └── styles.css
```

## How It Works

When the application starts, the student enters their name and chooses a quiz topic. The system loads the relevant question set from the quiz catalog and starts a quiz session.

For each question:

- the question is displayed,
- the user enters the correct option number,
- the system checks the response,
- the result is displayed with explanation,
- the final score is calculated.

At the end, the result is saved in memory and can be viewed in the quiz history section.

## Object-Oriented Concepts Demonstrated

- Encapsulation: model classes keep fields private and expose behavior through methods.
- Abstraction: `User` is an abstract type and `Quiz` defines the contract for quiz behavior.
- Inheritance: `Student` extends `User`.
- Polymorphism: `QuizSession` implements the quiz interface and behaves according to the current topic.
- Composition: a quiz is made of multiple questions, and each student stores quiz results.

## Requirements

- JDK 17 or newer

## Run the web application

Open a terminal in the project root and compile the Java sources:

```bash
javac --add-modules jdk.httpserver -d out \
  src/placementprep/Main.java \
  src/placementprep/model/*.java \
  src/placementprep/quiz/*.java \
  src/placementprep/service/*.java \
  src/placementprep/web/*.java
```

Then run the server:

```bash
java --add-modules jdk.httpserver -cp out placementprep.web.PlacementPrepServer
```

Open `http://localhost:8080` in your browser.

Keep the server terminal open while using the app. Press `Ctrl+C` to stop the server.

To use another port, pass it after the class name, for example:

```bash
java --add-modules jdk.httpserver -cp out placementprep.web.PlacementPrepServer 8081
```

The browser saves the display name locally. Quiz history is kept in server memory and is cleared when the server stops. Quiz answers are checked on the Java server.

## Console version

The original console version is still available:

```bash
java -cp out placementprep.Main
```

## Why This Project is Useful

This project is valuable for students because it demonstrates how to build a complete small application using Java without external frameworks. It combines several important software development ideas:

- user interaction,
- data modeling,
- validation,
- score tracking,
- OOP design,
- simple web development.

It is a practical example of how Java can be used to create a useful educational product.

## Future Improvements

The project can be extended in many useful ways:

- save quiz results in a database,
- add login and signup,
- add more quiz categories,
- include timed quizzes,
- create a dashboard for student performance,
- add admin features to manage questions,
- connect with a real web frontend framework.

## Screenshot / Demo Section

Add screenshots here as the project develops:

```text
[Project Homepage Screenshot]
[Quiz Page Screenshot]
[Result History Screenshot]
```

## Conclusion

The Placement Preparation and Quiz System is a beginner-friendly Java project that applies real-world programming concepts to solve an educational problem. It is simple, effective, and demonstrates how Java can be used to build useful daily-life applications.

This project is not just a quiz app — it is a practical example of software design, user interaction, and evaluation logic in an easy-to-understand format.
