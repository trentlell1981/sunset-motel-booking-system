# Sunset Motel Booking System

Sunset Motel Booking System is a full-stack motel booking application built with HTML, CSS, JavaScript, Java, Spring Boot, MySQL, and Lombok.

The application allows customers to search for available rooms by check-in date, check-out date, and guest count. Customers can then select an available room, enter guest information, and submit a booking. The system also includes an admin page where bookings can be viewed and cancelled.

## Features

- Customer-facing motel website
- Room availability search by date range and guest count
- Dynamic room cards loaded from the backend
- One homepage card per room type
- Available room results based on actual database records
- Guest booking form
- Booking confirmation message
- Guest count saved with each booking
- Backend validation for required fields and date ranges
- Double-booking prevention
- Cancel booking endpoint
- Admin page for viewing bookings
- Admin cancellation confirmation
- Cancelled bookings visually marked
- Cancelled bookings no longer block room availability
- Responsive layout for smaller screens

## Tech Stack

### Frontend

- HTML
- CSS
- JavaScript
- Font Awesome
- Live Server

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Lombok
- Maven

### Database

- MySQL
- DBeaver

## Project Structure

```text
motelbooking/
├── src/main/java/com/trent/motelbooking/
│   ├── controller/
│   │   ├── BookingController.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── RoomController.java
│   ├── dto/
│   │   └── BookingRequest.java
│   ├── entity/
│   │   ├── Booking.java
│   │   └── Room.java
│   ├── repository/
│   │   ├── BookingRepository.java
│   │   └── RoomRepository.java
│   ├── service/
│   │   ├── BookingService.java
│   │   ├── RoomService.java
│   │   └── impl/
│   │       ├── BookingServiceImpl.java
│   │       └── RoomServiceImpl.java
│   └── MotelbookingApplication.java
│
├── src/main/resources/
│   └── application.properties
│
└── frontend/
    ├── index.html
    ├── admin.html
    ├── style.css
    ├── script.js
    ├── admin.js
    └── logo.png