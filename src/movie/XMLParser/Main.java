package XMLParser;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws SQLException, NamingException, ClassNotFoundException {
        long startTime = System.currentTimeMillis();
        String jdbcURL = "jdbc:mysql://localhost:3306/moviedb";
        String username = "mytestuser";
        String password = "My6$Password";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password)) {
            // parse for stars from actors63.xml
            StarParser starParse = new StarParser();
            starParse.runExample();

            List<Star> stars = starParse.myStars;
            HashSet<String> starNames = new HashSet<>();

            String insertStars = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            PreparedStatement starsStatement = conn.prepareStatement(insertStars);
            int numStarInserted = 0;
            int numDuplicateStars = 0;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("DuplicateStars.txt"))) {

                int count = 0;
                conn.setAutoCommit(false);

                for (Star s : stars) {
                    String starName = s.getStarName();
                    String starId = s.getId();

                    if (!starNames.contains(starName)) {
                        starNames.add(starName);
                    } else {
                        numDuplicateStars++;
                        writer.write("Duplicate Star: Star ID: " + starId + ", Star Name: " + starName + "\n");
                        continue;
                    }

                    starsStatement.setString(1, starId);
                    starsStatement.setString(2, starName);
                    if (s.getDob() == 0) {
                        starsStatement.setNull(3, java.sql.Types.INTEGER);
                    } else {
                        starsStatement.setInt(3, s.getDob());
                    }

                    numStarInserted++;
                    count++;
                    starsStatement.addBatch();

                    if (count % 500 == 0) {
                        starsStatement.executeBatch();
                        starsStatement.clearBatch();
                        count = 0;
                    }
                }

                if (count != 0) {
                    starsStatement.executeBatch();
                    starsStatement.clearBatch();
                }

                conn.commit();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
            System.out.println("Number of stars inserted: " + numStarInserted);
            System.out.println("Number of duplicate stars names skipped: " + numDuplicateStars);
            starsStatement.close();


            // now we parse for stars in movies from cast124.xml
            StarInMovieParser starInMovieParse = new StarInMovieParser();
            starInMovieParse.runExample();

            List<StarInMovie> starsInMovies = starInMovieParse.myStarInMovies;

            HashSet<String> moviesWithStars = new HashSet<>();
            for (StarInMovie s : starsInMovies) {
                if (starNames.contains(s.getStarName())) {
                    moviesWithStars.add(s.getMovieId());
                }
            }

            // parse for movies, genres, genres in movies from main124.xml
            MovieParser movieParse = new MovieParser();
            movieParse.runExample();

            List<Movie> movies = movieParse.myMovies;
            HashSet<String> genres = new HashSet<>();
            HashMap<String, List<String>> genre_to_movie = new HashMap<>();

            for (Movie movie : movies) {
                List<String> movie_genres = movie.getGenres();
                String movieId = movie.getId();
                if (movie_genres != null && !movie_genres.isEmpty()) {
                    for (String genre : movie_genres) {
                        genres.add(genre.toLowerCase());
                    }
                    genre_to_movie.put(movieId, movie_genres);
                }
            }

            String getMovies = "SELECT id FROM movies";
            PreparedStatement getMoviesStatement = conn.prepareStatement(getMovies);
            ResultSet moviesRS = getMoviesStatement.executeQuery();

            HashSet<String> movieIds = new HashSet<>();

            while (moviesRS.next()) {
                String movieId = moviesRS.getString("id");
                movieIds.add(movieId);
            }
            moviesRS.close();
            getMoviesStatement.close();

            String insertGenreQuery = "INSERT INTO genres (name) VALUES (?)";
            PreparedStatement genreInsert = conn.prepareStatement(insertGenreQuery);
            int num_genres = 0;

            for (String genre : genres) {
                genreInsert.setString(1, genre);
                genreInsert.executeUpdate();
                num_genres++;
            }
            System.out.println("Number of genres inserted: " + num_genres);
            genreInsert.close();

            String insertMovieQuery = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
            PreparedStatement movieStmt = conn.prepareStatement(insertMovieQuery);
            int num_movies_inserted = 0;
            int num_inconsistent_movies = 0;
            int num_duplicate_movies = 0;
            int numMoviesNoStars = 0;
            int numMoviesNoGenres = 0;

            try (BufferedWriter duplicateWriter = new BufferedWriter(new FileWriter("DuplicateMovies.txt"));
                 BufferedWriter inconsistentWriter = new BufferedWriter(new FileWriter("InconsistentMovies.txt"));
                 BufferedWriter noGenreWriter = new BufferedWriter(new FileWriter("MoviesNoGenre.txt"))) {

                int count = 0;
                conn.setAutoCommit(false);

                for (Movie movie : movies) {
                    String movieId = movie.getId();
                    String title = movie.getMovieName();
                    int year = movie.getYear();
                    String director = movie.getDirector();
                    List<String> movie_genres = movie.getGenres();

                    if (director != null && (director.toLowerCase().contains("unknown") || director.toLowerCase().contains("unyear") || director.toLowerCase().contains("null"))) {
                        num_inconsistent_movies++;
                        inconsistentWriter.write("Inconsistent Movie: Movie ID: " + movieId + ", Title: " + title + ", Director: " + director + ", Year: " + year + "\n");
                        continue;
                    }

                    if (movieId == null || movieId.isEmpty() || title == null || title.isBlank() || director == null || director.isEmpty() || year < 1888 || year > 2020) {
                        num_inconsistent_movies++;
                        inconsistentWriter.write("Inconsistent Movie: Movie ID: " + movieId + ", Title: " + title + ", Director: " + director + ", Year: " + year + "\n");
                        continue;
                    }

                    if (movie_genres == null || movie_genres.isEmpty()) {
                        numMoviesNoGenres++;
                        noGenreWriter.write("Movie with No Genres: Movie ID: " + movieId + ", Title: " + title + ", Director: " + director + ", Year: " + year + "\n");
                    }

                    if (!moviesWithStars.contains(movieId)) {
                        numMoviesNoStars++;
                        continue;
                    }

                    if (movieIds.contains(movieId)) {
                        num_duplicate_movies++;
                        duplicateWriter.write("Duplicate Movie: Movie ID: " + movieId + ", Title: " + title + ", Director: " + director + ", Year: " + year + "\n");
                        continue;
                    }

                    movieIds.add(movieId);
                    movieStmt.setString(1, movieId);
                    movieStmt.setString(2, title);
                    movieStmt.setInt(3, year);
                    movieStmt.setString(4, director);

                    movieStmt.addBatch();
                    count++;
                    num_movies_inserted++;

                    if (count % 500 == 0) {
                        movieStmt.executeBatch();
                        movieStmt.clearBatch();
                        count = 0;
                    }
                }

                if (count != 0) {
                    movieStmt.executeBatch();
                    movieStmt.clearBatch();
                }

                conn.commit();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
            System.out.println("Number of movies inserted: " + num_movies_inserted);
            System.out.println("Number of duplicate movies: " + num_duplicate_movies);
            System.out.println("Number of inconsistent movies: " + num_inconsistent_movies);
            System.out.println("Number of movies with no stars: " + numMoviesNoStars);
            System.out.println("Number of movies with no genres: " + numMoviesNoGenres);
            movieStmt.close();

            String fetchGenresQuery = "SELECT id, name FROM genres";
            PreparedStatement fetchGenresStmt = conn.prepareStatement(fetchGenresQuery);
            ResultSet genreRs = fetchGenresStmt.executeQuery();

            HashMap<String, Integer> genreNameToId = new HashMap<>();

            while (genreRs.next()) {
                int genreId = genreRs.getInt("id");
                String genreName = genreRs.getString("name");
                genreNameToId.put(genreName, genreId);
            }
            genreRs.close();
            fetchGenresStmt.close();

            String insertGenreInMovieQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
            PreparedStatement insertGenreInMovieStmt = conn.prepareStatement(insertGenreInMovieQuery);

            int num_genre_mappings_inserted = 0;
            int num_genre_mappings_not_inserted = 0;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("InconsistentMovieGenreInMovies.txt"))) {

                int count = 0;
                conn.setAutoCommit(false);

                for (Map.Entry<String, List<String>> entry : genre_to_movie.entrySet()) {
                    String movieId = entry.getKey();
                    if (movieId == null) {
                        continue;
                    }

                    for (String genre : entry.getValue()) {
                        int genreId = genreNameToId.get(genre);

                        if (movieIds.contains(movieId)) {
                            insertGenreInMovieStmt.setInt(1, genreId);
                            insertGenreInMovieStmt.setString(2, movieId);
                            insertGenreInMovieStmt.executeUpdate();
                            num_genre_mappings_inserted++;
                            count++;
                        } else {
                            num_genre_mappings_not_inserted++;
                            writer.write("Genre in Movie, Movie not consistent: Movie ID: " + movieId + " Genre: " + genre + "\n");
                        }

                        if (count % 500 == 0) {
                            insertGenreInMovieStmt.executeBatch();
                            insertGenreInMovieStmt.clearBatch();
                            count = 0;
                        }
                    }
                }

                if (count != 0) {
                    insertGenreInMovieStmt.executeBatch();
                    insertGenreInMovieStmt.clearBatch();
                }

                conn.commit();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
            System.out.println("Number of genres in movies inserted: " + num_genre_mappings_inserted);
            System.out.println("Number of inconsistent genres in movies not inserted: " + num_genre_mappings_not_inserted);
            insertGenreInMovieStmt.close();

            String getStars = "SELECT id, name FROM stars";
            PreparedStatement getStarsStatement = conn.prepareStatement(getStars);
            ResultSet starsRS = getStarsStatement.executeQuery();

            HashMap<String, String> starNameToId = new HashMap<>();

            while (starsRS.next()) {
                String starId = starsRS.getString("id");
                String starName = starsRS.getString("name");
                starNameToId.put(starName, starId);
            }
            starsRS.close();
            getStarsStatement.close();

            int starsInMoviesInserted = 0;
            int moviesNotFound = 0;
            int starsNotFound = 0;
            int numDulicateStarInMovies = 0;

            String insertStarsInMovies = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
            PreparedStatement insertStarsInMoviesStatement = conn.prepareStatement(insertStarsInMovies);

            HashSet<String> seenPairs = new HashSet<>();

            try (BufferedWriter starsNotFoundWriter = new BufferedWriter(new FileWriter("StarsInMovieStarNotFound.txt"));
                 BufferedWriter moviesNotFoundWriter = new BufferedWriter(new FileWriter("StarInMovieMovieNotFound.txt"));
                 BufferedWriter duplicateStarsInMoviesWriter = new BufferedWriter(new FileWriter("DuplicateStarsInMovies.txt"))) {

                int count = 0;
                conn.setAutoCommit(false);

                for (StarInMovie sim : starsInMovies) {
                    String starName = sim.getStarName();
                    String movieId = sim.getMovieId();

                    if (!starNameToId.containsKey(starName)) {
                        starsNotFound++;
                        starsNotFoundWriter.write("Star in Movie: Star not found: " + starName + "\n");
                        continue;
                    }

                    if (!movieIds.contains(movieId)) {
                        moviesNotFound++;
                        moviesNotFoundWriter.write("Star in Movie: Movie not found: " + movieId + "\n");
                        continue;
                    }

                    String simString = starName + "-" + movieId;
                    if (seenPairs.contains(simString)) {
                        numDulicateStarInMovies++;
                        duplicateStarsInMoviesWriter.write("Duplicate star in movie: Star name: " + starName + ", Movie ID: " + movieId + "\n");
                        continue;
                    }

                    seenPairs.add(simString);
                    String starId = starNameToId.get(starName);
                    insertStarsInMoviesStatement.setString(1, starId);
                    insertStarsInMoviesStatement.setString(2, movieId);
                    insertStarsInMoviesStatement.addBatch();
                    count++;
                    starsInMoviesInserted++;

                    if (count % 500 == 0) {
                        insertStarsInMoviesStatement.executeBatch();
                        insertStarsInMoviesStatement.clearBatch();
                        count = 0;
                    }
                }

                if (count != 0) {
                    insertStarsInMoviesStatement.executeBatch();
                    insertStarsInMoviesStatement.clearBatch();
                }

                conn.commit();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
            System.out.println("Number of stars in movies inserted: " + starsInMoviesInserted);
            System.out.println("Number of movies not found for stars in movies: " + moviesNotFound);
            System.out.println("Number of stars not found for stars in movies: " + starsNotFound);
            System.out.println("Number of duplicate stars in movies: " + numDulicateStarInMovies);
            insertStarsInMoviesStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long durationInMillis = endTime - startTime;
        System.out.println("Execution time: " + durationInMillis + " ms");
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
