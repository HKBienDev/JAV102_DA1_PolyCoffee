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
                SET full_name = ?, email = ?, phone = ?, role_id = ?, is_active = ?, loyalty_points = ?
                WHERE id = ?
                """;
        try {
            return JdbcUtil.executeUpdate(sql,
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

    public int updatePassword(int id, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, newPassword, id);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đổi mật khẩu User", e);
        }
    }

    public int updateActive(int id, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, isActive, id);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái hoạt động User", e);
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
        if (username == null || username.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        List<User> list = findBySql(sql, username);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<User> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT * FROM users WHERE username ILIKE ? OR full_name ILIKE ? OR phone ILIKE ? OR email ILIKE ? ORDER BY id DESC";
        return findBySql(sql, pattern, pattern, pattern, pattern);
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) LIMIT 1";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, username);
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra trùng username", e);
        } finally {
            closeResultSet(rs);
        }
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) LIMIT 1";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, email);
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra trùng email", e);
        } finally {
            closeResultSet(rs);
        }
    }

    public boolean existsByEmailExceptId(String email, int excludeId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) AND id <> ? LIMIT 1";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, email, excludeId);
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra trùng email khi cập nhật", e);
        } finally {
            closeResultSet(rs);
        }
    }

    public List<User> filterStaff(String keyword, Integer roleId, Boolean active) {
        StringBuilder sql = new StringBuilder(
                "SELECT u.* FROM users u JOIN roles r ON u.role_id = r.id WHERE UPPER(TRIM(r.name)) <> 'CUSTOMER'");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim() + "%";
            sql.append(" AND (u.username ILIKE ? OR u.full_name ILIKE ? OR u.phone ILIKE ? OR u.email ILIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (roleId != null && roleId > 0) {
            sql.append(" AND u.role_id = ?");
            params.add(roleId);
        }
        if (active != null) {
            sql.append(" AND u.is_active = ?");
            params.add(active);
        }

        sql.append(" ORDER BY u.id DESC");
        return findBySql(sql.toString(), params.toArray());
    }

    private void closeResultSet(ResultSet rs) {
        if (rs == null)
            return;
        Statement stmt = null;
        Connection conn = null;
        try {
            stmt = rs.getStatement();
        } catch (SQLException ignored) {
        }
        try {
            if (stmt != null)
                conn = stmt.getConnection();
        } catch (SQLException ignored) {
        }
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}