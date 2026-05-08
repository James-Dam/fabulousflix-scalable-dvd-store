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


@WebServlet(name = "RemoveFromCartServlet", urlPatterns = "/api/remove-from-cart")
public class RemoveFromCartServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String movieId = request.getParameter("movie_id");

        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(movieId)) {
            cart.remove(movieId); // Remove movie from cart
            session.setAttribute("cart", cart);
        }

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