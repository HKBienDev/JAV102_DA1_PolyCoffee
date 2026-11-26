package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entity.OrderItem;
import utils.JdbcUtil;

public class OrderItemDAO implements CrudDAO<OrderItem, Integer> {

    private static final String BASE_SQL = "SELECT id, order_id, product_size_id, quantity, unit_price, sugar_level, ice_level, subtotal "
            + "FROM order_items";

    @Override
    public int create(OrderItem e) {
        String sql = "INSERT INTO order_items (order_id, product_size_id, quantity, unit_price, sugar_level, ice_level, subtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql,
                    e.getOrderId(), e.getProductSizeId(),
                    e.getQuantity(),
                    e.getUnitPrice(),
                    e.getSugarLevel(),
                    e.getIceLevel(),
                    e.getSubtotal());
            if (rs.next()) {
                e.setId(rs.getInt("id"));
                return 1;
            }
            return 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi tạo OrderItem", ex);
        } finally {
            close(rs);
        }
    }

    @Override
    public int update(OrderItem e) {
        String sql = "UPDATE order_items SET order_id=?, product_size_id=?, quantity=?, unit_price=?, "
                + "sugar_level=?, ice_level=?, subtotal=? WHERE id=?";
        return execUpdate(sql,
                e.getOrderId(),
                e.getProductSizeId(),
                e.getQuantity(),
                e.getUnitPrice(),
                e.getSugarLevel(),
                e.getIceLevel(),
                e.getSubtotal(),
                e.getId());
    }

    @Override
    public int delete(Integer id) {
        return execUpdate("DELETE FROM order_items WHERE id = ?", id);
    }

    @Override
    public int count() {
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery("SELECT COUNT(*) FROM order_items");
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi đếm OrderItem", ex);
        } finally {
            close(rs);
        }
    }

    @Override
    public List<OrderItem> findAll() {
        return findBySql(BASE_SQL + " ORDER BY id DESC");
    }

    @Override
    public OrderItem findById(Integer id) {
        List<OrderItem> list = findBySql(BASE_SQL + " WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<OrderItem> findBySql(String sql, Object... value) {
        List<OrderItem> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, value);
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi truy vấn OrderItem", ex);
        } finally {
            close(rs);
        }
    }

    public List<OrderItem> findByOrderId(int orderId) {
        return findBySql(BASE_SQL + " WHERE order_id = ? ORDER BY id ASC", orderId);
    }

    public int deleteByOrderId(int orderId) {
        return execUpdate("DELETE FROM order_items WHERE order_id = ?", orderId);
    }

    private int execUpdate(String sql, Object... args) {
        try {
            return JdbcUtil.executeUpdate(sql, args);
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi cập nhật OrderItem", ex);
        }
    }

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        return OrderItem.builder()
                .id(rs.getInt("id"))
                .orderId(rs.getInt("order_id"))
                .productSizeId(rs.getInt("product_size_id"))
                .quantity(rs.getInt("quantity"))
                .unitPrice(rs.getBigDecimal("unit_price"))
                .sugarLevel(rs.getString("sugar_level"))
                .iceLevel(rs.getString("ice_level"))
                .subtotal(rs.getBigDecimal("subtotal"))
                .build();
    }

    private void close(ResultSet rs) {
        if (rs == null)
            return;
        try {
            Statement st = rs.getStatement();
            try (Connection c = (st != null ? st.getConnection() : null)) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
    }
}