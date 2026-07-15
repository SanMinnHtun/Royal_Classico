## Royal Classico FC Management System ⚽

### Welcome to the `Royal_Classico` repository! This project is a web application designed for the administration and management of a football club, "Royal Classico FC" which is an amateur club . It features a public-facing interface for news and updates, along with a hidden administrative dashboard for managing various aspects of the club.

## Table of Contents

- [Project Title & Description](#project-title--description)
- [Key Features & Benefits](#key-features--benefits)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Prerequisites & Dependencies](#prerequisites--dependencies)
- [Installation & Setup Instructions](#installation--setup-instructions)
  - [Build the Application](#build-the-application)
  - [Run with Docker](#run-with-docker)
  - [Run Locally (without Docker)](#run-locally-without-docker)
- [Usage Examples](#usage-examples)
  - [Accessing the Public Site](#accessing-the-public-site)
  - [Accessing the Admin Dashboard](#accessing-the-admin-dashboard)
- [Configuration Options](#configuration-options)
- [Contributing Guidelines](#contributing-guidelines)
- [License Information](#license-information)
- [Acknowledgments](#acknowledgments)

-----

## Project Title & Description

**Royal Classico FC Management System**

This project provides a comprehensive web platform for the Royal Classico Football Club. It includes a user-friendly frontend to showcase club news and updates, and a secure, "security through obscurity" backend administration system. The admin dashboard allows for the management of players, news articles, and fixtures, with file upload capabilities for various media assets.

- **Owner:** SanMinnHtun

## Key Features & Benefits

*   **Public-Facing Website:** Displays club news, with an auto-rotating news slider and manual navigation.
*   **Hidden Admin Dashboard:** A robust management interface, secured by a PIN entry and a client-side secret token, ensuring administrative control is not publicly discoverable.
*   **Content Management:** Facilitates uploads and management of player profiles, news articles, and fixture details.
*   **Dockerized Deployment:** Easy and consistent deployment across different environments using Docker.
*   **Modern Web Stack:** Built with Java (likely Spring Boot) for the backend and JavaScript/HTML/CSS for the frontend, utilizing modern web standards.
*   **RESTful API:** Implied API for seamless interaction between the frontend and backend for administrative tasks.

## Technologies Used

## Languages

*   **Java**: For the backend logic, server-side processing, and API development.
*   **JavaScript**: For dynamic frontend interactions, including the news slider and admin client SDK.
*   **HTML & CSS**: For structuring and styling the web interfaces.

### Tools & Technologies

*   **Docker**: For containerization, ensuring consistent build and deployment environments.
*   **JVM (Java Virtual Machine)**: To run the Java application.
*   **Maven (inferred)**: For project build automation, given the `target/*.jar` output.

## Project Structure

The project follows a standard structure for Java web applications, with key directories highlighted below:

```
Royal_Classico/
├── src/
│   ├── main/
│   │   ├── java/                   # Java source code
│   │   └── resources/              # Application resources
│   │       ├── static/             # Static frontend assets (CSS, JS, images)
│   │       │   ├── css/
│   │       │   │   └── style.css   # Global stylesheets
│   │       │   ├── js/
│   │       │   │   ├── admin.js    # Admin client-side SDK
│   │       │   │   └── main.js     # Public site client-side logic
│   │       │   └── ...
│   │       └── ...
│   └── ...
├── target/                         # Compiled application artifacts (e.g., .jar)
├── uploads/                        # Directory for uploaded files (players, news, fixtures)
│   ├── players/
│   ├── news/
│   └── fixtures/
├── .gitignore                      # Git ignore file
├── Dockerfile                      # Docker build instructions
├── pom.xml (inferred)              # Maven Project Object Model (for Java dependencies and build)
└── README.md                       # Project overview README
```

## Prerequisites & Dependencies

To build and run this project, you will need the following installed on your system:

*   **Java Development Kit (JDK) 21 or later**: Required for compiling and running the Java backend.
*   **Maven**: For building the Java application (`pom.xml` is assumed).
*   **Docker (optional, but recommended)**: To build and run the application within a container.
*   **A modern web browser**: To access the application (Chrome, Firefox, Edge, Safari).

## Installation & Setup Instructions

Follow these steps to get the Royal Classico FC Management System up and running.

### 1. Clone the Repository

```bash
git clone https://github.com/SanMinnHtun/Royal_Classico.git
cd Royal_Classico
```

### 2. Build the Application

This project is presumed to use Maven for building.

```bash
# Clean previous builds and package the application into a JAR file
mvn clean install
```
This command will compile the Java code, run tests, and package the application into an executable JAR file in the `target/` directory (e.g., `target/royal-classico-0.0.1-SNAPSHOT.jar`).

### 3. Run with Docker (Recommended)

Docker provides an isolated and consistent environment for running the application.

```bash
# Build the Docker image
docker build -t royal-classico .

# Run the Docker container
# The app will be accessible at http://localhost:8080/royal-classico (or http://localhost:8080)
docker run -p 8080:8080 royal-classico
```

The application will start, and the public site will be accessible in your browser.

### 4. Run Locally (without Docker)

If you prefer to run the application directly on your machine without Docker:

```bash
# Ensure the JAR file has been built (see 'Build the Application' section)
# Run the JAR file
java -jar target/*.jar
```
The application will be accessible at `http://localhost:8080/royal-classico` (or potentially `http://localhost:8080` depending on the configured context path).

## Usage Examples

### Accessing the Public Site

Once the application is running, open your web browser and navigate to:

```
http://localhost:8080/royal-classico
```
(Note: The exact context path `/royal-classico` may vary depending on the Spring Boot configuration. It might also be accessible directly at `http://localhost:8080/`.)

Here, you will find the main public interface of the Royal Classico FC website, displaying news and other club information.

### Accessing the Admin Dashboard

The admin dashboard is intentionally obscured for security.

1.  **Hidden Entrance Page:**
    Navigate to the entrance page:
    ```
    http://localhost:8080/royal-classico/entrance
    ```
    You will need to enter a correct PIN to proceed. The PIN for local hidden access is not publicly listed here for security reasons. Upon successful PIN entry, a secret token is stored in your browser's `localStorage`, and you will be redirected to the dashboard.

2.  **Direct Admin Dashboard URL:**
    The admin dashboard URL itself is also obscured:
    ```
    http://localhost:8080/royal-classico/admin-control-system-april202650
    ```
    You will only be able to access this if you have correctly entered the PIN on the `/entrance` page, or if you manually activate the admin secret.

3.  **Developer Activation (for testing/development):**
    For quick local development and testing, you can bypass the PIN entry and activate the admin secret directly via your browser's developer console.

    Open your browser's developer console (F12 or Cmd+Option+I) and paste the following JavaScript code:

    ```javascript
    localStorage.setItem('admin_secret_token', 'RC-ADMIN-XKDF92-CLASSIFIED');
    location.reload();
    ```
    After reloading, you should have access to the hidden admin dashboard at `/royal-classico/admin-control-system-april202650` (or whichever URL it uses).

    To deactivate:
    ```javascript
    localStorage.removeItem('admin_secret_token');
    location.reload();
    ```
## Configuration Options

*   **Application Port**: The application runs on port `8080` by default, as configured in the `Dockerfile` and typical Spring Boot applications. This can be changed in `application.properties` (if present) or via environment variables.
*   **Admin PIN**: The PIN for the `/royal-classico/entrance` page is an internal configuration. Ensure it is strong and not easily guessable.
*   **Admin Secret Token**: The client-side token `RC-ADMIN-XKDF92-CLASSIFIED` is crucial for authenticating against the admin API. This should ideally be secured further in a production environment.
*   **Upload Directories**: The Dockerfile explicitly creates `uploads/players`, `uploads/news`, `uploads/fixtures`. These directories are used to store uploaded media and content. Ensure proper permissions if running outside of Docker.

## Contributing Guidelines

We welcome contributions to the Royal Classico FC Management System! If you're interested in contributing, please follow these guidelines:

1.  **Fork the repository**.
2.  **Clone your forked repository** to your local machine.
3.  **Create a new branch** for your feature or bug fix: `git checkout -b feature/your-feature-name` or `git checkout -b bugfix/issue-description`.
4.  **Make your changes**, ensuring they adhere to the existing code style.
5.  **Test your changes thoroughly**.
6.  **Commit your changes** with clear and descriptive commit messages.
7.  **Push your branch** to your forked repository.
8.  **Open a Pull Request** against the `main` branch of the original repository. Provide a detailed description of your changes.


It is highly recommended to add a license (e.g., MIT, Apache 2.0, GPL) to clarify usage, distribution, and modification terms for others.

## Acknowledgments

*   **Eclipse Temurin**: For providing the base JDK image used in the `Dockerfile`.
*   *   **Google Fonts**: For the `Inter`, `Roboto`, and `Bebas Neue` typefaces used in the frontend styling.

