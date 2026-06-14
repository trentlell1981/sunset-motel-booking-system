package com.trent.motelbooking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trent.motelbooking.dto.BookingRequest;
import com.trent.motelbooking.entity.Booking;
import com.trent.motelbooking.entity.Room;
import com.trent.motelbooking.repository.BookingRepository;
import com.trent.motelbooking.repository.RoomRepository;
import com.trent.motelbooking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBookingShouldRejectMissingGuestName() {
        BookingRequest request = createValidBookingRequest();
        request.setGuestName("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Guest name is required", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectMissingGuestEmail() {
        BookingRequest request = createValidBookingRequest();
        request.setGuestEmail("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Guest email is required", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectMissingRoomId() {
        BookingRequest request = createValidBookingRequest();
        request.setRoomId(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Room is required", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectInvalidGuestCount() {
        BookingRequest request = createValidBookingRequest();
        request.setGuestCount(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Guest count is required", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectMissingCheckInDate() {
        BookingRequest request = createValidBookingRequest();
        request.setCheckInDate("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Check-in date is required", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectMissingCheckOutDate() {
        BookingRequest request = createValidBookingRequest();
        request.setCheckOutDate("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Check-out date is required", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectRoomNotFound() {
        BookingRequest request = createValidBookingRequest();

        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectInactiveRoom() {
        BookingRequest request = createValidBookingRequest();

        Room room = createActiveRoom();
        room.setActive(false);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Room is not available", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectGuestCountOverRoomCapacity() {
        BookingRequest request = createValidBookingRequest();
        request.setGuestCount(5);

        Room room = createActiveRoom();
        room.setMaxGuests(2);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Guest count exceeds room capacity", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectInvalidDateRange() {
        BookingRequest request = createValidBookingRequest();
        request.setCheckInDate("2026-07-01");
        request.setCheckOutDate("2026-07-01");

        Room room = createActiveRoom();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Check-out date must be after check-in date", exception.getMessage());
    }

    @Test
    void createBookingShouldRejectOverlappingConfirmedBooking() {
        BookingRequest request = createValidBookingRequest();

        Room room = createActiveRoom();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                1L,
                "CONFIRMED",
                LocalDate.parse("2026-07-03"),
                LocalDate.parse("2026-07-01")
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals("Room is already booked for those dates", exception.getMessage());
    }

    @Test
    void createBookingShouldSaveValidBooking() {
        BookingRequest request = createValidBookingRequest();

        Room room = createActiveRoom();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                1L,
                "CONFIRMED",
                LocalDate.parse("2026-07-03"),
                LocalDate.parse("2026-07-01")
        )).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking savedBooking = bookingService.createBooking(request);

        assertEquals("Test Guest", savedBooking.getGuestName());
        assertEquals("guest@test.com", savedBooking.getGuestEmail());
        assertEquals("4065551111", savedBooking.getGuestPhone());
        assertEquals(1, savedBooking.getGuestCount());
        assertEquals(room, savedBooking.getRoom());
        assertEquals(LocalDate.parse("2026-07-01"), savedBooking.getCheckInDate());
        assertEquals(LocalDate.parse("2026-07-03"), savedBooking.getCheckOutDate());
        assertEquals(178.00, savedBooking.getTotalPrice());
        assertEquals("CONFIRMED", savedBooking.getStatus());

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void cancelBookingShouldRejectBookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.cancelBooking(999L)
        );

        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void cancelBookingShouldRejectAlreadyCancelledBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus("CANCELLED");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.cancelBooking(1L)
        );

        assertEquals("Booking is already cancelled", exception.getMessage());
    }

    @Test
    void cancelBookingShouldSetStatusToCancelled() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus("CONFIRMED");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking cancelledBooking = bookingService.cancelBooking(1L);

        assertEquals("CANCELLED", cancelledBooking.getStatus());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        assertEquals("CANCELLED", bookingCaptor.getValue().getStatus());
    }

    private BookingRequest createValidBookingRequest() {
        BookingRequest request = new BookingRequest();
        request.setGuestName("Test Guest");
        request.setGuestEmail("guest@test.com");
        request.setGuestPhone("4065551111");
        request.setGuestCount(1);
        request.setRoomId(1L);
        request.setCheckInDate("2026-07-01");
        request.setCheckOutDate("2026-07-03");

        return request;
    }

    private Room createActiveRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setRoomType("Standard Queen Room");
        room.setPricePerNight(89.00);
        room.setMaxGuests(2);
        room.setDescription("Standard queen room.");
        room.setActive(true);

        return room;
    }
}