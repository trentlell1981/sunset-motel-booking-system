package com.trent.motelbooking.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trent.motelbooking.dto.RoomRequest;
import com.trent.motelbooking.dto.RoomUpdateRequest;
import com.trent.motelbooking.entity.Booking;
import com.trent.motelbooking.entity.Room;
import com.trent.motelbooking.repository.BookingRepository;
import com.trent.motelbooking.repository.RoomRepository;
import com.trent.motelbooking.service.RoomService;

@Service
public class RoomServiceImpl implements RoomService {

    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> getAvailableRooms(String checkIn, String checkOut, Integer guests) {
        validateAvailabilityRequest(checkIn, checkOut, guests);

        LocalDate requestedCheckIn = parseDate(checkIn, "Check-in date is invalid");
        LocalDate requestedCheckOut = parseDate(checkOut, "Check-out date is invalid");

        if (!requestedCheckOut.isAfter(requestedCheckIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        List<Booking> overlappingBookings = bookingRepository
                .findByStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                        STATUS_CONFIRMED,
                        requestedCheckOut,
                        requestedCheckIn
                );

        Set<Long> bookedRoomIds = overlappingBookings.stream()
                .map(booking -> booking.getRoom().getId())
                .collect(Collectors.toSet());

        return roomRepository.findAll().stream()
                .filter(Room::getActive)
                .filter(room -> room.getMaxGuests() >= guests)
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .toList();
    }

    @Override
    public Room createRoom(RoomRequest request) {
        validateCreateRoomRequest(request);

        String roomNumber = request.getRoomNumber().trim();

        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new IllegalArgumentException("Room number already exists");
        }

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(request.getRoomType().trim());
        room.setPricePerNight(request.getPricePerNight());
        room.setMaxGuests(request.getMaxGuests());
        room.setDescription(request.getDescription().trim());
        room.setActive(true);

        return roomRepository.save(room);
    }

    @Override
    public Room updateRoom(Long id, RoomUpdateRequest request) {
        Room room = findRoomById(id);

        validateUpdateRoomRequest(request);

        room.setRoomType(request.getRoomType().trim());
        room.setPricePerNight(request.getPricePerNight());
        room.setMaxGuests(request.getMaxGuests());
        room.setDescription(request.getDescription().trim());

        return roomRepository.save(room);
    }

    @Override
    public Room deactivateRoom(Long id) {
        Room room = findRoomById(id);

        room.setActive(false);

        return roomRepository.save(room);
    }

    @Override
    public Room reactivateRoom(Long id) {
        Room room = findRoomById(id);

        room.setActive(true);

        return roomRepository.save(room);
    }

    private void validateAvailabilityRequest(String checkIn, String checkOut, Integer guests) {
        if (checkIn == null || checkIn.isBlank()) {
            throw new IllegalArgumentException("Check-in date is required");
        }

        if (checkOut == null || checkOut.isBlank()) {
            throw new IllegalArgumentException("Check-out date is required");
        }

        if (guests == null || guests < 1) {
            throw new IllegalArgumentException("Guest count is required");
        }
    }

    private void validateCreateRoomRequest(RoomRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Room request is required");
        }

        if (request.getRoomNumber() == null || request.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }

        if (request.getRoomType() == null || request.getRoomType().isBlank()) {
            throw new IllegalArgumentException("Room type is required");
        }

        validateRoomDetails(
                request.getPricePerNight(),
                request.getMaxGuests(),
                request.getDescription()
        );
    }

    private void validateUpdateRoomRequest(RoomUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Room update request is required");
        }

        if (request.getRoomType() == null || request.getRoomType().isBlank()) {
            throw new IllegalArgumentException("Room type is required");
        }

        validateRoomDetails(
                request.getPricePerNight(),
                request.getMaxGuests(),
                request.getDescription()
        );
    }

    private void validateRoomDetails(Double pricePerNight, Integer maxGuests, String description) {
        if (pricePerNight == null || pricePerNight <= 0) {
            throw new IllegalArgumentException("Price per night must be greater than 0");
        }

        if (maxGuests == null || maxGuests < 1) {
            throw new IllegalArgumentException("Max guests must be at least 1");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
    }

    private Room findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    private LocalDate parseDate(String date, String errorMessage) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}