package placementprep;

import placementprep.model.Question;
import placementprep.model.Student;
import placementprep.quiz.Quiz;
import placementprep.quiz.QuizSession;
import placementprep.service.QuizCatalog;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== Placement Preparation and Quiz System ===");
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            while (name.isEmpty()) {
                System.out.print("Name cannot be empty. Enter your name: ");
                name = scanner.nextLine().trim();
            }

            Student student = new Student(name);
            Map<String, List<Question>> quizzes = QuizCatalog.createQuizzes();
            boolean running = true;

            while (running) {
                System.out.println("\nWelcome, " + student.getName() + "!");
                System.out.println("1. Take a quiz");
                System.out.println("2. View quiz history");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");

                switch (scanner.nextLine().trim()) {
                    case "1" -> takeQuiz(scanner, student, quizzes);
                    case "2" -> student.showResults();
                    case "3" -> running = false;
                    default -> System.out.println("Please enter 1, 2, or 3.");
                }
            }

            System.out.println("Good luck with your placement preparation!");
        }
    }

    private static void takeQuiz(
            Scanner scanner,
            Student student,
            Map<String, List<Question>> quizzes
    ) {
        List<String> topics = List.copyOf(quizzes.keySet());
        System.out.println("\nChoose a quiz topic:");
        for (int i = 0; i < topics.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, topics.get(i));
        }
        System.out.println("0. Back to main menu");
        System.out.print("Choose a topic: ");

        String choice = scanner.nextLine().trim();
        if ("0".equals(choice)) {
            return;
        }

        int topicNumber;
        try {
            topicNumber = Integer.parseInt(choice);
        } catch (NumberFormatException exception) {
            System.out.println("Please enter a valid topic number.");
            return;
        }

        if (topicNumber < 1 || topicNumber > topics.size()) {
            System.out.println("That topic number is not available.");
            return;
        }

        String topic = topics.get(topicNumber - 1);
        Quiz quiz = new QuizSession(topic, quizzes.get(topic));
        student.recordResult(quiz.start(scanner));
    }
}
