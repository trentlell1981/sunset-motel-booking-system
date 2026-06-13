package com.trent.motelbooking.repository;

import com.trent.motelbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatusAndCheckInDateBeforeAndCheckOutDateAfter(
            String status,
            LocalDate checkOut,
            LocalDate checkIn
    );

    boolean existsByRoomIdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
            Long roomId,
            String status,
            LocalDate checkOut,
            LocalDate checkIn
    );
}