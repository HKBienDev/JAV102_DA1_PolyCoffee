package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entity.Category;
import utils.JdbcUtil;

public class CategoryDAO implements CrudDAO<Category, Integer> {
    private Category mapRow(ResultSet rs) throws SQLException {
        return Category.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .active(rs.getBoolean("is_active"))
                .build();
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

    @Override
    public int create(Category entity) {
        String sql = "INSERT INTO categories (name, description, is_active) VALUES (?, ?, ?)";
        try {
            return JdbcUtil.executeUpdate(
                    sql,
                    entity.getName(),
                    entity.getDescription(),
                    entity.isActive());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm mới Category", e);
        }
    }

    @Override
    public int update(Category entity) {
        String sql = "UPDATE categories SET name = ?, description = ?, is_active = ? WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(
                    sql,
                    entity.getName(),
                    entity.getDescription(),
                    entity.isActive(),
                    entity.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật Category", e);
        }
    }

    @Override
    public int delete(Integer id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa Category", e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM categories";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm Category", e);
        } finally {
            closeResultSet(rs);
        }
    }

    @Override
    public List<Category> findBySql(String sql, Object... value) {
        List<Category> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, value);
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn Category", e);
        } finally {
            closeResultSet(rs);
        }
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY id ASC";
        return findBySql(sql);
    }

    @Override
    public Category findById(Integer id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        List<Category> list = findBySql(sql, id);
        return list.isEmpty() ? null : list.get(0);
    }

    // Lấy các danh mục đang hoạt động để hiển thị lên menu
    public List<Category> findAllActive() {
        String sql = "SELECT * FROM categories WHERE is_active = TRUE ORDER BY id ASC";
        return findBySql(sql);
    }

    // Kiểm tra trùng tên danh mục (không phân biệt hoa/thường và khoảng trắng
    // đầu/cuối)
    public boolean existsByName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, name);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra trùng tên Category", e);
        } finally {
            closeResultSet(rs);
        }
        return false;
    }

    public boolean existsByNameExcludeId(String name, int excludeId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(TRIM(name)) = LOWER(TRIM(?)) AND id <> ?";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, name, excludeId);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra trùng tên Category khi cập nhật", e);
        } finally {
            closeResultSet(rs);
        }
        return false;
    }

    // [MỚI BỔ SUNG] Xóa mềm: bật/tắt trạng thái hoạt động của danh mục
    public int updateActive(int id, boolean isActive) {
        String sql = "UPDATE categories SET is_active = ? WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, isActive, id);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái Category", e);
        }
    }
}
