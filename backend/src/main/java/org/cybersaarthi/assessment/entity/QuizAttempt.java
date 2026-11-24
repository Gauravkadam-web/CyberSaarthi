package org.cybersaarthi.assessment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Column(nullable = false)
    private int score;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuizAttemptAnswer> answers = new ArrayList<>();

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt = Instant.now();

    public QuizAttempt() {
    }

    public QuizAttempt(UUID userId, UUID topicId, int score, int maxScore) {
        this.userId = userId;
        this.topicId = topicId;
        this.score = score;
        this.maxScore = maxScore;
        this.submittedAt = Instant.now();
    }

    public void addAnswer(QuizAttemptAnswer answer) {
        answers.add(answer);
        answer.setAttempt(this);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getTopicId() {
        return topicId;
    }

    public void setTopicId(UUID topicId) {
        this.topicId = topicId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public List<QuizAttemptAnswer> getAnswers() {
        return answers;
    }

    public double getScorePercentage() {
        return maxScore > 0 ? ((double) score / maxScore) * 100.0 : 0.0;
    }

    public void setAnswers(List<QuizAttemptAnswer> answers) {
        this.answers = answers;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
