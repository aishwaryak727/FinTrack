package com.fintrack.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity representing a financial transaction record in the FinTrack ledger.
 * Indexed by {@code user_id} and {@code created_at} for performant customer statement retrieval.
 */
@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_user_created", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_tx_user_id", columnList = "user_id")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default no-arg constructor required by JPA.
     */
    protected Transaction() {
    }

    /**
     * Convenience constructor to instantiate a new Transaction entity.
     *
     * @param userId      the unique identifier of the user who owns this transaction
     * @param amount      the monetary transaction value
     * @param description human-readable description or note for the transaction
     * @param type        the categorized transaction type
     * @param createdAt   the timestamp when this transaction occurred; defaults to now if null
     */
    public Transaction(
            Long userId,
            BigDecimal amount,
            String description,
            TransactionType type,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * JPA lifecycle hook ensuring creation timestamp is set before database insert.
     */
    @PrePersist
    protected void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * Returns the database surrogate primary key for this transaction.
     *
     * @return transaction ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the owner user ID.
     *
     * @return owner user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Updates the owner user ID.
     *
     * @param userId owner user ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the monetary amount of this transaction.
     *
     * @return amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Updates the transaction amount.
     *
     * @param amount new transaction amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Returns the narrative description for this transaction.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the narrative description.
     *
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the transaction categorization type.
     *
     * @return transaction type
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Updates the transaction categorization type.
     *
     * @param type new transaction type
     */
    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     * Returns the immutable creation timestamp for this transaction.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", createdAt=" + createdAt +
                '}';
    }
}
