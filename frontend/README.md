# Sunset Motel Booking System

Sunset Motel Booking System is a full-stack motel booking application built with HTML, CSS, JavaScript, Java, Spring Boot, Spring Security, MySQL, JPA, Maven, Lombok, JUnit, and Mockito.

The application allows customers to search for available motel rooms by check-in date, check-out date, and guest count. Customers can select an available room, enter guest information, and submit a booking. The system also includes a protected admin dashboard where bookings and rooms can be managed.

## Features

### Customer Website

* Customer-facing motel website
* Dynamic homepage room cards loaded from the backend
* One homepage card per room type
* Room availability search by date range and guest count
* Available room results based on actual database records
* Guest booking form
* Booking confirmation message
* Guest count saved with each booking
* Double-booking prevention
* Cancelled bookings no longer block room availability
* Inactive rooms do not appear in customer availability results
* Responsive layout for smaller screens
* Link from the customer website to the admin login page

### Booking Management

* Create customer bookings
* Validate required booking fields
* Validate check-in and check-out date ranges
* Validate guest count against room capacity
* Prevent overlapping confirmed bookings
* View all bookings from the admin dashboard
* Cancel confirmed bookings
* Prevent already-cancelled bookings from being cancelled again
* Cancelled bookings are visually marked in the admin dashboard

### Admin Dashboard

* Admin login page
* Protected admin booking endpoints
* Admin logout
* Admin booking filters:

  * Show All
  * Show Confirmed
  * Show Cancelled
* Admin booking search by:

  * Guest name
  * Email
  * Phone
  * Booking ID
  * Room number
  * Room type
  * Date
  * Status
* Status badges for confirmed, cancelled, active, and inactive records

### Room Management

* View all rooms in the admin dashboard
* Add new rooms
* Prevent duplicate room numbers
* Edit room details:

  * Room type
  * Price per night
  * Max guests
  * Description
* Deactivate rooms
* Reactivate rooms
* Active/inactive room status badges
* Inactive rooms are hidden from customer availability searches
* Backend validation for room creation and updates
* Input trimming for cleaner room data

### Security

* Spring Security added to the backend
* Public endpoints:

  * `GET /api/rooms`
  * `GET /api/rooms/available`
  * `POST /api/bookings`
* Protected admin endpoints:

  * `GET /api/bookings`
  * `PUT /api/bookings/{id}/cancel`
  * `POST /api/rooms`
  * `PUT /api/rooms/{id}`
  * `PUT /api/rooms/{id}/deactivate`
  * `PUT /api/rooms/{id}/reactivate`
* Admin credentials are configured through environment-variable fallbacks
* Database connection settings are configured through environment-variable fallbacks

## Tech Stack

### Frontend

* HTML
* CSS
* JavaScript
* Font Awesome
* Live Server

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* Lombok
* Maven

### Database

* MySQL
* DBeaver

### Testing

* JUnit
* Mockito

### Version Control

* Git
* GitHub

## Project Structure

```text
sunset-motel-booking-system/
├── backend/
│   ├── src/main/java/com/trent/motelbooking/
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── BookingController.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── RoomController.java
│   │   ├── dto/
│   │   │   ├── BookingRequest.java
│   │   │   ├── RoomRequest.java
│   │   │   └── RoomUpdateRequest.java
│   │   ├── entity/
│   │   │   ├── Booking.java
│   │   │   └── Room.java
│   │   ├── repository/
│   │   │   ├── BookingRepository.java
│   │   │   └── RoomRepository.java
│   │   ├── service/
│   │   │   ├── BookingService.java
│   │   │   ├── RoomService.java
│   │   │   └── impl/
│   │   │       ├── BookingServiceImpl.java
│   │   │       └── RoomServiceImpl.java
│   │   └── MotelbookingApplication.java
│   │
│   ├── src/main/resources/
│   │   └── application.properties
│   │
│   ├── src/test/java/com/trent/motelbooking/
│   │   ├── BookingServiceImplTest.java
│   │   ├── MotelbookingApplicationTests.java
│   │   └── RoomServiceImplTest.java
│   │
│   └── pom.xml
│
├── frontend/
│   ├── index.html
│   ├── admin.html
│   ├── style.css
│   ├── script.js
│   ├── admin.js
│   └── logo.png
│
├── .gitignore
└── README.md
```

