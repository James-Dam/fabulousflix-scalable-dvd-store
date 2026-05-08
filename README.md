
<li>Project 1: https://www.youtube.com/watch?v=vZTTsqC6jso&t=19s&ab_channel=ColinHarrison </li>
<li>Project 2: https://www.youtube.com/watch?v=e4ULRRYqZZ4&t=2s</li>
<li>Project 3: https://www.youtube.com/watch?v=qYTMTcwylLU&ab_channel=ColinHarrison</li>
<li>Project 4: https://youtu.be/ftSkSJjwR7s</li>
<li>Project 5: https://youtu.be/ftSkSJjwR7s</li>

<h2>Project 2 substring implementation</h2>
<p>Used %search% format for all string searches, finding all entries containing string search substring</p>
<p>Used browse_letter% format for all browsing by letter, finding all entries starting with browse letter</p>

<h2>Project 3 XML Parsing Optimizations</h2>
<p>Naive approach: ~ 100 - 140 seconds</p>
<p>1. Removed subqueries for checking existing movies and stars multiple times, instead all queried once and stored in hash sets/tables for future efficient access. saved ~ 5-10 seconds</p>
<p>2. Used batch queries with auto commit off to insert 500 records on each insert. greatly reduced time by ~ 80 - 100 seconds </p>
<p>3. Used SAX parsing instead of DOM parsing so we don't need to store entire XML files in memory</p>
<p>Final XML Parser time: ~ 12 - 20 seconds</p>

<h2>Project 3 Inconsistency Report</h2>
<p>Number of stars inserted: 6838</p>
<p>Number of duplicate stars names skipped: 24</p>
<p>Number of genres inserted: 104</p>
<p>Number of movies inserted: 7457</p>
<p>Number of duplicate movies: 27</p>
<p>Number of inconsistent movies: 305</p>
<p>Number of movies with no stars: 4326</p>
<p>Number of movies with no genres: 3216</p>
<p>Number of genres in movies inserted: 7186</p>
<p>Number of inconsistent genres in movies not inserted: 2650</p>
<p>Number of stars in movies inserted: 31305</p>
<p>Number of movies not found for stars in movies: 1566</p>
<p>Number of stars not found for stars in movies: 15816</p>
<p>Number of duplicate stars in movies: 251</p>
<p>(specific inconsistencies reported in files during parser execution, shown at end of demo)</p>

<h2>Project 3 Prepared Statement files</h2>
<p>InsertMovieServlet.java</p>
<p>InsertStarServlet.java</p>
<p>AddToCartServlet.java</p>
<p>EmployeeLoginServlet.java</p>
<p>LoginServlet.java</p>
<p>MovieListServlet.java</p>
<p>MovieServlet.java</p>
<p>PaymentConfirmationServlet.java</p>
<p>PaymentServlet.java</p>
<p>StarServlet.java</p>

<h2>Project 4 Connection Pooling </h2>
<p>Configuration path: WebContent/META-INF/context.xml</p>
<p>Connection pooling used in all servlets with prepared statements, files stated above</p>
<p>Instead of creating a new connection for every query, we use a set of pre-established connections, our connection pool. Each servlet grabs a connection from the pool when they need it, and return it back to the pool when done instead of closing it</p>
<p>With two backend sql instances, we create two seperate connection pools, as defined in our context.xml, one for each backend db. When servlets grab a connection they grab from either of the connection pools depending on their needs</p>

<h2>Project 4 Master-Slave </h2>
<p>Configuration path: WebContent/META-INF/context.xml</p>
<p>Code for determing route is in all servlets with prepared statements, files stated above</p>
<p>All queries that write to the database in any way are routed to the master instance. If the query is just reading from db, the query is randomly routed to either the slave or master</p>

<h2>Project 5 Endpoints</h2>
<p>Served by fabflix-login: /api/login, /api/employee_login</p>
<p>Served by fabflix-movie: everything else</p>

<h2>Contributions:</h2>
<br>
Colin Harrison
<ul>
    <p>Project 1:</p>
    <li>Worked on star servlet, movie-list servlet, movie servlet </li>
    <li>Created SQL query to pull from mysql database </li>
    <li>Worked on javascript files for movie list, movie, and star web pages</li>
    <li>Stylized tables using css for movie list, star, and movie web pages</li>
    <li>Created project 1 demonstration video</li>
    <br>
    <p>Project 2:</p>
    <li>Implemented searching and browsing functionalities</li>
    <li>Implemented shopping cart and confirmation page with session</li>
    <li>Applied css to beautify login, search, browse, movie, and shopping cart pages </li>
    <li>Did genre hyperlinks, fixed errors with pagination and ordering </li>
    <li>Implemented login filter</li>
    <br>
    <p>Project 3:</p>
    <li>Implemented XML Parsing</li>
    <li>Optimized XML Parsing</li>
    <li>Registered and setup domain name</li>
    <li>Setup https and hosting on aws instance, recorded demo video</li>
    <li>Fixed CSS for employee dashboard and subpages</li>
    <br>
    <p>Project 4:</p>
    <li>Setup JDBC connection pooling to both backend databases</li>
    <li>Implemented Master-Slave replication on backend databases</li>
    <li>Implemented routing to slave and master backend databases in code</li>
    <li>Scaled website with apache2 load balancers on aws and gcp virtual machines</li>
    <li>Recorded demonstration video</li>
    <br>
    <p>Project 5:</p>
    <li>Set up Docker and Dockerfile for usage</li>
    <li>Set up kubernetes and kubernetes pods</li>
    <li>Recorded demonstration video</li>
    <li>Added yaml files</li>
    <li>Changed Fabflix to multi-service architecture</li>
</ul>
<br>
<br>

James Dam:
<ul>
    <p>Project 1:</p>
    <li>Set up JDBC connection with mysql database</li>
    <li>Worked on star servlet, movie-list servlet, movie servlet</li>
    <li>Created html pages for movie-list, movie, star</li>
    <li>Worked on javascript files for movie list, movie, and star web pages</li>
    <li>Created css files and started stylizing backgrounds and headers with css</li>
    <br>
    <p>Project 2:</p>
    <li>Implemented Login page and checks for user in the database</li>
    <li>Implemented session saving for movies-list page</li>
    <li>Wrote queries for login, payment</li>
    <li>Implemented pagination and filtering with prev/next pages</li>
    <li>Stored parameters in URL for use in queries</li>
    <br>
     <p>Project 3:</p>
    <li>Implemented https files</li>
    <li>Added REcaptcha for user and employee login</li>
    <li>Set up Stored Procedure for movie insertion</li>
    <li>Created Employee dashboard for employee access with metadata and star insertion and movie insertion</li>
    <li>Created CSS for employee dashboard</li>
    <br>
    <p>Project 4:</p>
    <li>Implemented full text search on movie title search</li>
    <li>Implemented autocomplete on movie title search</li>
    <li>Added extra constraints on jQuery autocomplete to meet requirements</li>
    <br>
    <p>Project 5:</p>
    <li>Worked on setting up Docker</li>
    <li>Set up kubernetes and kubernetes pods</li>
    <li>Changed Fabflix to multi-service architecture</li>
</ul>
