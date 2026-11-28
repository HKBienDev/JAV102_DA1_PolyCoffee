package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import DAO.CategoryDAO;
import DAO.OrderDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import entity.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // 1. Thống kê Thực đơn (Sản phẩm & Danh mục)
            int totalProducts = productDAO.count();
            int availableProducts = productDAO.findAllAvailable().size();
            int totalCategories = categoryDAO.count();

            // 2. Thống kê Nhân sự (không tính khách hàng CUSTOMER)
            int totalStaff = userDAO.filterStaff(null, null, null).size();

            // 3. Thống kê Đơn hàng & Tổng doanh thu các đơn đã hoàn thành (COMPLETED)
            int totalOrders = orderDAO.count();
            int pendingOrders = orderDAO.findByStatus("PENDING").size();

            BigDecimal totalRevenue = BigDecimal.ZERO;
            List<Order> completedOrders = orderDAO.findByStatus("COMPLETED");
            for (Order order : completedOrders) {
                if (order.getFinalAmount() != null) {
                    totalRevenue = totalRevenue.add(order.getFinalAmount());
                }
            }

            // 4. Lấy 5 đơn hàng mới nhất để hiển thị lên bảng theo dõi nhanh
            List<Order> recentOrders = orderDAO.findByFilter(null, null, null, 1, 5);

            // 5. Tính doanh thu 7 ngày gần nhất để vẽ Biểu đồ cột
            Map<LocalDate, BigDecimal> dailyRevenueMap = new LinkedHashMap<>();
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                dailyRevenueMap.put(today.minusDays(i), BigDecimal.ZERO);
            }

            BigDecimal last7DaysRevenue = BigDecimal.ZERO;
            for (Order order : completedOrders) {
                if (order.getOrderDate() != null && order.getFinalAmount() != null) {
                    LocalDate orderDay = order.getOrderDate().toLocalDateTime().toLocalDate();
                    if (dailyRevenueMap.containsKey(orderDay)) {
                        dailyRevenueMap.put(orderDay, dailyRevenueMap.get(orderDay).add(order.getFinalAmount()));
                        last7DaysRevenue = last7DaysRevenue.add(order.getFinalAmount());
                    }
                }
            }

            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
            StringBuilder labelsJson = new StringBuilder("[");
            StringBuilder valuesJson = new StringBuilder("[");
            int idx = 0;
            for (Map.Entry<LocalDate, BigDecimal> entry : dailyRevenueMap.entrySet()) {
                if (idx > 0) {
                    labelsJson.append(",");
                    valuesJson.append(",");
                }
                labelsJson.append("\"").append(entry.getKey().format(dayFmt)).append("\"");
                valuesJson.append(entry.getValue().toPlainString());
                idx++;
            }
            labelsJson.append("]");
            valuesJson.append("]");

            // 5. Đóng gói dữ liệu gửi sang trang JSP
            req.setAttribute("totalProducts", totalProducts);
            req.setAttribute("availableProducts", availableProducts);
            req.setAttribute("totalCategories", totalCategories);
            req.setAttribute("totalStaff", totalStaff);
            req.setAttribute("totalOrders", totalOrders);
            req.setAttribute("pendingOrders", pendingOrders);
            req.setAttribute("totalRevenue", totalRevenue);
            req.setAttribute("recentOrders", recentOrders);
            req.setAttribute("last7DaysRevenue", last7DaysRevenue);
            req.setAttribute("chartLabelsJson", labelsJson.toString());
            req.setAttribute("chartValuesJson", valuesJson.toString());

        } catch (RuntimeException e) {
            e.printStackTrace();
            req.setAttribute("error", "Không thể tải đầy đủ số liệu thống kê: " + e.getMessage());
        }

        req.getRequestDispatcher("/views/admin-dashboard.jsp").forward(req, resp);
    }
}