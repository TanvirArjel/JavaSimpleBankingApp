package com.codapay.bankingapp;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.codapay.bankingapp.exceptions.AccountNotFoundException;
import com.codapay.bankingapp.exceptions.InsufficientFundException;
import com.codapay.bankingapp.exceptions.InvalidAmountException;

public class Bank {
    private ConcurrentHashMap<String, Account> accounts;
    private final AtomicLong accountNumberGenerator = new AtomicLong(1000000);

    public Bank() {
        accounts = new ConcurrentHashMap<>();
    }

    private String generateAccountNumber() {
        String accountNumber = String.valueOf(accountNumberGenerator.getAndIncrement());
        return accountNumber;
    }

    public String createAccount(String accountName, double deposit) throws InvalidAmountException {
        if (accountName == null || accountName.length() == 0) {
            throw new IllegalArgumentException("Account name cannot be null or empty");
        }

        if (deposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        }

        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, accountName, deposit);

        accounts.put(accountNumber, account);
        System.out.println("Account " + accountNumber + "added successfully.");

        return accountNumber;
    }

    public void removeAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() == 0 || !accounts.containsKey(accountNumber)) {
            throw new IllegalArgumentException("Account not found");
        }

        accounts.remove(accountNumber);
        System.out.println("Account " + accountNumber + " removed successfully.");
    }

    public Collection<Account> getAccounts() {
        return accounts.values();
    }

    public Account getAccountByNumber(String accountNumber) throws AccountNotFoundException {
        if (accountNumber == null || accountNumber.length() == 0) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        if (!accounts.containsKey(accountNumber)) {
            throw new AccountNotFoundException("Account not found");
        }

        return accounts.get(accountNumber);
    }

    public void deposit(String accountNumber, double amount) throws InvalidAmountException {
        if (accountNumber == null || accountNumber.length() == 0 || !accounts.containsKey(accountNumber)) {
            throw new IllegalArgumentException("Account not found");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        Account account = accounts.get(accountNumber);
        account.deposit(amount);
        System.out.println("Deposited " + amount + " to account " + accountNumber);
    }

    public void withdraw(String accountNumber, double amount)
            throws InsufficientFundException, InvalidAmountException {
        if (accountNumber == null || accountNumber.length() == 0 || !accounts.containsKey(accountNumber)) {
            throw new IllegalArgumentException("Account not found");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        Account account = accounts.get(accountNumber);
        account.withdraw(amount);
        System.out.println("Withdrew " + amount + " from account " + accountNumber);
    }

    public void transfer(String fromAccountNumber, String toAccountNumber, double amount)
            throws Exception {
        if (fromAccountNumber == null || fromAccountNumber.length() == 0 || !accounts.containsKey(fromAccountNumber)) {
            throw new IllegalArgumentException("From account not found");
        }

        if (toAccountNumber == null || toAccountNumber.length() == 0 || !accounts.containsKey(toAccountNumber)) {
            throw new IllegalArgumentException("To account not found");
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive");
        }

        Account fromAccount = accounts.get(fromAccountNumber);
        Account toAccount = accounts.get(toAccountNumber);

        // Do ordered locking to prevent deadlock
        Account firstLock = fromAccountNumber.compareTo(toAccountNumber) < 0 ? fromAccount : toAccount;
        Account secondLock = fromAccountNumber.compareTo(toAccountNumber) < 0 ? toAccount : fromAccount;

        // Using ReentrantLock to avoid deadlock

        if (firstLock.lock.tryLock(1000, TimeUnit.MICROSECONDS)) {
            try {
                if (secondLock.lock.tryLock(1000, TimeUnit.MICROSECONDS)) {
                    try {
                        fromAccount.withdraw(amount);
                        try {
                            toAccount.deposit(amount);
                            System.out.println(
                                    "Transferred " + amount + " from account " + fromAccountNumber + " to account "
                                            + toAccountNumber);
                        } catch (Exception e) {
                            // TODO: handle exception
                            System.err.println("Transfer failed: " + e.getMessage());
                            // Rollback the withdrawal
                            try {
                                fromAccount.deposit(amount);
                                System.out.println(
                                        "Rolled back withdrawal of " + amount + " from account " + fromAccountNumber);

                            } catch (Exception innerException) {
                                // TODO: handle exception
                                System.err.println("Rollback failed: " + innerException.getMessage());
                                // Log the error
                                System.err.println("Error during rollback: " + innerException.getMessage());
                            }

                        }
                    } catch (Exception e) {
                        System.err.println("Transfer failed: " + e.getMessage());
                    } finally {
                        secondLock.lock.unlock();
                    }
                }

            } finally {
                firstLock.lock.unlock();
            }
        }

        synchronized (firstLock) {
            synchronized (secondLock) {
                try {
                    fromAccount.withdraw(amount);
                    try {
                        toAccount.deposit(amount);
                        System.out.println(
                                "Transferred " + amount + " from account " + fromAccountNumber + " to account "
                                        + toAccountNumber);
                    } catch (Exception e) {
                        // TODO: handle exception
                        System.err.println("Transfer failed: " + e.getMessage());
                        // Rollback the withdrawal
                        try {
                            fromAccount.deposit(amount);
                            System.out.println(
                                    "Rolled back withdrawal of " + amount + " from account " + fromAccountNumber);

                        } catch (Exception innerException) {
                            // TODO: handle exception
                            System.err.println("Rollback failed: " + innerException.getMessage());
                            // Log the error
                            System.err.println("Error during rollback: " + innerException.getMessage());
                        }

                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    System.err.println("Transfer failed: " + e.getMessage());
                }
            }
        }
    }
}
