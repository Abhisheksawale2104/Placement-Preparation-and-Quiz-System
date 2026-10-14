package placementprep.quiz;

import placementprep.model.Question;
import placementprep.model.QuizResult;

import java.util.List;
import java.util.Scanner;

public class QuizSession implements Quiz {
    private final String topic;
    private final List<Question> questions;

    public QuizSession(String topic, List<Question> questions) {
        this.topic = topic;
        this.questions = List.copyOf(questions);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public QuizResult evaluate(List<Integer> answers) {
        if (answers.size() != questions.size()) {
            throw new IllegalArgumentException("Submit one answer for every question.");
        }

        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            int answer = answers.get(i);
            if (answer < 1 || answer > questions.get(i).getOptions().size()) {
                throw new IllegalArgumentException("One or more answers are not valid.");
            }
            if (questions.get(i).isCorrect(answer)) {
                score++;
            }
        }
        return new QuizResult(topic, score, questions.size());
    }

    @Override
    public QuizResult start(Scanner scanner) {
        int score = 0;
        System.out.println("\n=== " + topic + " Quiz ===");
        System.out.println("Enter the number of your answer.\n");

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            System.out.printf("%d. %s%n", i + 1, question.getPrompt());
            for (int option = 0; option < question.getOptions().size(); option++) {
                System.out.printf("   %d. %s%n", option + 1, question.getOptions().get(option));
            }

            int answer = readAnswer(scanner, question.getOptions().size());
            if (question.isCorrect(answer)) {
                score++;
                System.out.println("Correct! " + question.getExplanation() + "\n");
            } else {
                System.out.println(
                        "Not quite. Correct answer: " + question.getCorrectAnswer() + ". "
                                + question.getExplanation() + "\n"
                );
            }
        }

        QuizResult result = new QuizResult(topic, score, questions.size());
        System.out.printf("Quiz complete: %s%n", result);
        return result;
    }

    private int readAnswer(Scanner scanner, int optionCount) {
        while (true) {
            System.out.print("Your answer: ");
            String input = scanner.nextLine().trim();
            try {
                int answer = Integer.parseInt(input);
                if (answer >= 1 && answer <= optionCount) {
                    return answer;
                }
            } catch (NumberFormatException ignored) {
                // Invalid input is handled by the retry message below.
            }
            System.out.println("Enter a number from 1 to " + optionCount + ".");
        }
    }
}
