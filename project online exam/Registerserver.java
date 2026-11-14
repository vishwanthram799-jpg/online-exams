
import com.sun.net.httpserver.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class Registerserver {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8081), 0);

        server.createContext("/register", exchange -> {

            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String data = new String(
                exchange.getRequestBody().readAllBytes());

            Map<String, String> form = new HashMap<>();

            for (String pair : data.split("&")) {
                String[] p = pair.split("=", 2);
                form.put(
                    URLDecoder.decode(p[0], "UTF-8"),
                    URLDecoder.decode(p[1], "UTF-8")
                );
            }

            String name = form.get("name");
            String email = form.get("email");
            String password = form.get("password");

            String response;

            try {
                Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/exam_portal",
                    "root", "1234");

                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO students(name,email,password) VALUES(?,?,?)");

                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, password);

                ps.executeUpdate();

                response = "Registration Successful!";

                ps.close();
                con.close();

            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }

            byte[] result = response.getBytes("UTF-8");

            exchange.getResponseHeaders().set(
                "Content-Type", "text/plain; charset=UTF-8");

            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*");

            exchange.sendResponseHeaders(200, result.length);

            exchange.getResponseBody().write(result);
            exchange.close();
        });

        server.start();

        System.out.println("Server running at http://localhost:8081");
		server.createContext("/login", exchange -> {

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Origin", "*"
    );

    if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Methods", "POST, OPTIONS"
        );
        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Headers", "Content-Type"
        );
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    String body = new String(
        exchange.getRequestBody().readAllBytes(),
        java.nio.charset.StandardCharsets.UTF_8
    );

    String email = "";
    String password = "";

    for (String pair : body.split("&")) {

        String[] parts = pair.split("=", 2);

        String key = java.net.URLDecoder.decode(
            parts[0], "UTF-8"
        );

        String value = java.net.URLDecoder.decode(
            parts.length > 1 ? parts[1] : "", "UTF-8"
        );

        if (key.equals("email")) {
            email = value;
        }

        if (key.equals("password")) {
            password = value;
        }
    }

    String message;

    try {

        Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/exam_portal",
            "root",
            "1234"
        );

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM students WHERE email=? AND password=?"
        );

        ps.setString(1, email);
        ps.setString(2, password);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            message = "Login Successful!";
        } else {
            message = "Invalid email or password!";
        }

        rs.close();
        ps.close();
        con.close();

    } catch (Exception ex) {
        ex.printStackTrace();
        message = "Database error!";
    }

    byte[] result = message.getBytes(
        java.nio.charset.StandardCharsets.UTF_8
    );

    exchange.sendResponseHeaders(200, result.length);

    exchange.getResponseBody().write(result);
    exchange.close();

});
    }
}
