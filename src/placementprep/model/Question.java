package placementprep.model;

import java.util.List;

public class Question {
    private final String prompt;
    private final List<String> options;
    private final int correctOption;
    private final String explanation;

    public Question(String prompt, List<String> options, int correctOption, String explanation) {
        if (options.size() < 2 || correctOption < 1 || correctOption > options.size()) {
            throw new IllegalArgumentException("Question must have at least two options and a valid answer.");
        }
        this.prompt = prompt;
        this.options = List.copyOf(options);
        this.correctOption = correctOption;
        this.explanation = explanation;
    }

    public String getPrompt() {
        return prompt;
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean isCorrect(int selectedOption) {
        return selectedOption == correctOption;
    }

    public String getCorrectAnswer() {
        return options.get(correctOption - 1);
    }

    public String getExplanation() {
        return explanation;
    }
}
