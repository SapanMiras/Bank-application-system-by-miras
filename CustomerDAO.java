package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public long create(Connection conn, String fullName, String email) throws SQLException {
        String sql = "INSERT INTO customers(full_name, email) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            if (email == null || email.isBlank()) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public Customer getById(Connection conn, long id) throws SQLException {
        String sql = "SELECT id, full_name, email FROM customers WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Customer(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email")
                );
            }
        }
    }

    public List<Customer> list(Connection conn) throws SQLException {
        String sql = "SELECT id, full_name, email FROM customers ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Customer> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Customer(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email")
                ));
            }
            return out;
        }
    }
}
