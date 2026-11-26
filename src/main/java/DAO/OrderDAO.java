package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entity.Order;
import utils.JdbcUtil;

public class OrderDAO implements CrudDAO<Order, Integer> {

    private static final String BASE_SQL = "SELECT id, customer_id, staff_id, shift_id, pager_id, order_type, total_amount, "
            + "discount_amount, final_amount, status, payment_method, customer_name, customer_phone, note, order_date "
            + "FROM orders";

    @Override
    public int create(Order e) {
        String sql = "INSERT INTO orders (customer_id, staff_id, shift_id, pager_id, order_type, "
                + "total_amount, discount_amount, final_amount, status, payment_method, "
                + "customer_name, customer_phone, note, order_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE(?, NOW())) RETURNING id";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql,
                    e.getCustomerId(),
                    e.getStaffId(),
                    e.getShiftId(),
                    e.getPagerId(),
                    e.getOrderType(),
                    e.getTotalAmount(),
                    e.getDiscountAmount(),
                    e.getFinalAmount(),
                    e.getStatus(),
                    e.getPaymentMethod(),
                    e.getCustomerName(),
                    e.getCustomerPhone(),
                    e.getNote(),
                    e.getOrderDate());
            if (rs.next()) {
                e.setId(rs.getInt("id"));
                return 1;
            }
            return 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi tạo Order", ex);
        } finally {
            close(rs);
        }
    }

    @Override
    public int update(Order e) {
        String sql = "UPDATE orders SET customer_id=?, staff_id=?, shift_id=?, pager_id=?, order_type=?, "
                + "total_amount=?, discount_amount=?, final_amount=?, status=?, payment_method=?, "
                + "customer_name=?, customer_phone=?, note=? WHERE id=?";
        return execUpdate(sql,
                e.getCustomerId(),
                e.getStaffId(),
                e.getShiftId(),
                e.getPagerId(),
                e.getOrderType(),
                e.getTotalAmount(),
                e.getDiscountAmount(),
                e.getFinalAmount(),
                e.getStatus(),
                e.getPaymentMethod(),
                e.getCustomerName(),
                e.getCustomerPhone(),
                e.getNote(), e.getId());
    }

    @Override
    public int delete(Integer id) {
        return execUpdate("DELETE FROM orders WHERE id = ?", id);
    }

    @Override
    public int count() {
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery("SELECT COUNT(*) FROM orders");
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi đếm Order", ex);
        } finally {
            close(rs);
        }
    }

    @Override
    public List<Order> findAll() {
        return findBySql(BASE_SQL + " ORDER BY order_date DESC, id DESC");
    }

    @Override
    public Order findById(Integer id) {
        List<Order> list = findBySql(BASE_SQL + " WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Order> findBySql(String sql, Object... value) {
        List<Order> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, value);
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi truy vấn Order", ex);
        } finally {
            close(rs);
        }
    }

    public int updateStatus(int id, String status) {
        return execUpdate("UPDATE orders SET status = ? WHERE id = ?", status, id);
    }

    public List<Order> findByStatus(String status) {
        return findBySql(BASE_SQL + " WHERE status = ? ORDER BY order_date DESC, id DESC", status);
    }

    public List<Order> findByShiftId(int shiftId) {
        return findBySql(BASE_SQL + " WHERE shift_id = ? ORDER BY order_date DESC, id DESC", shiftId);
    }

    public List<Order> findByCustomerId(int customerId) {
        return findBySql(BASE_SQL + " WHERE customer_id = ? ORDER BY order_date DESC, id DESC", customerId);
    }

    public List<Order> findByFilter(String keyword, String status, String orderType, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(BASE_SQL + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim() + "%";
            sql.append(" AND (customer_name ILIKE ? OR customer_phone ILIKE ? OR CAST(id AS TEXT) ILIKE ?)");
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        if (orderType != null && !orderType.trim().isEmpty()) {
            sql.append(" AND order_type = ?");
            params.add(orderType.trim());
        }
        int safeSize = Math.max(1, pageSize);
        int offset = (Math.max(1, page) - 1) * safeSize;
        sql.append(" ORDER BY order_date DESC, id DESC LIMIT ? OFFSET ?");
        params.add(safeSize);
        params.add(offset);
        return findBySql(sql.toString(), params.toArray());
    }

    private int execUpdate(String sql, Object... args) {
        try {
            return JdbcUtil.executeUpdate(sql, args);
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi cập nhật Order", ex);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        return Order.builder()
                .id(rs.getInt("id"))
                .customerId(getNullableInt(rs, "customer_id"))
                .staffId(getNullableInt(rs, "staff_id"))
                .shiftId(getNullableInt(rs, "shift_id"))
                .pagerId(getNullableInt(rs, "pager_id"))
                .orderType(rs.getString("order_type"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .discountAmount(rs.getBigDecimal("discount_amount"))
                .finalAmount(rs.getBigDecimal("final_amount"))
                .status(rs.getString("status"))
                .paymentMethod(rs.getString("payment_method"))
                .customerName(rs.getString("customer_name"))
                .customerPhone(rs.getString("customer_phone"))
                .note(rs.getString("note"))
                .orderDate(rs.getTimestamp("order_date"))
                .build();
    }

    private Integer getNullableInt(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
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