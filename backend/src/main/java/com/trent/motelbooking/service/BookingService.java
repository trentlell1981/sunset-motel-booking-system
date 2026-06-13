package com.trent.motelbooking.service;

import com.trent.motelbooking.dto.BookingRequest;
import com.trent.motelbooking.entity.Booking;

import java.util.List;

public interface BookingService {

    Booking createBooking(BookingRequest bookingRequest);

    List<Booking> getAllBookings();

    Booking cancelBooking(Long id);
}