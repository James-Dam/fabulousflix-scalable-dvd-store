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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.*;

// Declaring a WebServlet called MovieListServlet, which maps to url "/api/movie-list"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Get current session with the parameters
        HttpSession session = request.getSession();

        //get parameters
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star_name = request.getParameter("star_name");
        String browse_letter = request.getParameter("browse_letter");
        String genre = request.getParameter("genre");
        String results = request.getParameter("results");
        String page = request.getParameter("page");
        String order = request.getParameter("order").toUpperCase();
        String order2 = request.getParameter("order2").toUpperCase();
        String first_selection = request.getParameter("first_selection");
        String second_selection = request.getParameter("second_selection");

        // Default values for null parameters
        if (results == null) results = "10";
        if (page == null) page = "1";

        // Store parameters in session
        HashMap<String, String> movieListParams = new HashMap<>();
        movieListParams.put("title", title);
        movieListParams.put("year", year);
        movieListParams.put("director", director);
        movieListParams.put("star_name", star_name);
        movieListParams.put("browse_letter", browse_letter);
        movieListParams.put("genre", genre);
        movieListParams.put("results", results);
        movieListParams.put("page", page);
        movieListParams.put("order", order);
        movieListParams.put("order2", order2);
        movieListParams.put("first_selection", first_selection);
        movieListParams.put("second_selection", second_selection);

        session.setAttribute("movieListParams", movieListParams);

        // The log message can be found in localhost log
        request.getServletContext().log("getting title: " + title);
        request.getServletContext().log("getting year: " + year);
        request.getServletContext().log("getting director: " + director);
        request.getServletContext().log("getting star: " + star_name);
        request.getServletContext().log("getting results" + results);
        request.getServletContext().log("getting page" + page);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        System.out.println("getting movie list page");
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // query
            StringBuilder query = new StringBuilder("SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating ");
            List<Object> params = new ArrayList<>();

            if (genre != null && !genre.isEmpty()) {
                query.append("FROM movies m ");
                query.append("JOIN genres_in_movies g ON g.movieId = m.id ");
                query.append("LEFT JOIN ratings r ON m.id = r.movieId ");
                query.append("WHERE g.genreId = ? ");
                params.add(genre);
            } else {
                if (star_name != null && !star_name.isEmpty()) {
                    query.append("FROM movies m ");
                    query.append("JOIN stars_in_movies sm ON sm.movieId = m.id ");
                    query.append("JOIN stars s ON s.id = sm.starId ");
                    query.append("LEFT JOIN ratings r ON m.id = r.movieId ");
                    query.append("WHERE s.name LIKE ? ");
                    params.add("%" + star_name + "%");

                } else {
                    query.append("FROM movies m ");
                    query.append("LEFT JOIN ratings r ON m.id = r.movieId ");
                    query.append("WHERE 1 = 1 ");
                }

                if (browse_letter != null && !browse_letter.isEmpty()) {
                    if (browse_letter.equals("*")) {
                        query.append("AND m.title REGEXP '^[^a-zA-Z0-9]' ");
                    } else {
                        query.append("AND m.title LIKE ? ");
                        params.add(browse_letter + "%");
                    }
                }

                if (title != null && !title.isEmpty()) {
                    // Tokenizes by whitespace
                    String[] words = title.trim().split("\\s+");

                    StringBuilder searchQuery = new StringBuilder();
                    for (String word : words) {
                        searchQuery.append("+").append(word).append("* ");
                    }

                    query.append("AND MATCH(m.title) AGAINST(? IN BOOLEAN MODE) ");
                    params.add(searchQuery.toString().trim());
                }

                if (year != null && !year.isEmpty()) {
                    query.append("AND m.year = ? ");
                    params.add(Integer.parseInt(year));
                }

                if (director != null && !director.isEmpty()) {
                    query.append("AND m.director LIKE ? ");
                    params.add("%" + director + "%");
                }

                if (star_name != null && !star_name.isEmpty()) {
                    query.append("AND s.name LIKE ? ");
                    params.add("%" + star_name + "%");
                }
            }

            // Find what to plan query around
            System.out.println(first_selection + second_selection);
            if (first_selection.equals("title")) {
                query.append("ORDER BY m.title");
            } else {
                query.append("ORDER BY r.rating");
            }

            query.append(" ").append(order);

            if (second_selection.equals("title")) {
                query.append(", m.title");
            } else {
                query.append(", r.rating");
            }

            query.append(" ").append(order2);
            query.append(" LIMIT ").append(results);
            int resultsPerPage = Integer.parseInt(results);
            int pageNumber = Integer.parseInt(page);
            int offset = (pageNumber - 1) * resultsPerPage;

            query.append(" OFFSET ").append(offset);

            System.out.println(query);
            PreparedStatement statement = conn.prepareStatement(query.toString());

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    statement.setInt(i + 1, (Integer) params.get(i));
                } else {
                    statement.setString(i + 1, (String) params.get(i));
                }
            }

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);

                // Query for first 3 stars
                String starsQuery =
                        "SELECT s.id, s.name, COUNT(sim2.movieId) AS movie_count " +
                                "FROM stars s " +
                                "JOIN stars_in_movies sim ON s.id = sim.starId " +
                                "JOIN stars_in_movies sim2 ON s.id = sim2.starId " +
                                "WHERE sim.movieId = ? " +
                                "GROUP BY s.id, s.name " +
                                "ORDER BY movie_count DESC, s.name ASC " +
                                "LIMIT 3;";

                PreparedStatement starsStmt = conn.prepareStatement(starsQuery);
                starsStmt.setString(1, movie_id);
                ResultSet starsRs = starsStmt.executeQuery();

                JsonArray starsJsonArray = new JsonArray();
                while (starsRs.next()) {
                    JsonObject starJson = new JsonObject();
                    starJson.addProperty("id", starsRs.getString("id"));
                    starJson.addProperty("name", starsRs.getString("name"));
                    starsJsonArray.add(starJson);
                }

                // Query for first 3 genres
                String genresQuery =
                        "SELECT g.id, g.name " +
                        "FROM genres g " +
                        "JOIN genres_in_movies gim ON g.id = gim.genreId " +
                        "WHERE gim.movieId = ? " +
                        "ORDER BY g.name ASC " +
                        "LIMIT 3;";


                PreparedStatement genresStmt = conn.prepareStatement(genresQuery);
                genresStmt.setString(1, movie_id);
                ResultSet genresRs = genresStmt.executeQuery();

                JsonArray genresJsonArray = new JsonArray();
                while (genresRs.next()) {
                    JsonObject genreJsonObject = new JsonObject();
                    genreJsonObject.addProperty("id", genresRs.getString("id"));
                    genreJsonObject.addProperty("name", genresRs.getString("name"));
                    genresJsonArray.add(genreJsonObject);
                }

                // Add the stars and genres to the main movie JSON object
                jsonObject.add("movie_stars", starsJsonArray);
                jsonObject.add("movie_genres", genresJsonArray);

                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}