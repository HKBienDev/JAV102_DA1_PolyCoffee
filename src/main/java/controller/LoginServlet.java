package controller;

import java.io.IOException;

import DAO.RoleDAO;
import DAO.UserDAO;
import entity.Role;
import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.ParamUtil;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthUtil.isAuthenticated(req)) {
            redirectByRole(req, resp, AuthUtil.getRole(req));
            return;
        }
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = ParamUtil.getString(req, "username", "");
        String password = req.getParameter("password");
        if (password == null) {
            password = "";
        }

        if (username.isEmpty() || password.isEmpty()) {
            forwardLoginWithError(req, resp, username, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        try {
            User user = userDAO.findByUsername(username);
            if (user == null || user.getPassword() == null || !user.getPassword().equals(password)) {
                forwardLoginWithError(req, resp, username, "Tên đăng nhập hoặc mật khẩu không chính xác!");
                return;
            }

            if (!user.isActive()) {
                forwardLoginWithError(req, resp, username,
                        "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Quản trị viên!");
                return;
            }

            Role role = roleDAO.findById(user.getRoleId());
            String roleName = (role != null && role.getName() != null)
                    ? role.getName().trim().toUpperCase()
                    : "CUSTOMER";

            req.getSession(true);
            req.changeSessionId();
            AuthUtil.setUser(req, user, roleName);

            redirectByRole(req, resp, roleName);

        } catch (RuntimeException e) {
            e.printStackTrace();
            forwardLoginWithError(req, resp, username, "Lỗi kết nối cơ sở dữ liệu, vui lòng thử lại!");
        }
    }

    private void forwardLoginWithError(HttpServletRequest req, HttpServletResponse resp, String username,
            String errorMsg)
            throws ServletException, IOException {
        req.setAttribute("error", errorMsg);
        req.setAttribute("username", username);
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    private void redirectByRole(HttpServletRequest req, HttpServletResponse resp, String roleName) throws IOException {
        String ctx = req.getContextPath();
        if ("ADMIN".equalsIgnoreCase(roleName)) {
            resp.sendRedirect(ctx + "/admin/dashboard");
        } else if ("STAFF".equalsIgnoreCase(roleName)) {
            resp.sendRedirect(ctx + "/pos");
        } else {
            resp.sendRedirect(ctx + "/");
        }
    }

}
