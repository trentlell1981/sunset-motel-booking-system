package com.trent.motelbooking.controller;

import com.trent.motelbooking.dto.BookingRequest;
import com.trent.motelbooking.entity.Booking;
import com.trent.motelbooking.service.BookingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = { "http://127.0.0.1:5500", "http://localhost:5500" })
public class BookingController {

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@GetMapping
	public List<Booking> getAllBookings() {
		return bookingService.getAllBookings();
	}

	@PostMapping
	public Booking createBooking(@RequestBody BookingRequest bookingRequest) {
		return bookingService.createBooking(bookingRequest);
	}

	@PutMapping("/{id}/cancel")
	public Booking cancelBooking(@PathVariable Long id) {
		return bookingService.cancelBooking(id);
	}
}