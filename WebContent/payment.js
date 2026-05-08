setTimeout(() => {
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
            window.location.href = "movies-list.html"; // Ensure correct URL
        }
    });
}, 5000);