package com.codapay.bankingapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.codapay.bankingapp.exceptions.InvalidAmountException;

public class AccountTest {
    @Test
    public void deposit_validAmount_increasesBalance() throws InvalidAmountException {
        // Arrange
        Account account = new Account("123", "John Doe", 100.00);

        // Act
        account.deposit(50);

        // Assert
        assertEquals(150, account.getBalance(), "Deposit did not increase balance correctly");
    }

    @Test
    public void deposit_invalidAmount_throwsException() throws InvalidAmountException {
        // Arrange
        Account account = new Account("123", "John Doe", 100.00);
        double invalidAmount = -50;
        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            account.deposit(invalidAmount);
        }, "Expected deposit to throw InvalidAmountException, but it didn't");
    }
}
