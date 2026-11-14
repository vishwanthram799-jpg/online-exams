import java.sql.*;

class TestConnections {
    public static void main(String[] args) {
        try {
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/exam_portal",
                "root",
                "1234"
            );

            System.out.println("Database Connected Successfully!");

            con.close();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}