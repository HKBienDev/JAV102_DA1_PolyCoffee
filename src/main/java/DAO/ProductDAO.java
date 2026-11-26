package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entity.Product;
import utils.JdbcUtil;

public class ProductDAO implements CrudDAO<Product, Integer> {
    // Chuyển 1 dòng dữ liệu từ ResultSet thành đối tượng Product
    private Product mapRow(ResultSet rs) throws SQLException {
        return Product.builder()
                .id(rs.getInt("id"))
                .categoryId(rs.getInt("category_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .imageUrl(rs.getString("image_url"))
                .available(rs.getBoolean("is_available"))
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }

    // Đóng kết nối an toàn sau khi dùng JdbcUtil.executeQuery
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
    public int create(Product entity) {
        String sql = "INSERT INTO products (category_id, name, description, image_url, is_available) VALUES (?, ?, ?, ?, ?)";
        try {
            return JdbcUtil.executeUpdate(
                    sql,
                    entity.getCategoryId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getImageUrl(),
                    entity.isAvailable());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm mới Product", e);
        }
    }

    public int createReturningId(Product entity) {
        String sql = "INSERT INTO products (category_id, name, description, image_url, is_available) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(
                    sql,
                    entity.getCategoryId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getImageUrl(),
                    entity.isAvailable());
            if (rs.next()) {
                return rs.getInt("id");
            }
            throw new RuntimeException("Không lấy được ID sau khi thêm mới Product");
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm mới Product", e);
        } finally {
            closeResultSet(rs);
        }
    }

    @Override
    public int update(Product entity) {
        String sql = "UPDATE products SET category_id = ?, name = ?, description = ?, image_url = ?, is_available = ? WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(
                    sql,
                    entity.getCategoryId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getImageUrl(),
                    entity.isAvailable(),
                    entity.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật Product", e);
        }
    }

    @Override
    public int delete(Integer id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa Product", e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM products";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm Product", e);
        } finally {
            closeResultSet(rs);
        }
    }

    @Override
    public List<Product> findBySql(String sql, Object... value) {
        List<Product> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, value);
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn Product", e);
        } finally {
            closeResultSet(rs);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM products ORDER BY id ASC";
        return findBySql(sql);
    }

    @Override
    public Product findById(Integer id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        List<Product> list = findBySql(sql, id);
        return list.isEmpty() ? null : list.get(0);
    }

    // Lấy các món đang bán (is_available = true)
    public List<Product> findAllAvailable() {
        String sql = "SELECT p.* FROM products p "
                + "JOIN categories c ON p.category_id = c.id "
                + "WHERE p.is_available = TRUE AND c.is_active = TRUE "
                + "ORDER BY p.id ASC";
        return findBySql(sql);
    }

    public List<Product> findByCategoryId(int categoryId) {
        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY id ASC";
        return findBySql(sql, categoryId);
    }

    // Lọc món theo danh mục
    public List<Product> findAvailableByCategoryId(int categoryId) {
        String sql = "SELECT p.* FROM products p "
                + "JOIN categories c ON p.category_id = c.id "
                + "WHERE p.category_id = ? AND p.is_available = TRUE AND c.is_active = TRUE "
                + "ORDER BY p.id ASC";
        return findBySql(sql, categoryId);
    }

    // Tìm kiếm món theo tên (dùng ILIKE để không phân biệt hoa/thường trong
    // PostgreSQL)
    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = "SELECT * FROM products WHERE name ILIKE ? ORDER BY id ASC";
        return findBySql(sql, "%" + keyword.trim() + "%");
    }

    // Bật/tắt nhanh trạng thái còn hàng
    public int updateAvailable(int id, boolean isAvailable) {
        String sql = "UPDATE products SET is_available = ? WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, isAvailable, id);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái Product", e);
        }
    }
}