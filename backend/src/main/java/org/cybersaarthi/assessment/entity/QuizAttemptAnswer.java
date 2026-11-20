package org.cybersaarthi.assessment.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempt_answers")
public class QuizAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "selected_option_key", nullable = false, length = 10)
    private String selectedOptionKey;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    public QuizAttemptAnswer() {
    }

    public QuizAttemptAnswer(UUID questionId, String selectedOptionKey, boolean isCorrect) {
        this.questionId = questionId;
        this.selectedOptionKey = selectedOptionKey;
        this.isCorrect = isCorrect;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QuizAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(QuizAttempt attempt) {
        this.attempt = attempt;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOptionKey() {
        return selectedOptionKey;
    }

    public void setSelectedOptionKey(String selectedOptionKey) {
        this.selectedOptionKey = selectedOptionKey;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
