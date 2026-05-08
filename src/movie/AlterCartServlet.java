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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AlterCartServlet", urlPatterns = "/api/alter-cart")
public class AlterCartServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieId = request.getParameter("movie_id");
        int increase = Integer.parseInt(request.getParameter("increase"));

        HttpSession session = request.getSession();
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

        int currentQuantity = cart.getOrDefault(movieId, 0);

        if (increase == 1) {
            cart.put(movieId, currentQuantity + 1);
        } else if (increase == 0 && currentQuantity > 1) {
            cart.put(movieId, currentQuantity - 1);
        }

        session.setAttribute("cart", cart);

        JsonObject responseJsonObject = new JsonObject();
        JsonArray cartJsonArray = new JsonArray();
        cart.forEach((movie_id, count) -> {
            JsonObject movieObject = new JsonObject();
            movieObject.addProperty("movie_id", movieId);
            movieObject.addProperty("count", count);
            cartJsonArray.add(movieObject);
        });
        responseJsonObject.add("cart", cartJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }
}