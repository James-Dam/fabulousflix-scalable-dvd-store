# Fabflix — Scalable Java/MySQL DVD E-Commerce Platform

Fabflix is a database-backed DVD e-commerce platform built as part of a UC Irvine database systems course. The project focuses on backend development, relational database design, search, authentication/security, performance optimization, and scalable deployment.

## Technical Highlights

- Built a Java/Tomcat backend with MySQL for a movie e-commerce platform
- Implemented full-text search, fuzzy matching, autocomplete, browsing, pagination, and cart/session functionality
- Improved backend performance using JDBC connection pooling and MySQL master-slave replication
- Routed read queries across master/slave databases while sending writes to the master database
- Containerized the application using Docker and deployed a multi-service architecture with Kubernetes
- Added HTTPS support, reCAPTCHA, prepared statements, and SQL injection prevention

## Tech Stack

Java, MySQL, Apache Tomcat, JDBC, JavaScript, HTML/CSS, Docker, Kubernetes, AWS

## Architecture

Client → Load Balancer → Tomcat Services → JDBC Connection Pool → MySQL Master/Slave

## My Contributions

- Set up JDBC/MySQL connectivity and implemented database-backed servlet logic
- Built login, payment, pagination, filtering, and session persistence features
- Added HTTPS support, reCAPTCHA, and employee dashboard functionality
- Created stored procedure support for movie insertion
- Implemented full-text search and autocomplete for movie title search
- Helped set up Docker, Kubernetes pods, and the multi-service architecture

## Demo Videos

- Project 1: Core movie browsing
- Project 2: Search, cart, checkout, and sessions
- Project 3: XML parsing, dashboard, and security
- Project 4: Connection pooling, replication, and load balancing
- Project 5: Docker/Kubernetes multi-service deployment

## Documentation

- [Course Milestones](docs/course-milestones.md)
- [XML Parser Report](docs/xml-parser-report.md)
- [Scalability and Deployment](docs/scalability-and-deployment.md)
- [Security and Database Notes](docs/security-and-database-notes.md)
- [API Endpoints](docs/api-endpoints.md)
- [Contributions](docs/contributions.md)

## Course Context

This was a team project for a UC Irvine database systems course. Some deployment files reference course-specific or cloud-specific environments.
