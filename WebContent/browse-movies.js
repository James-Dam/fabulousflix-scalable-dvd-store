function search_title(letter) {
    const queryParams = new URLSearchParams();
    queryParams.append('browse_letter', letter);

    //default search params
    queryParams.append("page", "1");
    queryParams.append("results", "10");
    queryParams.append("order", "ASC");
    queryParams.append("order2", "ASC");
    queryParams.append("first_selection", "title");
    queryParams.append("second_selection", "title");

    window.location.href = `./movies-list.html?${queryParams.toString()}`;
}

function search_genre(letter) {
    const queryParams = new URLSearchParams();
    queryParams.append('genre', letter);

    //default search params
    queryParams.append("page", "1");
    queryParams.append("results", "10");
    queryParams.append("order", "ASC");
    queryParams.append("order2", "ASC");
    queryParams.append("first_selection", "title");
    queryParams.append("second_selection", "rating");

    window.location.href = `./movies-list.html?${queryParams.toString()}`;
}

function handleResult(resultData) {
    let genresDiv = jQuery("#genres");

    resultData.forEach((genre) => {
        let genreSpan = jQuery("<span></span>")
            .addClass("genre")
            .text(genre.name)
            .click(() => search_genre(genre.id));

        genresDiv.append(genreSpan);
    });
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genre", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});