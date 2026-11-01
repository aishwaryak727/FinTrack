package com.fintrack.expense.repository;

import com.fintrack.expense.entity.SharedExpense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository providing data access and query operations
 * for {@link SharedExpense} entities.
 */
@Repository
public interface SharedExpenseRepository extends JpaRepository<SharedExpense, Long> {

    /**
     * Retrieves all shared expenses eagerly fetching their participants
     * to eliminate N+1 queries during net balance computations.
     *
     * @return list of all shared expenses with populated participants
     */
    @Query("SELECT DISTINCT se FROM SharedExpense se LEFT JOIN FETCH se.participants ORDER BY se.createdAt DESC")
    List<SharedExpense> findAllWithParticipants();

    /**
     * Finds all shared expenses initiated by a specific creator.
     *
     * @param creatorId the creator's user ID
     * @return list of shared expenses created by the given user
     */
    List<SharedExpense> findByCreatorId(Long creatorId);
}

