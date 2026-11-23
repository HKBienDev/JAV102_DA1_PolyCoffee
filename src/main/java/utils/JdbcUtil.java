package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcUtil {
	static String driver = "org.postgresql.Driver";
	static String dburl = "jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require";
	static String username = "postgres.uwxirktukqfmwykzvkbe";
	static String password = "PolyCofee@2026";
    // nạp driver
    static{
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**Mở kết nối*/
    private static Connection getConnection() throws SQLException {
    	return DriverManager.getConnection(dburl, username, password);
    }
    /**Tạo PreparedStatement làm việc với SQL hoặc PROC*/
    private static PreparedStatement createPreStmt(String sql, Object... values) throws SQLException {
    	Connection conn = getConnection();
        PreparedStatement ps = sql.trim().toUpperCase().startsWith("{CALL")
                ? conn.prepareCall(sql)
                : conn.prepareStatement(sql);

        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
        return ps;
    }

    /** Thao tác dữ liệu */
    public static int executeUpdate(String sql, Object... values) throws SQLException {
    	PreparedStatement ps = createPreStmt(sql, values);
        try {
            return ps.executeUpdate();
        } finally {
            if (ps != null) {
                Connection conn = ps.getConnection();
                ps.close();
                if (conn != null) {
                    conn.close();
                }
            }
        }
    }

    /**Truy vấn dữ liệu*/
    public static ResultSet executeQuery(String sql, Object... values) throws SQLException {
    	PreparedStatement ps = createPreStmt(sql, values);
        return ps.executeQuery();
    }
    
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Kết nối Supabase thành công! Database: " + conn.getCatalog());
        } catch (Exception e) {
            System.out.println("Kết nối thất bại!");
            e.printStackTrace();
        }
    }
    
   }


