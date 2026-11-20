package org.cybersaarthi.assessment.entity;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "quiz_options", uniqueConstraints = {
    @UniqueConstraint(name = "uq_question_option", columnNames = {"question_id", "option_key"})
})
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(name = "option_key", nullable = false, length = 10)
    private String optionKey;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 1;

    public QuizOption() {
    }

    public QuizOption(String optionKey, String optionText, int displayOrder) {
        this.optionKey = optionKey;
        this.optionText = optionText;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QuizQuestion getQuestion() {
        return question;
    }

    public void setQuestion(QuizQuestion question) {
        this.question = question;
    }

    public String getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(String optionKey) {
        this.optionKey = optionKey;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizOption that)) return false;
        return Objects.equals(optionKey, that.optionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionKey);
    }
}
