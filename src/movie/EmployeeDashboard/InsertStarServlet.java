package EmployeeDashboard;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "InsertStarServlet", urlPatterns = "/api/insert_star")
public class InsertStarServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String year = request.getParameter("year");

        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {

            // First we get the max id from the stars table
            String getMaxId = "SELECT MAX(id) FROM stars WHERE id LIKE 'nm%'";

            PreparedStatement getId = conn.prepareStatement(getMaxId);
            ResultSet executeId = getId.executeQuery();

            String newId = "nm0000001"; // Default ID if table is empty
            if (executeId.next()) {
                String lastId = executeId.getString(1);
                int numericPart = Integer.parseInt(lastId.substring(2)) + 1; // Extract number and increment
                newId = String.format("nm%07d", numericPart); // Format as nmXXXXXXX
            }

            // Declare our statement
            String insertStar =
                    "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";

            PreparedStatement insert = conn.prepareStatement(insertStar);
            insert.setString(1, newId);
            insert.setString(2, name);

            // Handle optional birthYear
            if (year == null || year.isEmpty()) {
                insert.setNull(3, Types.INTEGER);
            } else {
                insert.setInt(3, Integer.parseInt(year));
            }

            int executeInsertion = insert.executeUpdate();

            if (executeInsertion > 0) {
                responseJsonObject.addProperty("id", newId);
            } else {
                responseJsonObject.addProperty("id", "fail");
            }

            // Insert success:
            // get the new star id and send it back

        } catch (Exception e) {
            // Login fail
            responseJsonObject.addProperty("id", e.getMessage());
            // Log to localhost log
            request.getServletContext().log("Couldn't add new star");
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}