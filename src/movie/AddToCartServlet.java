package movie;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Random;


@WebServlet(name = "AddToCartServlet", urlPatterns = "/api/add-to-cart")
public class AddToCartServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_id = request.getParameter("movie_id");
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

        if (cart == null) {
            cart = new HashMap<>();
            cart.put(movie_id, cart.getOrDefault(movie_id, 0) + 1);
            session.setAttribute("cart", cart);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (cart) {
                cart.put(movie_id, cart.getOrDefault(movie_id, 0) + 1);
            }
        }

        checkPriceColumn();
        assignRandomPrice(movie_id);

        JsonObject responseJsonObject = new JsonObject();
        JsonArray cartJsonArray = new JsonArray();
        cart.forEach((movieId, count) -> {
            JsonObject movieObject = new JsonObject();
            movieObject.addProperty("movie_id", movieId);
            movieObject.addProperty("count", count);
            cartJsonArray.add(movieObject);
        });
        responseJsonObject.add("cart", cartJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }

    protected void checkPriceColumn() {
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_name = 'movies' AND column_name = 'price';";

            PreparedStatement checkPrice = conn.prepareStatement(query);
            ResultSet rs = checkPrice.executeQuery();
            String alterTableQuery = "ALTER TABLE movies ADD COLUMN price Integer DEFAULT NULL;";
            PreparedStatement alterTable = conn.prepareStatement(alterTableQuery);

            if (rs.next() && rs.getInt(1) == 0) {
                alterTable.executeUpdate(alterTableQuery);
                System.out.println("Added price column to movies table.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void assignRandomPrice(String movie_id) {
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT price FROM movies WHERE id = ?;";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, movie_id);

            ResultSet rs = statement.executeQuery();

            if (rs.next() && rs.getInt("price") == 0) {
                Random rand = new Random();
                int randomPrice = 250 + rand.nextInt(750);

                //update price in db
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE movies SET price = ? WHERE id = ?")) {
                    updateStmt.setInt(1, randomPrice);
                    updateStmt.setString(2, movie_id);
                    updateStmt.executeUpdate();
                    System.out.println("assigned price " + randomPrice + " to movie id: " + movie_id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}