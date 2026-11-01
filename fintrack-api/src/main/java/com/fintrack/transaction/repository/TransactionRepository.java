package com.fintrack.transaction.repository;

import com.fintrack.transaction.entity.Transaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository providing data access and persistence operations
 * for {@link Transaction} entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions owned by a given user, ordered with newest transactions first.
     *
     * @param userId user identifier
     * @return list of matching transactions
     */
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds a paginated slice of transactions owned by a user, ordered with newest transactions first.
     *
     * @param userId   user identifier
     * @param pageable pagination parameters (page, size, sort)
     * @return page of matching transactions
     */
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Finds a specific transaction by its ID while guaranteeing ownership by the specified user.
     *
     * @param id     transaction identifier
     * @param userId user identifier
     * @return Optional containing the transaction if present and owned by user, empty otherwise
     */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks whether a transaction with the given ID exists and belongs to the given user.
     *
     * @param id     transaction identifier
     * @param userId user identifier
     * @return true if the transaction exists and belongs to the user, false otherwise
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Deletes a specific transaction by ID while strictly enforcing ownership by the user.
     *
     * @param id     transaction identifier
     * @param userId user identifier
     */
    @Modifying
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * Deletes all transactions owned by a specific user.
     *
     * @param userId user identifier
     */
    @Modifying
    void deleteByUserId(Long userId);
}
