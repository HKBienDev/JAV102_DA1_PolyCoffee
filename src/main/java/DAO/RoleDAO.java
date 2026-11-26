package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import entity.Role;
import utils.JdbcUtil;

public class RoleDAO implements CrudDAO<Role, Integer> {
    private Role mapRow(ResultSet rs) throws SQLException {
        return Role.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .build();
    }

    @Override
    public int create(Role entity) {
        String sql = "INSERT INTO roles (name, description) VALUES (?, ?)";
        try {
            return JdbcUtil.executeUpdate(sql,
                    entity.getName(),
                    entity.getDescription());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm Role", e);
        }
    }

    @Override
    public int update(Role entity) {
        String sql = "UPDATE roles SET name = ?, description = ? WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql,
                    entity.getName(),
                    entity.getDescription(),
                    entity.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật Role", e);
        }
    }

    @Override
    public int delete(Integer id) {
        String sql = "DELETE FROM roles WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa Role", e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM roles";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm Role", e);
        } finally {
            closeResultSet(rs);
        }
    }

    @Override
    public List<Role> findAll() {
        return findBySql("SELECT * FROM roles ORDER BY id ASC");
    }

    @Override
    public Role findById(Integer id) {
        List<Role> list = findBySql("SELECT * FROM roles WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Role> findBySql(String sql, Object... value) {
        List<Role> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, value);
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn Role", e);
        } finally {
            closeResultSet(rs);
        }
    }

    public Role findByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM roles WHERE UPPER(TRIM(name)) = UPPER(TRIM(?))";
        List<Role> list = findBySql(sql, name);
        return list.isEmpty() ? null : list.get(0);
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
