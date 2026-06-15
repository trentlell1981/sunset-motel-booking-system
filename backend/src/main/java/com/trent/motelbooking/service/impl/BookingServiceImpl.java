package com.trent.motelbooking.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.trent.motelbooking.dto.BookingRequest;
import com.trent.motelbooking.entity.Booking;
import com.trent.motelbooking.entity.Room;
import com.trent.motelbooking.repository.BookingRepository;
import com.trent.motelbooking.repository.RoomRepository;
import com.trent.motelbooking.service.BookingService;

@Service
public class BookingServiceImpl implements BookingService {

    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public Booking createBooking(BookingRequest bookingRequest) {
        validateBookingRequest(bookingRequest);

        Room room = findRoomById(bookingRequest.getRoomId());

        validateRoomIsActive(room);
        validateGuestCountFitsRoom(bookingRequest.getGuestCount(), room);

        LocalDate checkInDate = parseDate(bookingRequest.getCheckInDate(), "Check-in date is invalid");
        LocalDate checkOutDate = parseDate(bookingRequest.getCheckOutDate(), "Check-out date is invalid");

        validateDateRange(checkInDate, checkOutDate);
        validateRoomAvailability(bookingRequest.getRoomId(), checkInDate, checkOutDate);

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double totalPrice = nights * room.getPricePerNight();

        Booking booking = new Booking();
        booking.setGuestName(bookingRequest.getGuestName().trim());
        booking.setGuestEmail(bookingRequest.getGuestEmail().trim());
        booking.setGuestPhone(trimOrNull(bookingRequest.getGuestPhone()));
        booking.setGuestCount(bookingRequest.getGuestCount());
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(STATUS_CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    @Override
    public Booking cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (STATUS_CANCELLED.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        booking.setStatus(STATUS_CANCELLED);

        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    private void validateBookingRequest(BookingRequest bookingRequest) {
        if (bookingRequest == null) {
            throw new IllegalArgumentException("Booking request is required");
        }

        if (bookingRequest.getGuestName() == null || bookingRequest.getGuestName().isBlank()) {
            throw new IllegalArgumentException("Guest name is required");
        }

        if (bookingRequest.getGuestEmail() == null || bookingRequest.getGuestEmail().isBlank()) {
            throw new IllegalArgumentException("Guest email is required");
        }

        if (bookingRequest.getRoomId() == null) {
            throw new IllegalArgumentException("Room is required");
        }

        if (bookingRequest.getGuestCount() == null || bookingRequest.getGuestCount() < 1) {
            throw new IllegalArgumentException("Guest count is required");
        }

        if (bookingRequest.getCheckInDate() == null || bookingRequest.getCheckInDate().isBlank()) {
            throw new IllegalArgumentException("Check-in date is required");
        }

        if (bookingRequest.getCheckOutDate() == null || bookingRequest.getCheckOutDate().isBlank()) {
            throw new IllegalArgumentException("Check-out date is required");
        }
    }

    private Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    private void validateRoomIsActive(Room room) {
        if (!room.getActive()) {
            throw new IllegalArgumentException("Room is not available");
        }
    }

    private void validateGuestCountFitsRoom(Integer guestCount, Room room) {
        if (guestCount > room.getMaxGuests()) {
            throw new IllegalArgumentException("Guest count exceeds room capacity");
        }
    }

    private LocalDate parseDate(String date, String errorMessage) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateDateRange(LocalDate checkInDate, LocalDate checkOutDate) {
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        if (nights < 1) {
            throw new IllegalArgumentException("Booking must be at least one night");
        }
    }

    private void validateRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        boolean roomAlreadyBooked = bookingRepository.existsByRoomIdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                roomId,
                STATUS_CONFIRMED,
                checkOutDate,
                checkInDate
        );

        if (roomAlreadyBooked) {
            throw new IllegalArgumentException("Room is already booked for those dates");
        }
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }
}