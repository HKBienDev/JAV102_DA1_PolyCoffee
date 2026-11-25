package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import entity.User;
import utils.JdbcUtil;

public class UserDAO implements CrudDAO<User, Integer> {

    private User mapRow(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .fullName(rs.getString("full_name"))
                .email(rs.getString("email"))
                .phone(rs.getString("phone"))
                .roleId(rs.getInt("role_id"))
                .active(rs.getBoolean("is_active"))
                .loyaltyPoints(rs.getInt("loyalty_points"))
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }

    @Override
    public int create(User entity) {
        String sql = """
                INSERT INTO users (username, password, full_name, email, phone, role_id, is_active, loyalty_points, created_at )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
                    """;
        try {
            return JdbcUtil.executeUpdate(sql,
                    entity.getUsername(),
                    entity.getPassword(),
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getPhone(),
                    entity.getRoleId(),
                    entity.isActive(),
                    entity.getLoyaltyPoints());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm User", e);
        }
    }

    @Override
    public int update(User entity) {
        String sql = """
                UPDATE users
                SET password = ?, full_name = ?, email = ?, phone = ?, role_id = ?, is_active = ?, loyalty_points = ?
                WHERE id = ?
                """;
        try {
            return JdbcUtil.executeUpdate(sql,
                    entity.getPassword(),
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getPhone(),
                    entity.getRoleId(),
                    entity.isActive(),
                    entity.getLoyaltyPoints(),
                    entity.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật User", e);
        }
    }

    @Override
    public int delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, id);
        } catch (SQLException exception) {
            throw new RuntimeException("Lỗi xóa User", exception);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM users";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Lỗi đếm User", exception);
        } finally {
            closeResultSet(rs);
        }
    }

    @Override
    public List<User> findAll() {
        return findBySql("SELECT * FROM users ORDER BY id DESC");
    }

    @Override
    public User findById(Integer id) {
        List<User> list = findBySql("SELECT * FROM users WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<User> findBySql(String sql, Object... value) {
        List<User> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, value);
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn User", e);
        } finally {
            closeResultSet(rs);
        }
    }

    public User findByUsername(String username) {
        List<User> list = findBySql("SELECT * FROM users WHERE username = ?", username);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<User> search(String keyword) {
        String pattern = "%" + keyword + "%";
        String sql = "SELECT * FROM users WHERE username ILIKE ? OR full_name ILIKE ? OR phone ILIKE ? ORDER BY id DESC";
        return findBySql(sql, pattern, pattern, pattern);
    }

    private void closeResultSet(ResultSet rs) {
        if (rs == null)
            return;
        try {
            Statement stmt = rs.getStatement();
            Connection conn = (stmt != null) ? stmt.getConnection() : null;
            rs.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}