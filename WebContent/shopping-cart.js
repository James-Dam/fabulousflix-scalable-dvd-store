function handleResult(resultData) {
    console.log("handleResult: populating shopping cart data");

    // Find the empty table body by id cart_table_body
    let movieTableBodyElement = jQuery("#cart_table_body");
    let total = 0;

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < Math.min(100, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            '<a href="./movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +
            '</a>' +
            "</th>"

        rowHTML +=
            `<th>
                <button onClick="decreaseCount('${resultData[i]["movie_id"]}')">-</button>
                <span style="margin: 0 10px;">${resultData[i]["count"]}</span>
                <button onClick="increaseCount('${resultData[i]["movie_id"]}')">+</button>
            </th>`;

        rowHTML +=
            "<th>" +
            resultData[i]["price"] +
            "</th>";

        rowHTML +=
            "<th>" +
            resultData[i]["total"] +
            "</th>";

        rowHTML +=
            `<td>
                <button onClick="removeFromCart('${resultData[i]["movie_id"]}')">Delete</button>
            </td>`;

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
        total += resultData[i]["total"]
    }

    jQuery("#total_amount").text(`Total: $${total}`);
}

function removeFromCart(movieId) {
    console.log(`Removing movie with ID: ${movieId} from cart`);

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/remove-from-cart",
        data: { movie_id: movieId },
        success: (resultData) => {
            alert("Movie removed from cart!");
            location.reload();
        }
    });
}

function decreaseCount(movieId) {
    console.log(`decrementing count of movie with ID: ${movieId} from cart`);

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/alter-cart",
        data: { movie_id: movieId, increase: 0 },
        success: (resultData) => {
            location.reload();
        }
    });
}

function increaseCount(movieId) {
    console.log(`incrementing count of movie with ID: ${movieId} from cart`);

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/alter-cart",
        data: { movie_id: movieId, increase: 1 },
        success: (resultData) => {
            location.reload();
        }
    });
}

function proceedToPayment() {
    let totalAmount = document.getElementById('total_amount').textContent;
    totalAmount = totalAmount.replace('Total: $', '');
    window.location.href = 'payment-page.html?total=' + encodeURIComponent(totalAmount);
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET", // Setting request method
    url: "api/shopping-cart",
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

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