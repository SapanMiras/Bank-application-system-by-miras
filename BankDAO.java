package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankDAO {

    public long create(Connection conn, String name) throws SQLException {
        String sql = "INSERT INTO banks(name) VALUES (?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public Bank getById(Connection conn, long id) throws SQLException {
        String sql = "SELECT id, name FROM banks WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Bank(rs.getLong("id"), rs.getString("name"));
            }
        }
    }

    public List<Bank> list(Connection conn) throws SQLException {
        String sql = "SELECT id, name FROM banks ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Bank> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Bank(rs.getLong("id"), rs.getString("name")));
            }
            return out;
        }
    }
}
