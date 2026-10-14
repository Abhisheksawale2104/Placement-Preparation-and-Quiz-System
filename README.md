# Placement Preparation and Quiz System

A web-based mini project built with Core Java and the JDK HTTP server, focused only on Core Java and object-oriented programming. Each subject has 20 placement-style multiple-choice questions with immediate scoring and explanations.

## Requirements

- JDK 17 or newer

## Run the web application

Open a terminal in the project root and compile the Java sources:

```text
javac --add-modules jdk.httpserver -d out src/placementprep/Main.java src/placementprep/model/*.java src/placementprep/quiz/*.java src/placementprep/service/*.java src/placementprep/web/*.java
java --add-modules jdk.httpserver -cp out placementprep.web.PlacementPrepServer
```

Open **http://localhost:8080** in your browser. Keep the server terminal open while using the app; press `Ctrl+C` to stop it. To use another port, pass it after the class name, for example `java --add-modules jdk.httpserver -cp out placementprep.web.PlacementPrepServer 8081`.

The browser saves your display name locally. Quiz history is kept in server memory and is cleared when the server stops. Quiz answers are checked on the Java server.

## Console version

The original console application is still available:

```text
java -cp out placementprep.Main
```

## OOP concepts demonstrated

- **Encapsulation:** model classes keep their fields private and expose behavior through methods.
- **Abstraction:** `User` is an abstract base class, and `Quiz` defines the contract for a quiz.
- **Inheritance:** `Student` extends `User`.
- **Polymorphism:** `QuizSession` implements `Quiz`, and the application starts quizzes through the `Quiz` interface.
- **Composition:** a quiz session contains questions, and a student keeps quiz results.
