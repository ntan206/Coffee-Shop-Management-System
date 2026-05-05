package connectdb;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    public static Connection con = null;

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // Driver cho MySQL
        Class.forName("com.mysql.cj.jdbc.Driver");

        // URL kết nối MySQL (ví dụ với freesqldatabase)
        String url = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12824805?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Ho_Chi_Minh&useSSL=false";
        String user = "sql12824805";   // user do freesqldatabase cấp
        String password = "hp1EwZD5Q1"; // thay bằng mật khẩu thực tế

        con = DriverManager.getConnection(url, user, password);
        return con;
    }

//	public static Connection getConnect() throws SQLException, ClassNotFoundException {
//		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//		String url = "jdbc:sqlserver://localhost:1433;databaseName=sql12824805;encrypt=false;trustServerCertificate=true";
//		String user = "sa";
//		String password = "sapassword";
//		con = DriverManager.getConnection(url, user, password);
//		return con;
//	}
//	
    
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
