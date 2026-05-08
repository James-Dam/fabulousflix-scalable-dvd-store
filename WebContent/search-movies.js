document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("movie_search");

    form.onsubmit = function (event) {
        event.preventDefault();

        // grab form content
        const title = document.getElementById("title").value;
        const year = document.getElementById("year").value;
        const director = document.getElementById("director").value;
        const star = document.getElementById("star").value;

        // pass parameters
        const queryParams = new URLSearchParams();
        if (title) queryParams.append("title", title);
        if (year) queryParams.append("year", year);
        if (director) queryParams.append("director", director);
        if (star) queryParams.append("star_name", star);

        //default search params
        queryParams.append("page", "1");
        queryParams.append("results", "10");
        queryParams.append("order", "ASC");
        queryParams.append("order2", "ASC");
        queryParams.append("first_selection", "title");
        queryParams.append("second_selection", "rating");

        // Redirect to the movie list page with query parameters
        window.location.href = `./movies-list.html?${queryParams.toString()}`;
    };
});


// Autocomplete

const cache = {};

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    if (cache[query]) {
        console.log("Using cached results for:", query);
        console.log("Cached suggestion list:", cache[query]);
        doneCallback({ suggestions: cache[query] });
        return;
    } else {
        console.log("sending AJAX request to backend Java Servlet")
    }

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "autocomplete?query=" + query,
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    // parse the string into JSON
    var jsonData = typeof data === "string" ? JSON.parse(data) : data;

    cache[query] = jsonData;
    console.log("Received suggestion list:", jsonData);

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    window.location.href = "./movie.html?id=" + suggestion["data"]["id"];
}

$('#title').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // additional settings
    deferRequestBy: 300,
    minChars: 3,
    noCache: false,
    lookupLimit: 10
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {

}

// bind pressing enter key to a handler function
$('#title').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})