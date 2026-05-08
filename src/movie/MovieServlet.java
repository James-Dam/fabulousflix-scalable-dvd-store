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
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "MovieServlet", urlPatterns = "/api/movie")
public class MovieServlet extends HttpServlet {
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

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        System.out.println("getting movie info");
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE m.id = ? " +
                    "ORDER BY r.rating DESC;";


            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            while (rs.next()) {

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs

                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }

            String starsQuery =
                    "SELECT s.id, s.name, COUNT(sim2.movieId) AS movie_count " +
                            "FROM stars s " +
                            "JOIN stars_in_movies sim ON s.id = sim.starId " +
                            "JOIN stars_in_movies sim2 ON s.id = sim2.starId " +
                            "WHERE sim.movieId = ? " +
                            "GROUP BY s.id, s.name " +
                            "ORDER BY movie_count DESC, s.name ASC;";



            PreparedStatement starsStmt = conn.prepareStatement(starsQuery);
            starsStmt.setString(1, id);
            ResultSet starsRs = starsStmt.executeQuery();

            JsonArray starsJsonArray = new JsonArray();
            while (starsRs.next()) {
                JsonObject starJson = new JsonObject();
                starJson.addProperty("id", starsRs.getString("id"));
                starJson.addProperty("name", starsRs.getString("name"));
                starsJsonArray.add(starJson);
            }

            // Query all the genres
            String genresQuery =
                    "SELECT g.id, g.name " +
                            "FROM genres g " +
                            "JOIN genres_in_movies gim ON g.id = gim.genreId " +
                            "WHERE gim.movieId = ? " +
                            "ORDER BY g.name ASC;";

            PreparedStatement genresStmt = conn.prepareStatement(genresQuery);
            genresStmt.setString(1, id);
            ResultSet genresRs = genresStmt.executeQuery();

            JsonArray genresJsonArray = new JsonArray();
            while (genresRs.next()) {
                JsonObject genreJsonObject = new JsonObject();
                genreJsonObject.addProperty("id", genresRs.getString("id"));
                genreJsonObject.addProperty("name", genresRs.getString("name"));
                genresJsonArray.add(genreJsonObject);
            }

            jsonObject.add("movie_stars", starsJsonArray);
            jsonObject.add("movie_genres", genresJsonArray);
            jsonArray.add(jsonObject);

            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
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
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}