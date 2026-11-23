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

@WebFilter({"/admin/*", "/staff/*", "/pos/*", "/bills/*"})
public class AuthFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		if (!AuthUtil.isAuthenticated(req)) {
			resp.sendRedirect(req.getContextPath()+ "/login");
			return;
		}
		
		String uri = req.getRequestURI();
		String adminPrefix = req.getContextPath() + "/admin";
		if (uri.startsWith(adminPrefix) && !AuthUtil.isAdmin(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này!");
			return;
		}
		
		chain.doFilter(request, response);
	}
}
