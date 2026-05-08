function handleMetaData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle metadata response", resultDataJson);

    if (resultDataJson["status"] === "ok") {
        let metadataTable = $("#metadata");
        metadataTable.empty(); // Clear previous data
        metadataTable.append(`<div class='metadata-label'>METADATA</div>`);

        // Iterate over each table in the JSON response
        Object.keys(resultDataJson["tables"]).forEach((tableName) => {
            // Add a table label
            metadataTable.append(`<div class='table-label'>${tableName}</div>`);

            // Create table structure
            let tableHTML = `<table>
                <tr><th>Column Name</th><th>Data Type</th></tr>`;

            // Iterate through columns for this table
            resultDataJson["tables"][tableName].forEach((column) => {
                tableHTML += `<tr>
                    <td>${column["column_name"]}</td>
                    <td>${column["data_type"]}</td>
                </tr>`;
            });

            tableHTML += `</table>`;
            metadataTable.append(tableHTML);
        });
    } else {
        console.log("Failed to retrieve metadata");
        $("#metadata").html(`<p style="color: red;">Error: ${resultDataJson["status"]}</p>`);
    }
}

// Call Servlet
$.ajax({
    url: "/cs122b_project1_war/api/metadata",
    method: "GET",
    success: handleMetaData
});
