package com.codapay.bankingapp;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
        Bank bank = new Bank();
        String johnAccountNumber = bank.createAccount("John Doe", 1000.0);
        String smithAccountNumber = bank.createAccount("Jane Smith", 2000.0);
        String aliceAccountNumber = bank.createAccount("Alice Johnson", 1500.0);

        bank.deposit(johnAccountNumber, 200);
        bank.withdraw(smithAccountNumber, 500);
        bank.transfer(johnAccountNumber, smithAccountNumber, 300);

        Account johnAccount = bank.getAccountByNumber(johnAccountNumber);
        Account smithAccount = bank.getAccountByNumber(smithAccountNumber);

        System.out.println("Account balance for John Doe: " + johnAccount.getBalance());
        System.out.println("Account balance for Jane Smith: " + smithAccount.getBalance());

        System.out.println("Hello World!");
    }
}
