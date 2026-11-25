package utils;

import entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuthUtil {
	public static final String SESSION_USER_KEY = "currentUser";

	public static void setUser(HttpServletRequest req, User user) {
		HttpSession session = req.getSession(true);
		session.setAttribute(SESSION_USER_KEY, user);
	}

	public static User getUser(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return null;
		}
		return (User) session.getAttribute(SESSION_USER_KEY);
	}

	public static void clear(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	public static boolean isAuthenticated(HttpServletRequest req) {
		return getUser(req) != null;
	}

	public static boolean hasRole(HttpServletRequest req, String roleName) {
		User user = getUser(req);
		return user != null && user.getRoleName() != null && user.getRoleName().equalsIgnoreCase(roleName);
	}

	public static boolean isAdmin(HttpServletRequest req) {
		return hasRole(req, "ADMIN");
	}

	public static boolean isStaff(HttpServletRequest req) {
		return hasRole(req, "STAFF");
	}

	public static boolean isCustomer(HttpServletRequest req) {
		return hasRole(req, "CUSTOMER");
	}

	public static String getRedirectUrlByRole(HttpServletRequest req) {
		String ctx = req.getContextPath();
		if (isAdmin(req)) {
			return ctx + "/admin/dashboard";
		}
		if (isStaff(req)) {
			return ctx + "/pos";
		}
		return ctx + "/home"; // Chuyển hướng role khách về home
	}
}
