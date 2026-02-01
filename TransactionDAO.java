package org.example;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    public void log(Connection conn,
                    Long fromId,
                    Long toId,
                    String type,
                    BigDecimal amount,
                    String note) throws SQLException {

        String sql = """
                INSERT INTO transactions(from_account_id, to_account_id, type, amount, note)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (fromId == null) ps.setNull(1, Types.BIGINT);
            else ps.setLong(1, fromId);

            if (toId == null) ps.setNull(2, Types.BIGINT);
            else ps.setLong(2, toId);

            ps.setString(3, type);
            ps.setBigDecimal(4, amount);
            ps.setString(5, note);
            ps.executeUpdate();
        }
    }

    public List<Transaction> listByAccount(Connection conn, long accountId, int limit) throws SQLException {
        String sql = """
            SELECT id, from_account_id, to_account_id, type, amount, note, created_at
            FROM transactions
            WHERE from_account_id = ? OR to_account_id = ?
            ORDER BY created_at DESC
            LIMIT ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ps.setLong(2, accountId);
            ps.setInt(3, Math.max(1, Math.min(limit, 200)));
            try (ResultSet rs = ps.executeQuery()) {
                List<Transaction> out = new ArrayList<>();
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime created = (ts == null) ? null : ts.toLocalDateTime();
                    out.add(new Transaction(
                            rs.getLong("id"),
                            (Long) rs.getObject("from_account_id"),
                            (Long) rs.getObject("to_account_id"),
                            rs.getString("type"),
                            rs.getBigDecimal("amount"),
                            rs.getString("note"),
                            created
                    ));
                }
                return out;
            }
        }
    }
}
