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

@WebServlet(name = "InsertMovieServlet", urlPatterns = "/api/insert_movie")
public class InsertMovieServlet extends HttpServlet {
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
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String bYear = request.getParameter("bYear"); // Optional birth year
        String genre = request.getParameter("genre");

        String starId = null;
        String newMovieId = null;
        String genreId = null;

        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            // Generate a new movie ID
//            String getMaxMovieId = "SELECT MAX(id) FROM movies WHERE id LIKE 'tt%'";
//            try (PreparedStatement getMovieId = conn.prepareStatement(getMaxMovieId);
//                 ResultSet executeMovieId = getMovieId.executeQuery()) {
//                if (executeMovieId.next() && executeMovieId.getString(1) != null) {
//                    String lastId = executeMovieId.getString(1);
//                    int numericPart = Integer.parseInt(lastId.substring(2)) + 1;
//                    newMovieId = String.format("tt%07d", numericPart);
//                }
//            }

            // Call stored procedure
            String insertMovie = "CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            CallableStatement insert = conn.prepareCall(insertMovie);
            insert.setString(1, title);
            insert.setInt(2, Integer.parseInt(year));
            insert.setString(3, director);
            insert.setString(4, star);

            if (bYear == null || bYear.isEmpty()) {
                insert.setNull(5, Types.INTEGER);
            } else {
                insert.setInt(5, Integer.parseInt(bYear));
            }

            insert.setString(6, genre);
            // Register OUT parameters
            insert.registerOutParameter(7, Types.VARCHAR); // p_out_movie_id
            insert.registerOutParameter(8, Types.VARCHAR); // p_out_status
            insert.registerOutParameter(9, Types.VARCHAR); // star_status
            insert.registerOutParameter(10, Types.VARCHAR); // genre_status

            insert.execute();

            String movieId = insert.getString(7);
            String status = insert.getString(8);
            String starStatus = insert.getString(9);
            String genreStatus = insert.getString(10);

            System.out.println(movieId + " " + status + " " + starStatus + " " + genreStatus);

            // Get the Star ID after inserting
            String getStarIdQuery = "SELECT id FROM stars WHERE name = ? LIMIT 1";
            try (PreparedStatement getStarId = conn.prepareStatement(getStarIdQuery)) {
                getStarId.setString(1, star);
                try (ResultSet executeStarId = getStarId.executeQuery()) {
                    if (executeStarId.next()) {
                        starId = executeStarId.getString("id");
                    }
                }
            }

            // Get the Genre ID after inserting
            String getGenreIdQuery = "SELECT id FROM genres WHERE name = ? LIMIT 1";
            try (PreparedStatement getGenreId = conn.prepareStatement(getGenreIdQuery)) {
                getGenreId.setString(1, genre);
                try (ResultSet executeGenreId = getGenreId.executeQuery()) {
                    if (executeGenreId.next()) {
                        genreId = executeGenreId.getString("id");
                    }
                }
            }

            // Send response back to JavaScript
            responseJsonObject.addProperty("status", status);
            responseJsonObject.addProperty("movieid", movieId);
            responseJsonObject.addProperty("starid", starId);
            responseJsonObject.addProperty("genreid", genreId);
            responseJsonObject.addProperty("starStatus", starStatus);
            responseJsonObject.addProperty("genreStatus", genreStatus);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", e.getMessage());
            request.getServletContext().log("Couldn't add new movie", e);
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}
