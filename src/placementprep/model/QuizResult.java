package placementprep.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QuizResult {
    private final String topic;
    private final int score;
    private final int totalQuestions;
    private final LocalDateTime completedAt;

    public QuizResult(String topic, int score, int totalQuestions) {
        this.topic = topic;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.completedAt = LocalDateTime.now();
    }

    public double getPercentage() {
        return totalQuestions == 0 ? 0 : (score * 100.0) / totalQuestions;
    }

    public String getTopic() {
        return topic;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    @Override
    public String toString() {
        String timestamp = completedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return String.format(
                "%s | %d/%d correct (%.0f%%) | %s",
                topic, score, totalQuestions, getPercentage(), timestamp
        );
    }
}
