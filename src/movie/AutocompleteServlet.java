package movie;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

@WebServlet("/autocomplete")
public class AutocompleteServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    private Random random = new Random();

    public void init(ServletConfig config) {
        try {
            if (random.nextBoolean()) {
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
            } else {
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slavedb");
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = dataSource.getConnection()) {
            String query = request.getParameter("query");
            if (query == null || query.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Query parameter is required");
                return;
            }

            // Output stream to STDOUT
            PrintWriter out = response.getWriter();

            String fts = "SELECT * FROM movies WHERE MATCH(title) AGAINST('" + query + "*' IN BOOLEAN MODE) LIMIT 10";

            // Declare our statement
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(fts);

            // Create jsonArray and loop through 10 movies if there are 10
            JsonArray jsonArray = new JsonArray();

            for (int i = 0; i < 10; ++i) {
                if (resultSet.next()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("value", resultSet.getString("title"));

                    JsonObject data = new JsonObject();
                    data.addProperty("id", resultSet.getString("id"));
                    data.addProperty("year", resultSet.getString("year"));
                    data.addProperty("director", resultSet.getString("director"));

                    jsonObject.add("data", data);
                    resultSet.next();
                    jsonArray.add(jsonObject);
                }
            }

            resultSet.close();
            statement.close();

            System.out.println(jsonArray);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error: " + e.getMessage());  // Send error response
        }
    }
}