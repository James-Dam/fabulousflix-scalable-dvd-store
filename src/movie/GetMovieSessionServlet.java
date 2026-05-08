package movie;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@WebServlet(name = "GetMovieSessionServlet", urlPatterns = "/api/get-movie-session")
public class GetMovieSessionServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession();
        HashMap<String, String> movieListParams = (HashMap<String, String>) session.getAttribute("movieListParams");

        PrintWriter out = response.getWriter();
        if (movieListParams != null) {
            Gson gson = new Gson();
            out.write(gson.toJson(movieListParams));
        } else {
            out.write("{}"); // Return empty JSON if no session data exists
        }
        response.setStatus(200);
        out.close();
    }
}
