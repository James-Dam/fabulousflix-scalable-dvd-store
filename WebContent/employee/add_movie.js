let insertMovie = $("#insert_movie");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleMovieResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle inserting movie response");
    console.log(resultDataJson);
    $("#status").html("");

    console.log(resultDataJson["status"] + " " + resultDataJson["starStatus"] + " " + resultDataJson["genreStatus"]);

    let statusMessage = "";

    // If successful, it will update the user
    if (resultDataJson["status"] === "INSERTED") {
        statusMessage += "New movie added with id: " + resultDataJson["movieid"] + "<br>";
    } else if (resultDataJson["status"] === "EXISTS") {
        statusMessage += "Movie already exists with ID: " + resultDataJson["movieid"] + "<br>";
        $("#status").html(statusMessage);
        return;
    }
    if (resultDataJson["starStatus"] === "INSERTED") {
        statusMessage += "New star added with id: " + resultDataJson["starid"] + "<br>";
    } else if (resultDataJson["starStatus"] === "EXISTS") {
        statusMessage += "Star already exists with id: " + resultDataJson["starid"] + " (linked to movie)<br>";
    }

    if (resultDataJson["genreStatus"] === "INSERTED") {
        statusMessage += "New genre added with id: " + resultDataJson["genreid"];
    } else if (resultDataJson["genreStatus"] === "EXISTS") {
        statusMessage += "Genre already exists with id: " + resultDataJson["genreid"] + " (linked to movie)";
    }

    $("#status").html(statusMessage);

}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit movie form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "/cs122b_project1_war/api/insert_movie", {
            method: "POST",
            // Serialize the form to the data sent by POST request
            data: insertMovie.serialize(),
            success: handleMovieResult
        }
    );
}

// Bind the submit action of the form to a handler function
insertMovie.submit(submitLoginForm);