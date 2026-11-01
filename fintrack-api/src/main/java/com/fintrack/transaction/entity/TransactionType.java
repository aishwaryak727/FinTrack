package com.fintrack.transaction.entity;

/**
 * Represents the supported types of financial transactions within FinTrack.
 */
public enum TransactionType {

    /**
     * Inflow of funds into the user's account.
     */
    INCOME,

    /**
     * Outflow of funds representing an expense.
     */
    EXPENSE,

    /**
     * Movement of funds between accounts or entities.
     */
    TRANSFER
}

