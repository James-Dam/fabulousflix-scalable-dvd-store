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
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
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
        String first_name = request.getParameter("first");
        String last_name = request.getParameter("last");
        String credit_card_num = request.getParameter("number");
        String expiration_date = request.getParameter("date");
        String customer_id;

        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {

            String checkPayment =
                    "SELECT * " +
                            "FROM customers c " +
                            "JOIN creditcards cc ON c.ccID = cc.id " +
                            "WHERE c.firstName = ? " +
                            "AND c.lastName = ? " +
                            "AND cc.id = ? " +
                            "AND cc.expiration = ?";

            PreparedStatement payment = conn.prepareStatement(checkPayment);

            // Set parameters correctly
            payment.setString(1, first_name);
            payment.setString(2, last_name);
            payment.setString(3, credit_card_num);
            payment.setString(4, expiration_date);

            ResultSet result = payment.executeQuery();

            if (result.next()) {
                customer_id = String.valueOf(result.getInt("id"));
                responseJsonObject.addProperty("status", "success");
            } else {
                throw new Exception("Incorrect payment");
            }

            try (Connection connection = dataSource.getConnection()) {
                String checkColumnExists =
                "SELECT COUNT(*) " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_NAME = 'sales' " +
                "AND COLUMN_NAME = 'quantity';";


                try (PreparedStatement checkStatement = connection.prepareStatement(checkColumnExists);
                     ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) == 0) {
                        String addQuantity = "ALTER TABLE sales ADD COLUMN quantity INTEGER DEFAULT 1";
                        try (PreparedStatement alterStatement = connection.prepareStatement(addQuantity)) {
                            alterStatement.executeUpdate();
                        }
                    }
                }
            }

            // If everything is good, then we update the database
            HttpSession session = request.getSession();
            HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

            java.sql.Date saleDate = new java.sql.Date(System.currentTimeMillis());
            String insertSalesSQL = "INSERT INTO sales (customerID, movieID, saleDate, quantity) VALUES (?, ?, ?, ?)";

            List<Integer> saleIds = (List<Integer>) session.getAttribute("saleIds");
            if (saleIds == null) {
                saleIds = new ArrayList<>();
            }

            try (Connection conns = dataSource.getConnection()) {
                for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                    String movie_id = entry.getKey();
                    int count = entry.getValue();

                    try (PreparedStatement statement = conns.prepareStatement(insertSalesSQL, Statement.RETURN_GENERATED_KEYS)) {
                        statement.setString(1, customer_id);
                        statement.setString(2, movie_id);
                        statement.setString(3, saleDate.toString());
                        statement.setInt(4, count);
                        statement.executeUpdate();

                        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int saleId = generatedKeys.getInt(1);
                                saleIds.add(saleId);
                            }
                        }
                    }
                }

                session.setAttribute("saleIds", saleIds);
            }
        } catch (Exception e) {
            responseJsonObject.addProperty("error", "Invalid payment");
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}