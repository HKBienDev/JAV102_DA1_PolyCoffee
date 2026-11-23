package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

public class ParamUtil {

	public static String getString(HttpServletRequest req, String name, String defaultValue) {
		String value = req.getParameter(name);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		return value.trim();
	}
	
	public static int getInt (HttpServletRequest req, String name, int defaultValue) {
		String value = req.getParameter(name);
		if(value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	public static int getInt(HttpServletRequest req, String name) {
	    return getInt(req, name, 0);
	}
	
	public static double getDouble(HttpServletRequest req, String name, double defaultValue) {
		String value = req.getParameter(name);
		
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	public static double getDouble (HttpServletRequest req, String name) {
		return getDouble(req, name, 0.0);
	}
	
	public static boolean getBoolean(HttpServletRequest req, String name, boolean defaultValue) {
		String value = req.getParameter(name);
		
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		
		String normalized = value.trim().toLowerCase();
		if ("true".equals(normalized) || "1".equals(normalized) || "on".equals(normalized)) {
			return true;
		}
		
		if ("false".equals(normalized) || "0".equals(normalized) || "off".equals(normalized)) {
			return false;
		}
		return defaultValue;
	}
	
	public static boolean getBoolean(HttpServletRequest req, String name) {
	    return getBoolean(req, name, false);
	}
	
	public static Date getDate(HttpServletRequest req, String name, String pattern) {
	    String value = req.getParameter(name);
	    if (value == null || value.trim().isEmpty()) {
	        return null;
	    }
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
	        sdf.setLenient(false);
	        return sdf.parse(value.trim());
	    } catch (ParseException e) {
	        return null;
	    }
	}
}
