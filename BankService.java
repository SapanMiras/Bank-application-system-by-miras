package org.example;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BankService {
    private final BankDAO bankDAO = new BankDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO txDAO = new TransactionDAO();

    // ---------------- BANKS ----------------
    public long createBank(String name) throws SQLException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Bank name is required");
        try (Connection conn = DB.getConnection()) {
            return bankDAO.create(conn, name.trim());
        }
    }

    public Bank getBank(long bankId) throws SQLException {
        try (Connection conn = DB.getConnection()) {
            Bank b = bankDAO.getById(conn, bankId);
            if (b == null) throw new IllegalArgumentException("Bank not found: " + bankId);
            return b;
        }
    }

    public List<Bank> listBanks() throws SQLException {
        try (Connection conn = DB.getConnection()) {
            return bankDAO.list(conn);
        }
    }

    // ---------------- CUSTOMERS ----------------
    public long createCustomer(String fullName, String email) throws SQLException {
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Customer fullName is required");
        try (Connection conn = DB.getConnection()) {
            return customerDAO.create(conn, fullName.trim(), email);
        }
    }

    public Customer getCustomer(long customerId) throws SQLException {
        try (Connection conn = DB.getConnection()) {
            Customer c = customerDAO.getById(conn, customerId);
            if (c == null) throw new IllegalArgumentException("Customer not found: " + customerId);
            return c;
        }
    }

    public List<Customer> listCustomers() throws SQLException {
        try (Connection conn = DB.getConnection()) {
            return customerDAO.list(conn);
        }
    }

    // ---------------- ACCOUNTS ----------------
    public long openAccount(long customerId, long bankId, BigDecimal initialBalance) throws SQLException {
        if (initialBalance == null) initialBalance = BigDecimal.ZERO;
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance can't be negative");
        }

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Customer c = customerDAO.getById(conn, customerId);
                if (c == null) throw new IllegalArgumentException("Customer not found: " + customerId);

                Bank b = bankDAO.getById(conn, bankId);
                if (b == null) throw new IllegalArgumentException("Bank not found: " + bankId);

                long accountId = accountDAO.createAccount(conn, customerId, bankId, c.fullName, initialBalance);

                if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
                    txDAO.log(conn, null, accountId, "DEPOSIT", initialBalance, "Initial deposit");
                }
                conn.commit();
                return accountId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Account getAccount(long accountId) throws SQLException {
        try (Connection conn = DB.getConnection()) {
            Account acc = accountDAO.getAccount(conn, accountId);
            if (acc == null) throw new IllegalArgumentException("Account not found: " + accountId);
            return acc;
        }
    }

    public BigDecimal getBalance(long accountId) throws SQLException {
        return getAccount(accountId).balance;
    }

    public void deposit(long accountId, BigDecimal amount) throws SQLException {
        validateAmount(amount);

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal bal = accountDAO.getBalanceForUpdate(conn, accountId);
                if (bal == null) throw new IllegalArgumentException("Account not found: " + accountId);

                BigDecimal newBal = bal.add(amount);
                accountDAO.updateBalance(conn, accountId, newBal);

                txDAO.log(conn, null, accountId, "DEPOSIT", amount, "Deposit");

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void withdraw(long accountId, BigDecimal amount) throws SQLException {
        validateAmount(amount);

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal bal = accountDAO.getBalanceForUpdate(conn, accountId);
                if (bal == null) throw new IllegalArgumentException("Account not found: " + accountId);

                if (bal.compareTo(amount) < 0) throw new IllegalArgumentException("Not enough money");

                BigDecimal newBal = bal.subtract(amount);
                accountDAO.updateBalance(conn, accountId, newBal);

                txDAO.log(conn, accountId, null, "WITHDRAW", amount, "Withdraw");

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void transfer(long fromId, long toId, BigDecimal amount) throws SQLException {
        validateAmount(amount);
        if (fromId == toId) throw new IllegalArgumentException("fromId and toId can't be same");

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // To avoid deadlocks, lock accounts in the same order every time
                long first = Math.min(fromId, toId);
                long second = Math.max(fromId, toId);

                BigDecimal balFirst = accountDAO.getBalanceForUpdate(conn, first);
                if (balFirst == null) throw new IllegalArgumentException("Account not found: " + first);

                BigDecimal balSecond = accountDAO.getBalanceForUpdate(conn, second);
                if (balSecond == null) throw new IllegalArgumentException("Account not found: " + second);

                BigDecimal fromBal = (fromId == first) ? balFirst : balSecond;
                BigDecimal toBal   = (toId == first) ? balFirst : balSecond;

                if (fromBal.compareTo(amount) < 0) throw new IllegalArgumentException("Not enough money");

                accountDAO.updateBalance(conn, fromId, fromBal.subtract(amount));
                accountDAO.updateBalance(conn, toId, toBal.add(amount));

                txDAO.log(conn, fromId, toId, "TRANSFER", amount, "Transfer");

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Transaction> listTransactions(long accountId, int limit) throws SQLException {
        try (Connection conn = DB.getConnection()) {
            Account acc = accountDAO.getAccount(conn, accountId);
            if (acc == null) throw new IllegalArgumentException("Account not found: " + accountId);
            return txDAO.listByAccount(conn, accountId, limit);
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
    }
}
