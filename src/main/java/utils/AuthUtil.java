package utils;

import entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuthUtil {
	public static final String SESSION_USER_KEY = "currentUser";
	public static final String SESSION_ROLE_KEY = "currentRole";

	public static void setUser(HttpServletRequest req, User user) {
		HttpSession session = req.getSession(true);
		session.setAttribute(SESSION_USER_KEY, user);
	}

	public static void setUser(HttpServletRequest req, User user, String roleName) {
		HttpSession session = req.getSession(true);
		session.setAttribute(SESSION_USER_KEY, user);
		session.setAttribute(SESSION_ROLE_KEY, roleName != null ? roleName.trim().toUpperCase() : "");
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

	public static String getRole(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return null;
		}
		return (String) session.getAttribute(SESSION_ROLE_KEY);
	}

	public static boolean isAuthenticated(HttpServletRequest req) {
		User user = getUser(req);
		return user != null && user.isActive();
	}

	public static boolean isAdmin(HttpServletRequest req) {
		return isAuthenticated(req) && "ADMIN".equalsIgnoreCase(getRole(req));
	}

	public static boolean isStaffOrAdmin(HttpServletRequest req) {
		String role = getRole(req);
		return isAuthenticated(req) && ("ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role));
	}
}
