package movie;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "PaymentConfirmationServlet", urlPatterns = "/api/payment-confirmation")
public class PaymentConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        HttpSession session = request.getSession();
        List<Integer> saleIds = (List<Integer>) session.getAttribute("saleIds");

        if (saleIds == null || saleIds.isEmpty()) {
            // If no sales exist, return an empty array
            response.getWriter().write("[]");
            response.setStatus(200);
            return;
        }

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            JsonArray cartJsonArray = new JsonArray();

            String query = "SELECT s.id AS sale_id, m.id AS movie_id, m.title AS movie_title, s.quantity, m.price " +
                    "FROM sales s " +
                    "JOIN movies m ON s.movieId = m.id " +
                    "WHERE s.id = ?";

            for (int saleId : saleIds) {
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setInt(1, saleId);
                    ResultSet rs = statement.executeQuery();

                    if (rs.next()) {
                        JsonObject movieObject = new JsonObject();
                        movieObject.addProperty("sale_id", rs.getInt("sale_id"));
                        movieObject.addProperty("movie_id", rs.getString("movie_id"));
                        movieObject.addProperty("movie_title", rs.getString("movie_title"));
                        movieObject.addProperty("quantity", rs.getInt("quantity"));
                        movieObject.addProperty("price", rs.getInt("price"));
                        movieObject.addProperty("total", rs.getInt("price") * rs.getInt("quantity"));
                        cartJsonArray.add(movieObject);
                    }
                }
            }


            // Write JSON string to output
            out.write(cartJsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            session.setAttribute("saleIds", null);
            session.setAttribute("cart", null);
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}