## Backend API Endpoints

### Public Room Endpoints

```text
GET /api/rooms
```

Returns all rooms.

```text
GET /api/rooms/available?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD&guests=1
```

Returns active rooms available for the selected date range and guest count.

### Public Booking Endpoint

```text
POST /api/bookings
```

Creates a new booking.

Example request body:

```json
{
  "guestName": "Test Guest",
  "guestEmail": "guest@test.com",
  "guestPhone": "4065551111",
  "guestCount": 1,
  "roomId": 1,
  "checkInDate": "2026-07-01",
  "checkOutDate": "2026-07-03"
}
```

### Protected Booking Endpoints

```text
GET /api/bookings
```

Returns all bookings. Requires admin login.

```text
PUT /api/bookings/{id}/cancel
```

Cancels a booking. Requires admin login.

### Protected Room Management Endpoints

```text
POST /api/rooms
```

Creates a new room. Requires admin login.

Example request body:

```json
{
  "roomNumber": "401",
  "roomType": "Single Room",
  "pricePerNight": 79.00,
  "maxGuests": 1,
  "description": "Cozy single room with one twin bed."
}
```

```text
PUT /api/rooms/{id}
```

Updates an existing room. Requires admin login.

Example request body:

```json
{
  "roomType": "Updated Single Room",
  "pricePerNight": 85.00,
  "maxGuests": 1,
  "description": "Updated cozy single room with one twin bed."
}
```

```text
PUT /api/rooms/{id}/deactivate
```

Marks a room inactive. Requires admin login.

```text
PUT /api/rooms/{id}/reactivate
```

Marks a room active. Requires admin login.

## Environment Variables

The backend uses environment-variable fallbacks in `application.properties`.

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/motel_db}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}

app.admin.username=${ADMIN_USERNAME:admin}
app.admin.password=${ADMIN_PASSWORD:admin123}
```

Local development uses the fallback values. Production deployments can provide real values through environment variables.

## Local Development Setup

### Backend

1. Open the backend project in Eclipse.
2. Make sure MySQL is running.
3. Make sure the `motel_db` database exists.
4. Start the Spring Boot application.
5. The backend should run on:

```text
http://localhost:8080
```

### Frontend

1. Open the frontend folder in VS Code.
2. Start Live Server.
3. Open the customer website:

```text
index.html
```

4. Open the admin dashboard:

```text
admin.html
```

## Local Admin Login

Default local admin credentials:

```text
Username: admin
Password: admin123
```

These values are configured as local fallbacks and can be replaced with environment variables.

## Automated Tests

The backend includes automated JUnit and Mockito service-layer tests for core business rules.

Test coverage includes:

* Room creation validation
* Duplicate room number prevention
* Room update validation
* Room deactivate/reactivate behavior
* Booking request validation
* Double-booking prevention
* Booking cancellation logic

Current test classes include:

```text
RoomServiceImplTest
BookingServiceImplTest
MotelbookingApplicationTests
```

To run backend tests from the backend folder:

```bash
mvn test
```

The tests can also be run in Eclipse using:

```text
Run As > JUnit Test
```

## Current Limitations

This project is designed as a learning and portfolio project. It includes several production-style improvements, but it is not yet a fully deployed production application.

Current limitations include:

* Admin authentication uses HTTP Basic Auth with an in-memory user
* Admin passwords are not yet stored in a database
* Passwords are not yet BCrypt-hashed
* No payment processing
* No email confirmations
* No deployed cloud database
* No production hosting configuration yet
* No pagination for large booking lists
* No customer account system

## Future Improvements

Possible future improvements include:

* Database-backed admin users
* BCrypt password hashing
* Role-based admin accounts
* Email confirmation for bookings
* Payment processing
* Booking archive feature
* Pagination for admin booking results
* Production deployment
* Cloud MySQL database
* Production CORS configuration
* HTTPS setup
* More controller and security tests
* Frontend form validation improvements
* Better admin dashboard layout
