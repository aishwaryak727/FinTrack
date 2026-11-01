package com.fintrack.expense.service;

import com.fintrack.exception.InvalidExpenseException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.expense.dto.CreateExpenseRequest;
import com.fintrack.expense.dto.ParticipantShareRequest;
import com.fintrack.expense.dto.SharedExpenseResponse;
import com.fintrack.expense.dto.UserBalanceResponse;
import com.fintrack.expense.entity.ExpenseParticipant;
import com.fintrack.expense.entity.SharedExpense;
import com.fintrack.expense.entity.SplitType;
import com.fintrack.expense.repository.SharedExpenseRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Production implementation of {@link ExpenseService} handling group expense
 * creation, EQUAL and CUSTOM split distributions with exact penny allocation,
 * and cumulative multi-expense net balance settlement.
 */
@Service
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseServiceImpl.class);

    private final SharedExpenseRepository sharedExpenseRepository;

    /**
     * Constructs ExpenseServiceImpl with constructor injection.
     *
     * @param sharedExpenseRepository repository for shared expenses
     */
    public ExpenseServiceImpl(SharedExpenseRepository sharedExpenseRepository) {
        this.sharedExpenseRepository = Objects.requireNonNull(
                sharedExpenseRepository,
                "sharedExpenseRepository must not be null"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SharedExpenseResponse createExpense(CreateExpenseRequest request) {
        validateRequest(request);

        BigDecimal normalizedTotal = request.totalAmount().setScale(2, RoundingMode.HALF_UP);

        log.info("Creating shared expense: creatorId={}, totalAmount={}, splitType={}, participantsCount={}",
                request.creatorId(), normalizedTotal, request.splitType(), request.participants().size());

        SharedExpense expense = new SharedExpense(
                request.creatorId(),
                request.description().trim(),
                normalizedTotal,
                request.splitType(),
                LocalDateTime.now()
        );

        List<ExpenseParticipant> participantEntities = calculateParticipantShares(request, normalizedTotal);
        for (ExpenseParticipant participant : participantEntities) {
            expense.addParticipant(participant);
        }

        SharedExpense saved = sharedExpenseRepository.save(expense);
        log.info("Successfully persisted shared expense id={} with {} participants",
                saved.getId(), saved.getParticipants().size());

        return SharedExpenseResponse.fromEntity(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SharedExpenseResponse getExpenseById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidExpenseException("Expense ID must be positive");
        }

        SharedExpense expense = sharedExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared expense not found with ID: " + id));

        return SharedExpenseResponse.fromEntity(expense);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SharedExpenseResponse> getAllExpenses() {
        return sharedExpenseRepository.findAllWithParticipants()
                .stream()
                .map(SharedExpenseResponse::fromEntity)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Long, BigDecimal> getBalances() {
        List<SharedExpense> allExpenses = sharedExpenseRepository.findAllWithParticipants();
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();

        for (SharedExpense expense : allExpenses) {
            Long creatorId = expense.getCreatorId();
            BigDecimal totalPaid = expense.getTotalAmount().setScale(2, RoundingMode.HALF_UP);

            // Creator pays the full expense upfront: +totalPaid
            balances.put(creatorId, balances.getOrDefault(creatorId, BigDecimal.ZERO).add(totalPaid));

            // Each participant owes their designated share: -shareAmount
            if (expense.getParticipants() != null) {
                for (ExpenseParticipant participant : expense.getParticipants()) {
                    Long userId = participant.getUserId();
                    BigDecimal share = participant.getShareAmount().setScale(2, RoundingMode.HALF_UP);
                    balances.put(userId, balances.getOrDefault(userId, BigDecimal.ZERO).subtract(share));
                }
            }
        }

        // Normalize all balances to 2 decimal places
        Map<Long, BigDecimal> normalizedBalances = new LinkedHashMap<>();
        BigDecimal sumOfBalances = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
            BigDecimal scaled = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            normalizedBalances.put(entry.getKey(), scaled);
            sumOfBalances = sumOfBalances.add(scaled);
        }

        log.info("Calculated net balances for {} users. Total balance sum: {}",
                normalizedBalances.size(), sumOfBalances);

        return normalizedBalances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserBalanceResponse> getUserBalances() {
        Map<Long, BigDecimal> balances = getBalances();
        return balances.entrySet().stream()
                .map(e -> UserBalanceResponse.of(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * Calculates the individual participant shares based on the selected split type.
     * For EQUAL splits, exact penny distribution is used to guarantee that the sum of shares
     * equals the total expense amount down to the exact cent.
     *
     * @param request         the creation request
     * @param normalizedTotal the total expense amount normalized to 2 decimal places
     * @return list of populated ExpenseParticipant entities
     */
    private List<ExpenseParticipant> calculateParticipantShares(
            CreateExpenseRequest request,
            BigDecimal normalizedTotal
    ) {
        List<ExpenseParticipant> participants = new ArrayList<>();
        int count = request.participants().size();

        if (request.splitType() == SplitType.EQUAL) {
            long totalCents = normalizedTotal.movePointRight(2).longValueExact();
            long baseCents = totalCents / count;
            long remainderCents = totalCents % count;

            for (int i = 0; i < count; i++) {
                ParticipantShareRequest p = request.participants().get(i);
                long participantCents = baseCents + (i < remainderCents ? 1 : 0);
                BigDecimal share = BigDecimal.valueOf(participantCents, 2);
                participants.add(new ExpenseParticipant(p.userId(), share));
            }
        } else if (request.splitType() == SplitType.CUSTOM) {
            BigDecimal runningSum = BigDecimal.ZERO;

            for (ParticipantShareRequest p : request.participants()) {
                if (p.shareAmount() == null) {
                    throw new InvalidExpenseException(
                            "Share amount is required for all participants when using CUSTOM split"
                    );
                }

                if (p.shareAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new InvalidExpenseException(
                            "Share amount must be greater than zero for participant: " + p.userId()
                    );
                }

                BigDecimal scaledShare = p.shareAmount().setScale(2, RoundingMode.HALF_UP);
                runningSum = runningSum.add(scaledShare);
                participants.add(new ExpenseParticipant(p.userId(), scaledShare));
            }

            if (runningSum.compareTo(normalizedTotal) != 0) {
                throw new InvalidExpenseException(String.format(
                        "Sum of custom participant shares (%s) must equal the total expense amount (%s)",
                        runningSum, normalizedTotal
                ));
            }
        }

        return participants;
    }

    /**
     * Validates incoming request parameters against business invariants.
     *
     * @param request creation request
     */
    private void validateRequest(CreateExpenseRequest request) {
        if (request == null) {
            throw new InvalidExpenseException("Expense request body is required");
        }

        if (request.creatorId() == null || request.creatorId() <= 0) {
            throw new InvalidExpenseException("Creator ID must be a positive integer");
        }

        if (request.description() == null || request.description().isBlank()) {
            throw new InvalidExpenseException("Expense description is required");
        }

        if (request.description().length() > 255) {
            throw new InvalidExpenseException("Description cannot exceed 255 characters");
        }

        if (request.totalAmount() == null) {
            throw new InvalidExpenseException("Total amount is required");
        }

        if (request.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseException("Total amount must be greater than zero");
        }

        if (request.totalAmount().scale() > 2) {
            throw new InvalidExpenseException("Total amount cannot have more than 2 decimal places");
        }

        if (request.splitType() == null) {
            throw new InvalidExpenseException("Split type is required (EQUAL or CUSTOM)");
        }

        if (request.participants() == null || request.participants().size() < 2) {
            throw new InvalidExpenseException("An expense requires at least two unique participants");
        }

        Set<Long> participantIds = new HashSet<>();
        for (ParticipantShareRequest p : request.participants()) {
            if (p == null || p.userId() == null) {
                throw new InvalidExpenseException("Participant user ID is required");
            }
            if (p.userId() <= 0) {
                throw new InvalidExpenseException("Participant user ID must be positive");
            }
            if (!participantIds.add(p.userId())) {
                throw new InvalidExpenseException("Duplicate participant detected: user ID " + p.userId());
            }
            if (p.shareAmount() != null && p.shareAmount().scale() > 2) {
                throw new InvalidExpenseException("Participant share amount cannot have more than 2 decimal places");
            }
        }

        if (participantIds.size() < 2) {
            throw new InvalidExpenseException("An expense requires at least two unique participants");
        }
    }
}

