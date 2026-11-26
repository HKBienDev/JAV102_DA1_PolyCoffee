package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import DAO.RoleDAO;
import DAO.UserDAO;
import entity.Role;
import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.AuthUtil;
import utils.ParamUtil;

@WebServlet({ "/user-manage", "/admin/user-manage" })
public class UserManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            Object msg = session.getAttribute("flashMessage");
            Object err = session.getAttribute("flashError");
            if (msg != null) {
                req.setAttribute("message", msg);
                session.removeAttribute("flashMessage");
            }
            if (err != null) {
                req.setAttribute("error", err);
                session.removeAttribute("flashError");
            }
        }

        try {
            String action = ParamUtil.getString(req, "action", "");
            if ("edit".equals(action)) {
                int id = ParamUtil.getInt(req, "id", 0);
                User editUser = userDAO.findById(id);
                if (editUser != null) {
                    req.setAttribute("form", editUser);
                    req.setAttribute("isEdit", true);
                } else {
                    req.setAttribute("error", "Không tìm thấy nhân viên có ID = " + id);
                }
            }

            loadPageData(req);
        } catch (RuntimeException e) {
            e.printStackTrace();
            req.setAttribute("error", "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
        req.getRequestDispatcher("/views/user-manage.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ParamUtil.getString(req, "action", "");
        try {
            switch (action) {
                case "create" -> handleCreate(req, resp);
                case "update" -> handleUpdate(req, resp);
                case "change-password" -> handleChangePassword(req, resp);
                case "toggle-active" -> handleToggleActive(req, resp);
                case "delete" -> handleDelete(req, resp);
                default -> resp.sendRedirect(getBaseUrl(req));
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            setFlashError(req, "Thao tác thất bại: " + e.getMessage());
            resp.sendRedirect(getBaseUrl(req));
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = ParamUtil.getString(req, "username", "");
        String password = ParamUtil.getString(req, "password", "");
        String fullName = ParamUtil.getString(req, "fullName", "");
        String email = ParamUtil.getString(req, "email", "");
        String phone = ParamUtil.getString(req, "phone", "");
        int roleId = ParamUtil.getInt(req, "roleId", 0);
        boolean active = ParamUtil.getBoolean(req, "active", true);
        User formUser = User.builder()
                .username(username)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .roleId(roleId)
                .active(active)
                .loyaltyPoints(0)
                .build();

        String validationError = validateInput(username, password, fullName, email, phone, roleId, true);
        if (validationError == null) {
            if (userDAO.existsByUsername(username)) {
                validationError = "Tên đăng nhập '" + username + "' đã tồn tại!";
            } else if (!email.isEmpty() && userDAO.existsByEmail(email)) {
                validationError = "Email '" + email + "' đã được sử dụng!";
            }
        }
        if (validationError != null) {
            req.setAttribute("error", validationError);
            req.setAttribute("form", formUser);
            req.setAttribute("isEdit", false);
            loadPageData(req);
            req.getRequestDispatcher("/views/user-manage.jsp").forward(req, resp);
            return;
        }
        formUser.setPassword(password);
        userDAO.create(formUser);
        setFlashMessage(req, "Thêm nhân viên '" + fullName + "' thành công!");
        resp.sendRedirect(getBaseUrl(req));
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = ParamUtil.getInt(req, "id", 0);
        User existing = userDAO.findById(id);
        if (existing == null) {
            setFlashError(req, "Không tìm thấy nhân viên cần cập nhật!");
            resp.sendRedirect(getBaseUrl(req));
            return;
        }
        String fullName = ParamUtil.getString(req, "fullName", "");
        String email = ParamUtil.getString(req, "email", "");
        String phone = ParamUtil.getString(req, "phone", "");
        int roleId = ParamUtil.getInt(req, "roleId", 0);
        boolean active = ParamUtil.getBoolean(req, "active", true);

        User currentUser = AuthUtil.getUser(req);
        if (currentUser != null && currentUser.getId() == id && !active) {
            active = true;
            setFlashError(req, "Bạn không thể tự khóa tài khoản đang đăng nhập của chính mình!");
            resp.sendRedirect(getBaseUrl(req) + "?action=edit&id=" + id);
            return;
        }
        User formUser = User.builder()
                .id(id)
                .username(existing.getUsername())
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .roleId(roleId)
                .active(active)
                .loyaltyPoints(existing.getLoyaltyPoints())
                .build();
        String validationError = validateInput(existing.getUsername(), "skip-pass", fullName, email, phone, roleId,
                false);
        if (validationError == null && !email.isEmpty() && userDAO.existsByEmailExceptId(email, id)) {
            validationError = "Email '" + email + "' đã thuộc về tài khoản khác!";
        }
        if (validationError != null) {
            req.setAttribute("error", validationError);
            req.setAttribute("form", formUser);
            req.setAttribute("isEdit", true);
            loadPageData(req);
            req.getRequestDispatcher("/views/user-manage.jsp").forward(req, resp);
            return;
        }
        userDAO.update(formUser);
        setFlashMessage(req, "Cập nhật thông tin nhân viên '" + fullName + "' thành công!");
        resp.sendRedirect(getBaseUrl(req));
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = ParamUtil.getInt(req, "id", 0);
        String newPassword = ParamUtil.getString(req, "newPassword", "");
        String confirmPassword = ParamUtil.getString(req, "confirmPassword", "");
        if (id <= 0 || userDAO.findById(id) == null) {
            setFlashError(req, "Không tìm thấy nhân viên để đổi mật khẩu!");
            resp.sendRedirect(getBaseUrl(req));
            return;
        }
        if (newPassword.length() < 6) {
            setFlashError(req, "Mật khẩu mới phải có ít nhất 6 ký tự!");
            resp.sendRedirect(getBaseUrl(req) + "?action=edit&id=" + id);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            setFlashError(req, "Mật khẩu xác nhận không khớp!");
            resp.sendRedirect(getBaseUrl(req) + "?action=edit&id=" + id);
            return;
        }
        userDAO.updatePassword(id, newPassword);
        setFlashMessage(req, "Đã đặt lại mật khẩu thành công cho nhân viên #" + id + "!");
        resp.sendRedirect(getBaseUrl(req));
    }

    private void handleToggleActive(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = ParamUtil.getInt(req, "id", 0);
        User currentUser = AuthUtil.getUser(req);
        if (currentUser != null && currentUser.getId() == id) {
            setFlashError(req, "Không thể tự khóa tài khoản đang đăng nhập của chính bạn!");
            resp.sendRedirect(getBaseUrl(req));
            return;
        }
        User target = userDAO.findById(id);
        if (target == null) {
            setFlashError(req, "Không tìm thấy nhân viên!");
            resp.sendRedirect(getBaseUrl(req));
            return;
        }
        boolean nextStatus = !target.isActive();
        userDAO.updateActive(id, nextStatus);
        String statusText = nextStatus ? "Mở khóa" : "Khóa";
        setFlashMessage(req, "Đã " + statusText.toLowerCase() + " tài khoản '" + target.getUsername() + "'!");
        resp.sendRedirect(getBaseUrl(req));
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = ParamUtil.getInt(req, "id", 0);
        User currentUser = AuthUtil.getUser(req);
        if (currentUser != null && currentUser.getId() == id) {
            setFlashError(req, "Không thể tự xóa tài khoản đang đăng nhập!");
            resp.sendRedirect(getBaseUrl(req));
            return;
        }
        try {
            userDAO.delete(id);
            setFlashMessage(req, "Đã xóa nhân viên #" + id + " khỏi hệ thống!");
        } catch (RuntimeException e) {

            setFlashError(req,
                    "Không thể xóa cứng vì nhân viên này đã có dữ liệu ca làm/hóa đơn. Vui lòng dùng nút Khóa tài khoản!");
        }
        resp.sendRedirect(getBaseUrl(req));
    }

    private void loadPageData(HttpServletRequest req) {
        List<Role> allRoles = roleDAO.findAll();
        List<Role> staffRoles = new ArrayList<>();
        Map<Integer, String> roleMap = new HashMap<>();
        for (Role r : allRoles) {
            roleMap.put(r.getId(), r.getName());
            if (r.getName() != null && !"CUSTOMER".equalsIgnoreCase(r.getName().trim())) {
                staffRoles.add(r);
            }
        }
        List<User> allStaff = userDAO.filterStaff(null, null, null);
        int totalStaff = allStaff.size();
        int activeStaff = 0;
        for (User u : allStaff) {
            if (u.isActive()) {
                activeStaff++;
            }
        }
        int lockedStaff = totalStaff - activeStaff;
        String keyword = ParamUtil.getString(req, "keyword", "");
        int roleId = ParamUtil.getInt(req, "roleId", 0);
        String status = ParamUtil.getString(req, "status", "all");
        Boolean activeFilter = null;
        if ("active".equalsIgnoreCase(status)) {
            activeFilter = true;
        } else if ("locked".equalsIgnoreCase(status)) {
            activeFilter = false;
        }

        List<User> filteredUsers;
        if (keyword.isEmpty() && roleId <= 0 && activeFilter == null) {
            filteredUsers = allStaff;
        } else {
            filteredUsers = userDAO.filterStaff(keyword, roleId > 0 ? roleId : null, activeFilter);
        }

        req.setAttribute("users", filteredUsers);
        req.setAttribute("staffRoles", staffRoles);
        req.setAttribute("roleMap", roleMap);
        req.setAttribute("totalStaff", totalStaff);
        req.setAttribute("activeStaff", activeStaff);
        req.setAttribute("lockedStaff", lockedStaff);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedRoleId", roleId);
        req.setAttribute("selectedStatus", status);
    }

    private String validateInput(String username, String password, String fullName,
            String email, String phone, int roleId, boolean isCreate) {
        if (isCreate && (username.isEmpty() || !username.matches("^[a-zA-Z0-9._-]{3,30}$"))) {
            return "Tên đăng nhập phải từ 3–30 ký tự, không dấu và không chứa khoảng trắng!";
        }
        if (isCreate && password.length() < 6) {
            return "Mật khẩu khởi tạo phải có ít nhất 6 ký tự!";
        }
        if (fullName.isEmpty()) {
            return "Họ và tên nhân viên không được để trống!";
        }
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return "Định dạng Email không hợp lệ!";
        }
        if (!phone.isEmpty() && !phone.matches("^0\\d{9}$")) {
            return "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0!";
        }
        if (roleId <= 0) {
            return "Vui lòng chọn chức vụ cho nhân viên!";
        }
        return null;
    }

    private String getBaseUrl(HttpServletRequest req) {
        return req.getContextPath() + req.getServletPath();
    }

    private void setFlashMessage(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flashMessage", message);
    }

    private void setFlashError(HttpServletRequest req, String error) {
        req.getSession(true).setAttribute("flashError", error);
    }
}
