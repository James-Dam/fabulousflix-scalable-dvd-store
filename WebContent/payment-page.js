let payment_form = $("#payment_form");

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

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handlePaymentEvent(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("success.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["error"]);
        $("#error").text(resultDataJson["error"]);
    }
}

/**
 * Submit the form content with POST method
 * @param paymentSubmitEvent
 */
function submitPaymentForm(paymentSubmitEvent) {
    console.log("submit payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    paymentSubmitEvent.preventDefault();

    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentEvent
        }
    );
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

let total = getParameterByName("total");
if (total) {
    document.getElementById("total_price").textContent = "Total Price: $" + total;
}

// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);