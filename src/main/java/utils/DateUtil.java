package utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
	public static final String DEFAULT_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
	public static final String HTML_DATE_FORMAT = "yyyy-MM-dd";
	
	public static Date parse (String dateStr, String pattern) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			sdf.setLenient(false);
			return sdf.parse(dateStr.trim());
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static String format(Date date, String pattern) {
		if (date == null) {
			return "";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	
	public static String format(Date date) {
		return format (date, DEFAULT_DATE_FORMAT);
	}
	
	public static Date now() {
		return new Date();
	}
	
	public static Date addDays(Date date, int days) {
		if (date == null) {
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, days);
		return cal.getTime();
	}
	
	public static java.sql.Date toSqlDate (Date date) {
		return date == null ? null : new java.sql.Date(date.getTime());
	}
	
	public static Timestamp toTimestamp(Date date) {
		return date == null ? null : new Timestamp(date.getTime());
	}
}
