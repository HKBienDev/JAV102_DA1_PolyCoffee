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
    private void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                Statement st = rs.getStatement();
                Connection conn = (st != null) ? st.getConnection() : null;
                rs.close();
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
                    entity.isAvailable()
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
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
                    entity.getId()
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Integer id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try {
            return JdbcUtil.executeUpdate(sql, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(rs);
        }
        return 0;
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(rs);
        }
        return list;
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
        String sql = "SELECT * FROM products WHERE is_available = TRUE ORDER BY id ASC";
        return findBySql(sql);
    }

    // Lọc món theo danh mục
    public List<Product> findByCategoryId(int categoryId) {
        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY id ASC";
        return findBySql(sql, categoryId);
    }

    // Tìm kiếm món theo tên (dùng ILIKE để không phân biệt hoa/thường trong PostgreSQL)
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
            e.printStackTrace();
            return 0;
        }
    }
}