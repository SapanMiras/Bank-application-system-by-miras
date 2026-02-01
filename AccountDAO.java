package org.example;

import java.math.BigDecimal;
import java.sql.*;

public class AccountDAO {

    public long createAccount(Connection conn, long customerId, long bankId, String ownerName, BigDecimal initialBalance) throws SQLException {
        String sql = "INSERT INTO accounts(customer_id, bank_id, owner_name, balance) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, bankId);
            ps.setString(3, ownerName);
            ps.setBigDecimal(4, initialBalance);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public Account getAccount(Connection conn, long id) throws SQLException {
        String sql = "SELECT id, bank_id, customer_id, owner_name, balance FROM accounts WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Account(
                        rs.getLong("id"),
                        rs.getLong("bank_id"),
                        rs.getLong("customer_id"),
                        rs.getString("owner_name"),
                        rs.getBigDecimal("balance")
                );
            }
        }
    }

    // FOR UPDATE locks the row for the duration of the transaction (prevents race conditions)
    public BigDecimal getBalanceForUpdate(Connection conn, long id) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getBigDecimal(1);
            }
        }
    }

    public void updateBalance(Connection conn, long id, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, id);
            int updated = ps.executeUpdate();
            if (updated == 0) throw new SQLException("Account not found: " + id);
        }
    }
}
