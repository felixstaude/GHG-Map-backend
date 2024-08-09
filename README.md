GHG Map Backend
This project is a backend service for managing geographical pins on a map. The service allows you to add pins with
associated images, retrieve pin data, and gather statistical information about the pins added over time. The backend is
built using Spring Boot and is designed to handle various HTTP requests for these operations.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Endpoints](#endpoints)
  - [Pin Endpoints](#pin-endpoints)
  - [Statistics Endpoints](#statistics-endpoints)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)

## Getting Started
### Prerequisites
To run this project, ensure you have the following installed on your system:
- Java 17 or higher
- Maven
### Installation
1. Clone the repository to your local machine.
2. Navigate to the project directory.
```bash
git clone https://github.com/felixstaude/ghg-map-backend.git
cd ghg-map-backend
```
3. Build the project using Maven.
```bash
mvn clean install
```
## Project Structure
The project is organized as follows:
- **`de.felixstaude.ghgmap.api.connection.pin`**: Contains classes for managing pins, including adding pins and
  retrieving pin data.
- **`de.felixstaude.ghgmap.api.statistics`**: Contains classes for calculating and retrieving statistics about the pins
  added, including daily, monthly, and yearly statistics.
- **`de.felixstaude.ghgmap.config`**: Contains configuration classes, such as CORS settings.
- **`de.felixstaude.ghgmap.file`**: Manages file operations, including saving images to the server.
- **`de.felixstaude.ghgmap.database`**: Contains classes for handling the data structures related to pins.
- **`de.felixstaude.ghgmap`**: Contains the main application class and configuration classes.
## API Endpoints
### Pin API
- **`POST /api/pin/add`**
  Adds a new pin with associated metadata and an image.
  **Request Parameters:**
- `json`: The pin metadata in JSON format.
- `image`: The image file associated with the pin.
  **Response:**
- A JSON object containing the status, pin ID, latitude, longitude, description, and image URL.
- **`GET /api/pin/get/all`**
  Retrieves all pins as a JSON object where each key is the pin ID and the value contains latitude and longitude.
  **Response:**
- A JSON object with the format:
```json
{
"1": {"lat": 51.163361, "lng": 10.447683},
"2": {"lat": 53.163361, "lng": 9.447683}
}
```
- **`GET /api/pin/get/data`**
  Retrieves detailed data for a specific pin.
  **Request Parameters:**
- `pinId`: The ID of the pin.
  **Response:**
- A JSON object containing the pin's ID, user ID, description, latitude, longitude, and image path.
### Statistics API
- **`GET /api/statistics/pins/today`**
  Retrieves the number of pins added today.
- **`GET /api/statistics/pins/day`**
  Retrieves the number of pins added on a specific day.
  **Request Parameters:**
- `day`: The day (e.g., `09`).
- `month`: The month (e.g., `08`).
- `year`: The year (e.g., `2024`).
- **`GET /api/statistics/pins/month`**
  Retrieves the number of pins added in a specific month.
  **Request Parameters:**
- `month`: The month (e.g., `08`).
- `year`: The year (e.g., `2024`).
- **`GET /api/statistics/pins/year`**
  Retrieves the number of pins added in a specific year.
  **Request Parameters:**
- `year`: The year (e.g., `2024`).
## Configuration
The application is configured to store images in the `data/images/` directory. The `spring.web.resources.static-locations`
property in `application.properties` allows static access to these images.
```properties
spring.web.resources.static-locations=file:./data/images/
```
## Building and Running
To build and run the project:
1. Build the project with Maven.
```bash
mvn clean install
```
2. Run the application.
```bash
mvn spring-boot:run
```
The application will start on `http://localhost:8080`.
## Dependencies
The project uses the following dependencies:
- **Spring Boot Starter Web**: Provides the necessary libraries to build a web application.
- **Spring Boot Starter Test**: For testing the application.
- **JUnit Jupiter Engine**: For unit testing.
- **Mockito Core**: For mocking dependencies in tests.
## License
This project is licensed under the MIT License. See the LICENSE file for details.
