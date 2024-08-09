
# GHG Map Backend

## Overview
The GHG Map Backend is a Spring Boot application that provides APIs for managing and retrieving geolocation pins with associated data, including statistics about the pins created per day, month, and year. It allows users to upload images, associate them with geographic coordinates, and retrieve statistics about the pins. Additionally, it now includes functionality to retrieve pins associated with specific users and save town information based on the pin's location.

## Table of Contents
- [Setup](#setup)
- [Endpoints](#endpoints)
    - [Pin Management](#pin-management)
        - [Add Pin](#add-pin)
        - [Get All Pins](#get-all-pins)
        - [Get Pin Data](#get-pin-data)
        - [Get Pins by User ID](#get-pins-by-user-id)
    - [Statistics](#statistics)
        - [Get Today's Pin Count](#get-todays-pin-count)
        - [Get Pin Count by Day](#get-pin-count-by-day)
        - [Get Pin Count by Month](#get-pin-count-by-month)
        - [Get Pin Count by Year](#get-pin-count-by-year)
- [File Structure](#file-structure)
- [Dependencies](#dependencies)

## Setup
To run the GHG Map Backend, you need to have Java 17 and Maven installed.

### Steps:
1. Clone the repository.
2. Navigate to the project directory.
3. Run `mvn clean install` to build the project.
4. Start the application with `mvn spring-boot:run`.

## Endpoints

### Pin Management

#### Add Pin
- **Endpoint**: `/api/pin/add`
- **Method**: `POST`
- **Description**: Adds a new pin with an image. The town of the pin's location is also determined and stored.
- **Request**:
    - `json`: JSON string containing `lat`, `lng`, `description`, `userId`.
    - `image`: Multipart file containing the image to be associated with the pin.
- **Response**:
    - `ok`: `true` if the pin was successfully added.
    - `pinId`: ID of the created pin.
    - `lat`: Latitude of the pin.
    - `lng`: Longitude of the pin.
    - `description`: Description of the pin.
    - `imageUrl`: URL to access the uploaded image.

#### Get All Pins
- **Endpoint**: `/api/pin/get/all`
- **Method**: `GET`
- **Description**: Retrieves all pins with their latitude and longitude.
- **Response**: JSON object with pin IDs as keys and objects containing `lat` and `lng`.

Example Response:
```json
{
  "1": {"lng": 6.910400390625001, "lat": 51.781435604431195},
  "2": {"lng": 12.095947265625, "lat": 53.258641373488096},
  "3": {"lng": 9.865722656250002, "lat": 50.63901028125873},
  "4": {"lng": 10.272216796875002, "lat": 52.6097193915665}
}
```

#### Get Pin Data
- **Endpoint**: `/api/pin/get/data`
- **Method**: `GET`
- **Description**: Retrieves detailed information about a specific pin.
- **Request Parameters**:
    - `pinId`: ID of the pin.
- **Response**: JSON object containing `pinId`, `userId`, `description`, `lat`, `lng`, `town`, and `imagePath`.

#### Get Pins by User ID
- **Endpoint**: `/api/pin/get/user`
- **Method**: `GET`
- **Description**: Retrieves all pins created by a specific user.
- **Request Parameters**:
    - `userId`: The ID of the user whose pins are to be retrieved.
- **Response**:
    - `ok`: `true` if the user has created pins, `false` otherwise.
    - `pins`: A list of pins created by the user, each containing `pinId`, `lat`, `lng`, `description`, and `imagePath`.

### Statistics

#### Get Today's Pin Count
- **Endpoint**: `/api/statistics/pins/today`
- **Method**: `GET`
- **Description**: Retrieves the number of pins created today.
- **Response**: Integer representing the count.

#### Get Pin Count by Day
- **Endpoint**: `/api/statistics/pins/day`
- **Method**: `GET`
- **Description**: Retrieves the number of pins created on a specific day.
- **Request Parameters**:
    - `day`: Day (DD).
    - `month`: Month (MM).
    - `year`: Year (YYYY).
- **Response**: Integer representing the count.

#### Get Pin Count by Month
- **Endpoint**: `/api/statistics/pins/month`
- **Method**: `GET`
- **Description**: Retrieves the number of pins created in a specific month.
- **Request Parameters**:
    - `month`: Month (MM).
    - `year`: Year (YYYY).
- **Response**: Integer representing the count.

#### Get Pin Count by Year
- **Endpoint**: `/api/statistics/pins/year`
- **Method**: `GET`
- **Description**: Retrieves the number of pins created in a specific year.
- **Request Parameters**:
    - `year`: Year (YYYY).
- **Response**: Integer representing the count.

## File Structure

```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── de
│   │   │       └── felixstaude
│   │   │           └── ghgmap
│   │   │               ├── api
│   │   │               │   ├── connection
│   │   │               │   │   └── pin
│   │   │               │   │       ├── PinController.java
│   │   │               │   │       └── PinRequest.java
│   │   │               │   └── statistics
│   │   │               │       ├── PinsPerDay.java
│   │   │               │       ├── PinsPerMonth.java
│   │   │               │       ├── PinsPerYear.java
│   │   │               │       ├── User.java
│   │   │               │       └── StatisticsController.java
│   │   │               ├── config
│   │   │               │   └── CorsConfig.java
│   │   │               ├── database
│   │   │               │   └── PinData.java
│   │   │               ├── file
│   │   │               │   └── ImageProcessor.java
│   │   │               ├── nominatim
│   │   │               │   └── NominatimClient.java
│   │   │               └── Main.java
│   │   └── resources
│   │       └── application.properties
├── data
│   ├── images
│   └── pins.csv
└── pom.xml
```

## Dependencies
- Spring Boot 3.3.0
- JUnit 5.10.2
- Mockito 5.7.0

Ensure that the `spring.web.resources.static-locations` in your `application.properties` file is set correctly to serve images from the `data/images/` directory.