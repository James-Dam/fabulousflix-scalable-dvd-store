let insert_stars = $("#insert_stars");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle inserting star response");
    console.log(resultDataJson);
    console.log(resultDataJson["id"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["id"] !== "fail") {
        $("#show_star_id").text("New star added with id " + resultDataJson["id"]);
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        $("#show_star_id").text(resultDataJson["id"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "/cs122b_project1_war/api/insert_star", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: insert_stars.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
insert_stars.submit(submitLoginForm);