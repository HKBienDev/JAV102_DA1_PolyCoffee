package controller;

import java.io.IOException;

import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.ParamUtil;

@WebServlet({ "/login", "/logout" })
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/logout".equals(path)) {
            AuthUtil.clear(req);
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (AuthUtil.isAuthenticated(req)) {
            resp.sendRedirect(AuthUtil.getRedirectUrlByRole(req));
            return;
        }

        req.getRequestDispatcher("/views/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = ParamUtil.getString(req, "username");
        String password = ParamUtil.getString(req, "password");

        if (username.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            req.setAttribute("username", username);
            req.getRequestDispatcher("/views/auth/login.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.findByUserName(username);

        if (user == null || !user.getPassword().equals(password)) {
            req.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            req.setAttribute("username", username);
            req.getRequestDispatcher("/views/auth/login.jsp").forward(req, resp);
            return;
        }

        if (!user.isActive()) {
            req.setAttribute("error", "Tài khoản của bạn hiện đang bị khóa!");
            req.setAttribute("username", username);
            req.getRequestDispatcher("/views/auth/login.jsp").forward(req, resp);
            return;
        }

        AuthUtil.setUser(req, user);
        resp.sendRedirect(AuthUtil.getRedirectUrlByRole(req));
    }
}
