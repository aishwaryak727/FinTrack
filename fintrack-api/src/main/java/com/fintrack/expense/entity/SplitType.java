package com.fintrack.expense.entity;

/**
 * Defines the supported split methods for shared expenses.
 */
public enum SplitType {

    /**
     * Expense is divided equally among all designated participants.
     */
    EQUAL,

    /**
     * Expense is divided according to explicit custom share amounts per participant.
     */
    CUSTOM
}

