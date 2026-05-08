function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function handleResult(resultData) {
    console.log("handleResult: populating movies info from resultData");

    // change the movie title
    let movieTitle = jQuery("#movie_title");

    let rowHTML = "";
    rowHTML += "<h1>" + resultData[0]["movie_title"] + "</h1>";
    movieTitle.append(rowHTML);


    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < Math.min(1, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            resultData[i]["movie_title"] +
            "</th>";

        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        let genresHTML = "";
        for (let genre of resultData[i]["movie_genres"]) {
            const queryParams = new URLSearchParams();
            queryParams.append('genre', genre.id);
            queryParams.append('first_selection', 'title');
            queryParams.append('second_selection', 'rating');
            queryParams.append('page', '1');
            queryParams.append('results', '10');
            queryParams.append('order', 'ASC');
            queryParams.append('order2', 'ASC');

            genresHTML += `<a href="./movies-list.html?${queryParams.toString()}">${genre.name}</a>, `;
        }
        genresHTML = genresHTML.slice(0, -2);
        rowHTML += "<th>" + genresHTML + "</th>";

        let starsHTML = "";
        for (let star of resultData[i]["movie_stars"]) {
            starsHTML += '<a href="./single-star.html?id=' + star.id + '">' + star.name + '</a>, ';
        }
        starsHTML = starsHTML.slice(0, -2);
        rowHTML += "<th>" + starsHTML + "</th>";

        let rating = resultData[i]["movie_rating"];
        rating = rating ? '☆' + rating : "N/A";
        rowHTML += "<th>" + rating + "</th>";
        rowHTML += `<th><button onclick="addToCart('${resultData[i]['movie_id']}', '${resultData[i]['movie_title']}')" class="add-to-cart-btn">Add to Cart</button></th>`;
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

function addToCart(movie_id, movie_title) {
    console.log(`Adding ${movie_title} (ID: ${movie_id}) to cart.`)
    alert(`${movie_title} has been added to your cart!`);

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/add-to-cart",
        data: {movie_id: movie_id},
        success: (response) => {
            console.log("Cart updated successfully:", response);
        },
        error: (err) => {
            console.error("Error adding to cart:", err);
        }
    })
}

function goBackToMovies() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/get-movie-session",
        success: (sessionData) => {
            let params = new URLSearchParams(sessionData);
            window.location.href = "movies-list.html?" + params.toString();
        },
        error: (err) => {
            console.error("Error retrieving session data:", err);
            window.location.href = "movie-list.html";
        }
    });
}

let movieId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});