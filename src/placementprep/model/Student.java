package placementprep.model;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private final List<QuizResult> results = new ArrayList<>();

    public Student(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Student";
    }

    public void recordResult(QuizResult result) {
        results.add(result);
    }

    public List<QuizResult> getResults() {
        return List.copyOf(results);
    }

    public void showResults() {
        if (results.isEmpty()) {
            System.out.println("\nNo quiz attempts yet. Take a quiz to see your results here.");
            return;
        }

        System.out.println("\n=== Quiz History ===");
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, results.get(i));
        }
    }
}
