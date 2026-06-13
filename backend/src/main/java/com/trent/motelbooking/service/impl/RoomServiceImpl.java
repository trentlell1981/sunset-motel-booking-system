package com.trent.motelbooking.service.impl;

import com.trent.motelbooking.dto.RoomRequest;
import com.trent.motelbooking.dto.RoomUpdateRequest;
import com.trent.motelbooking.entity.Booking;
import com.trent.motelbooking.entity.Room;
import com.trent.motelbooking.repository.BookingRepository;
import com.trent.motelbooking.repository.RoomRepository;
import com.trent.motelbooking.service.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

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
        LocalDate requestedCheckIn = LocalDate.parse(checkIn);
        LocalDate requestedCheckOut = LocalDate.parse(checkOut);

        List<Booking> overlappingBookings = bookingRepository
                .findByStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                        "CONFIRMED",
                        requestedCheckOut,
                        requestedCheckIn
                );

        Set<Long> bookedRoomIds = overlappingBookings.stream()
                .map(booking -> booking.getRoom().getId())
                .collect(Collectors.toSet());

        return roomRepository.findAll().stream()
                .filter(room -> room.getActive())
                .filter(room -> room.getMaxGuests() >= guests)
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .toList();
    }

    @Override
    public Room createRoom(RoomRequest request) {
        if (request.getRoomNumber() == null || request.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }

        if (request.getRoomType() == null || request.getRoomType().isBlank()) {
            throw new IllegalArgumentException("Room type is required");
        }

        if (roomRepository.existsByRoomNumber(request.getRoomNumber().trim())) {
            throw new IllegalArgumentException("Room number already exists");
        }

        if (request.getPricePerNight() == null || request.getPricePerNight() <= 0) {
            throw new IllegalArgumentException("Price per night must be greater than 0");
        }

        if (request.getMaxGuests() == null || request.getMaxGuests() < 1) {
            throw new IllegalArgumentException("Max guests must be at least 1");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber().trim());
        room.setRoomType(request.getRoomType().trim());
        room.setPricePerNight(request.getPricePerNight());
        room.setMaxGuests(request.getMaxGuests());
        room.setDescription(request.getDescription().trim());
        room.setActive(true);

        return roomRepository.save(room);
    }

    @Override
    public Room updateRoom(Long id, RoomUpdateRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (request.getRoomType() == null || request.getRoomType().isBlank()) {
            throw new IllegalArgumentException("Room type is required");
        }

        if (request.getPricePerNight() == null || request.getPricePerNight() <= 0) {
            throw new IllegalArgumentException("Price per night must be greater than 0");
        }

        if (request.getMaxGuests() == null || request.getMaxGuests() < 1) {
            throw new IllegalArgumentException("Max guests must be at least 1");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        room.setRoomType(request.getRoomType().trim());
        room.setPricePerNight(request.getPricePerNight());
        room.setMaxGuests(request.getMaxGuests());
        room.setDescription(request.getDescription().trim());

        return roomRepository.save(room);
    }

    @Override
    public Room deactivateRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        room.setActive(false);

        return roomRepository.save(room);
    }

    @Override
    public Room reactivateRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        room.setActive(true);

        return roomRepository.save(room);
    }
}