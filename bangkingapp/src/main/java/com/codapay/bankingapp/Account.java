package com.codapay.bankingapp;

import java.util.concurrent.locks.ReentrantLock;

import com.codapay.bankingapp.exceptions.InsufficientFundException;
import com.codapay.bankingapp.exceptions.InvalidAmountException;

public class Account {
    private final String accountNumber;
    private final String accountHolderName;
    private double balance;

    // This lock is used to synchronize access to the balance
    private final Object balanceLock = new Object();
    final ReentrantLock lock = new ReentrantLock();

    public Account(String accountNumber, String accountHolderName, double initialDeposit)
            throws InvalidAmountException {
        if (initialDeposit < 0) {
            throw new InvalidAmountException("Initial deposit cannot be negative.");
        }

        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = initialDeposit;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive.");
        }

        // Synchronize access to the balance
        synchronized (balanceLock) {
            balance += amount;
            System.out.println("Deposited: " + amount + " successfully. New balance: " + balance);
        }
    }

    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdraw amount cannot be zero or nagative.");
        }

        // Synchronize access to the balance

        synchronized (balanceLock) {
            if (amount > balance) {
                throw new InsufficientFundException("Insufficient funds. Current balance: " + balance);
            }

            balance -= amount;
            System.out.println("Withdrawn: " + amount + " successfully. New balance: " + balance);
        }
    }

    @Override
    public String toString() {
        return "Account: {" +
                "accountNumber='" + accountNumber + '\'' +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", balance=" + balance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Account))
            return false;

        Account other = (Account) o;

        if (!accountNumber.equals(other.accountNumber))
            return false;

        if (Double.compare(other.balance, balance) != 0)
            return false;

        return accountHolderName.equals(other.accountHolderName);
    }

    @Override
    public int hashCode() {
        return accountNumber.hashCode() + accountHolderName.hashCode() + Double.hashCode(balance);
    }
}
