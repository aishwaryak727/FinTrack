package com.fintrack.expense.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA entity representing a shared group expense in FinTrack.
 * Tracks the creator who paid the full amount upfront and the collection of participants
 * among whom the expense is divided.
 */
@Entity
@Table(
        name = "shared_expenses",
        indexes = {
                @Index(name = "idx_shared_exp_creator", columnList = "creator_id"),
                @Index(name = "idx_shared_exp_created", columnList = "created_at DESC")
        }
)
public class SharedExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false, length = 20)
    private SplitType splitType;

    @OneToMany(
            mappedBy = "sharedExpense",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ExpenseParticipant> participants = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA.
     */
    protected SharedExpense() {
    }

    /**
     * Constructs a new SharedExpense instance.
     *
     * @param creatorId   the ID of the user who paid the full expense initially
     * @param description brief narrative description of the expense
     * @param totalAmount the total monetary expense amount
     * @param splitType   the method used to divide the expense (EQUAL or CUSTOM)
     * @param createdAt   creation timestamp; defaults to now if null
     */
    public SharedExpense(
            Long creatorId,
            String description,
            BigDecimal totalAmount,
            SplitType splitType,
            LocalDateTime createdAt
    ) {
        this.creatorId = creatorId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.splitType = splitType;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * JPA lifecycle hook setting creation timestamp before insertion.
     */
    @PrePersist
    protected void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * Adds a participant to this shared expense and establishes the bidirectional relationship.
     *
     * @param participant the participant to associate
     */
    public void addParticipant(ExpenseParticipant participant) {
        if (participant != null) {
            this.participants.add(participant);
            participant.setSharedExpense(this);
        }
    }

    /**
     * Returns the unique surrogate identifier.
     *
     * @return expense ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the creator user ID who paid the full expense upfront.
     *
     * @return creator user ID
     */
    public Long getCreatorId() {
        return creatorId;
    }

    /**
     * Sets the creator user ID.
     *
     * @param creatorId creator user ID
     */
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Returns the expense narrative description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the expense narrative description.
     *
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the total monetary amount of the expense.
     *
     * @return total amount
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets the total monetary amount of the expense.
     *
     * @param totalAmount new total amount
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Returns the split strategy type.
     *
     * @return split type
     */
    public SplitType getSplitType() {
        return splitType;
    }

    /**
     * Sets the split strategy type.
     *
     * @param splitType new split type
     */
    public void setSplitType(SplitType splitType) {
        this.splitType = splitType;
    }

    /**
     * Returns the list of participating users and their assigned shares.
     *
     * @return list of participants
     */
    public List<ExpenseParticipant> getParticipants() {
        return participants;
    }

    /**
     * Replaces the participants list and establishes bidirectional references.
     *
     * @param participants new list of participants
     */
    public void setParticipants(List<ExpenseParticipant> participants) {
        this.participants.clear();
        if (participants != null) {
            for (ExpenseParticipant p : participants) {
                addParticipant(p);
            }
        }
    }

    /**
     * Returns the immutable creation timestamp.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedExpense that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SharedExpense{" +
                "id=" + id +
                ", creatorId=" + creatorId +
                ", description='" + description + '\'' +
                ", totalAmount=" + totalAmount +
                ", splitType=" + splitType +
                ", createdAt=" + createdAt +
                '}';
    }
}

