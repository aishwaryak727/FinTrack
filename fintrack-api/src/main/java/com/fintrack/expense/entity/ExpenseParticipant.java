package com.fintrack.expense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity representing an individual participant and their designated monetary share
 * within a {@link SharedExpense}.
 */
@Entity
@Table(
        name = "expense_participants",
        indexes = {
                @Index(name = "idx_exp_part_user", columnList = "user_id"),
                @Index(name = "idx_exp_part_shared", columnList = "shared_expense_id")
        }
)
public class ExpenseParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_expense_id", nullable = false)
    private SharedExpense sharedExpense;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "share_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal shareAmount;

    /**
     * Default constructor required by JPA.
     */
    protected ExpenseParticipant() {
    }

    /**
     * Constructs a new participant with user ID and designated share amount.
     *
     * @param userId      the user ID of the participant
     * @param shareAmount the monetary share owed by this participant
     */
    public ExpenseParticipant(Long userId, BigDecimal shareAmount) {
        this.userId = userId;
        this.shareAmount = shareAmount;
    }

    /**
     * Constructs a new participant linked to a shared expense.
     *
     * @param sharedExpense the parent shared expense
     * @param userId        the user ID of the participant
     * @param shareAmount   the monetary share owed by this participant
     */
    public ExpenseParticipant(SharedExpense sharedExpense, Long userId, BigDecimal shareAmount) {
        this.sharedExpense = sharedExpense;
        this.userId = userId;
        this.shareAmount = shareAmount;
    }

    /**
     * Returns the primary key identifier.
     *
     * @return participant ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the associated shared expense.
     *
     * @return parent shared expense
     */
    public SharedExpense getSharedExpense() {
        return sharedExpense;
    }

    /**
     * Sets the associated shared expense.
     *
     * @param sharedExpense parent shared expense
     */
    public void setSharedExpense(SharedExpense sharedExpense) {
        this.sharedExpense = sharedExpense;
    }

    /**
     * Returns the participant's user ID.
     *
     * @return user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the participant's user ID.
     *
     * @param userId participant user ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the monetary share owed by this participant.
     *
     * @return share amount
     */
    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    /**
     * Sets the monetary share owed by this participant.
     *
     * @param shareAmount new share amount
     */
    public void setShareAmount(BigDecimal shareAmount) {
        this.shareAmount = shareAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseParticipant that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ExpenseParticipant{" +
                "id=" + id +
                ", userId=" + userId +
                ", shareAmount=" + shareAmount +
                '}';
    }
}

