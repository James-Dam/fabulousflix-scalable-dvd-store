# Security and Database Notes

## Prepared Statements

Prepared statements were used across database-backed servlet files to reduce SQL injection risk.

Relevant files:

- InsertMovieServlet.java
- InsertStarServlet.java
- AddToCartServlet.java
- EmployeeLoginServlet.java
- LoginServlet.java
- MovieListServlet.java
- MovieServlet.java
- PaymentConfirmationServlet.java
- PaymentServlet.java
- StarServlet.java

## Authentication and Security Features

- Added HTTPS support
- Added reCAPTCHA for user and employee login
- Used prepared statements for database-backed queries
- Added employee dashboard access controls
- Added stored procedure support for movie insertion
