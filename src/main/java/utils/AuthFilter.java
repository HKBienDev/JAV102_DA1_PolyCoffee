package utils;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter({ "/admin/*", "/staff/*", "/pos/*", "/bills/*" })
public class AuthFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		if (!AuthUtil.isAuthenticated(req)) {
			resp.sendRedirect(req.getContextPath() + "/login");
			return;
		}

		String uri = req.getRequestURI();
		String ctx = req.getContextPath();

		if (uri.startsWith(ctx + "/admin") && !AuthUtil.isAdmin(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ quản trị viên mới có quyền truy cập!");
			return;
		}

		boolean isStaffArea = uri.startsWith(ctx + "/staff") || uri.startsWith(ctx + "/pos")
				|| uri.startsWith(ctx + "/bills");

		if (isStaffArea && !(AuthUtil.isAdmin(req) || AuthUtil.isStaff(req))) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chức năng chỉ dành cho nhân viên!");
			return;
		}

		chain.doFilter(request, response);
	}
}
