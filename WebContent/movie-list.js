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
    console.log("Populating results");

    // Populate the movies list
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movies_list_body");

    // Clear the body
    movieTableBodyElement.empty();
    let i = 0;
    // Concatenate the html tags with resultData jsonObject to create table rows
    while(i < resultData.length) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            '<a href="./movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        let genresHTML = "";
        for (let genre of resultData[i]["movie_genres"]) {
            console.log(genre);
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
        i++;
    }

    const nextPageBtn = document.getElementById("next_page");
    const prevPageBtn = document.getElementById("prev_page");

    const urlParams = new URLSearchParams(window.location.search);
    const maxResults = parseInt(urlParams.get('results')) || 10; // Default to 10 if missing
    const page = parseInt(urlParams.get('page')) || 1;

    prevPageBtn.disabled = page === 1;
    nextPageBtn.disabled = resultData.length < maxResults;
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

let title = getParameterByName("title");
let year = getParameterByName("year");
let director = getParameterByName("director");
let star_name = getParameterByName("star_name");
let browse_letter = getParameterByName("browse_letter");
let genre = getParameterByName("genre");
let results = getParameterByName("results") || 10;
let page = parseInt(getParameterByName("page")) || 1;
let order = getParameterByName("order");
let order2 = getParameterByName("order2");
let first_selection = getParameterByName("first_selection");
let second_selection = getParameterByName("second_selection");

console.log("Title: " + title);
console.log("Year: " + year);
console.log("Director: " + director);
console.log("Star: " + star_name);
console.log("Browse Letter: " + browse_letter);
console.log("Genre: " + genre);
console.log("Results: " + results);
console.log("Page: " + page);

document.getElementById("results").value = results;

// Prepare the data to send in the request
let data = { results };

// Pages should always be in the data request
data.results = results;
data.page = page;
data.order = order;
data.order2 = order2;
data.first_selection = first_selection;
data.second_selection = second_selection;

if (browse_letter) {
    data.browse_letter = browse_letter;
} else if (genre) {
    data.genre = genre;
} else {
    if (title) data.title = title;
    if (year) data.year = year;
    if (director) data.director = director;
    if (star_name) data.star_name = star_name;
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movie-list",
    data: data,
    success: (resultData) => handleResult(resultData)
});

// Pagination
document.getElementById("pagination_form").addEventListener("click", function(event) {
    event.preventDefault();

    let params = new URLSearchParams(window.location.search);

    let results = params.get("results") || "10"

    // Determine which button was clicked
    let clickedButton = event.target.value;

    if (clickedButton === "next") {
        page++; // Increment page number
    } else if (clickedButton === "prev" && page > 1) {
        page--; // Decrement page number
    }

    params.set("page", page); // Update page in URL parameters
    params.set("results", results); // Keep results per page the same

    console.log(`Page changed to: ${page}`);

    // Update the URL and fetch new data
    window.history.replaceState({}, "", "?" + params.toString());

    // Fetch new results with updated page number
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movie-list",
        data: Object.fromEntries(params.entries()), // Send updated params
        success: (resultData) => handleResult(resultData)
    });
});


// Change results per page
document.getElementById("display_results").addEventListener("submit", function(event) {
    event.preventDefault();

    let selectedResults = document.getElementById("results").value;
    let params = new URLSearchParams(window.location.search);
    params.set("results", selectedResults);
    page = 1;
    params.set("page", page)

    console.log("Updated results:", selectedResults);

    // Update the data object with the new pages value
    data.pages = selectedResults;

    window.history.replaceState({}, "", "?" + params.toString());

    // Send AJAX request with updated parameters
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movie-list",
        data: Object.fromEntries(params.entries()),
        success: (resultData) => handleResult(resultData)
    });
});

// Ordering
document.getElementById("order_results").addEventListener("submit", function(event) {
    event.preventDefault();

    let params = new URLSearchParams(window.location.search);

    // Retrieve the selected sorting values
    let order = $("#order").val();
    let order2 = $("#order2").val();
    let first_value = $("#first_selection").val();
    let second_value = $("#second_selection").val();

    let results = params.get("results") || 10;

    // Set the new sorting parameters
    params.set("order", order);
    params.set("order2", order2)
    params.set("first_selection", first_value);
    params.set("second_selection", second_value);
    params.set("results", results);

    // Keep existing pagination parameters
    let page = params.get("page") || 1;
    params.set("page", page);

    console.log(`Sorting changed to: Order - ${order}, First - ${first_value}, Order2 - ${order2}, Second - ${second_value}, Results - ${results}`);

    // Update the URL without refreshing
    window.history.replaceState({}, "", "?" + params.toString());

    // Fetch new sorted results
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movie-list",
        data: Object.fromEntries(params.entries()), // Send updated params
        success: (resultData) => handleResult(resultData)
    });
});

