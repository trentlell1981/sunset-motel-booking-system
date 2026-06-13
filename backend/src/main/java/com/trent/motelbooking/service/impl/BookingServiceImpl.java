package com.trent.motelbooking.service.impl;

import com.trent.motelbooking.dto.BookingRequest;
import com.trent.motelbooking.entity.Booking;
import com.trent.motelbooking.entity.Room;
import com.trent.motelbooking.repository.BookingRepository;
import com.trent.motelbooking.repository.RoomRepository;
import com.trent.motelbooking.service.BookingService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookingRepository;
	private final RoomRepository roomRepository;

	public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository) {
		this.bookingRepository = bookingRepository;
		this.roomRepository = roomRepository;
	}

	@Override
	public Booking createBooking(BookingRequest bookingRequest) {
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

		Room room = roomRepository.findById(bookingRequest.getRoomId())
				.orElseThrow(() -> new IllegalArgumentException("Room not found"));

		if (!room.getActive()) {
			throw new IllegalArgumentException("Room is not available");
		}

		if (bookingRequest.getGuestCount() > room.getMaxGuests()) {
			throw new IllegalArgumentException("Guest count exceeds room capacity");
		}

		LocalDate checkInDate = LocalDate.parse(bookingRequest.getCheckInDate());
		LocalDate checkOutDate = LocalDate.parse(bookingRequest.getCheckOutDate());

		if (!checkOutDate.isAfter(checkInDate)) {
			throw new IllegalArgumentException("Check-out date must be after check-in date");
		}

		boolean roomAlreadyBooked = bookingRepository.existsByRoomIdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
				bookingRequest.getRoomId(), "CONFIRMED", checkOutDate, checkInDate);

		if (roomAlreadyBooked) {
			throw new IllegalArgumentException("Room is already booked for those dates");
		}

		long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

		if (nights <= 0) {
			throw new IllegalArgumentException("Booking must be at least one night");
		}

		double totalPrice = nights * room.getPricePerNight();

		Booking booking = new Booking();
		booking.setGuestName(bookingRequest.getGuestName());
		booking.setGuestEmail(bookingRequest.getGuestEmail());
		booking.setGuestPhone(bookingRequest.getGuestPhone());
		booking.setGuestCount(bookingRequest.getGuestCount());
		booking.setRoom(room);
		booking.setCheckInDate(checkInDate);
		booking.setCheckOutDate(checkOutDate);
		booking.setTotalPrice(totalPrice);
		booking.setStatus("CONFIRMED");
		booking.setCreatedAt(LocalDateTime.now());

		return bookingRepository.save(booking);
	}

	@Override
	public Booking cancelBooking(Long id) {
		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found"));

		if ("CANCELLED".equals(booking.getStatus())) {
			throw new IllegalArgumentException("Booking is already cancelled");
		}

		booking.setStatus("CANCELLED");

		return bookingRepository.save(booking);
	}

	@Override
	public List<Booking> getAllBookings() {
		return bookingRepository.findAll();
	}
}