package com.trent.motelbooking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trent.motelbooking.dto.RoomRequest;
import com.trent.motelbooking.dto.RoomUpdateRequest;
import com.trent.motelbooking.entity.Room;
import com.trent.motelbooking.repository.BookingRepository;
import com.trent.motelbooking.repository.RoomRepository;
import com.trent.motelbooking.service.impl.RoomServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    void createRoomShouldRejectDuplicateRoomNumber() {
        RoomRequest request = new RoomRequest();
        request.setRoomNumber("401");
        request.setRoomType("Single Room");
        request.setPricePerNight(79.00);
        request.setMaxGuests(1);
        request.setDescription("Cozy single room with one twin bed.");

        when(roomRepository.existsByRoomNumber("401")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.createRoom(request));

        assertEquals("Room number already exists", exception.getMessage());
    }

    @Test
    void createRoomShouldSaveValidRoom() {
        RoomRequest request = new RoomRequest();
        request.setRoomNumber("501");
        request.setRoomType("Test Room");
        request.setPricePerNight(99.00);
        request.setMaxGuests(2);
        request.setDescription("Test room description.");

        when(roomRepository.existsByRoomNumber("501")).thenReturn(false);

        roomService.createRoom(request);

        verify(roomRepository).save(any());
    }

    @Test
    void createRoomShouldRejectInvalidPrice() {
        RoomRequest request = new RoomRequest();
        request.setRoomNumber("502");
        request.setRoomType("Test Room");
        request.setPricePerNight(0.00);
        request.setMaxGuests(2);
        request.setDescription("Test room description.");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.createRoom(request));

        assertEquals("Price per night must be greater than 0", exception.getMessage());
    }

    @Test
    void createRoomShouldRejectInvalidMaxGuests() {
        RoomRequest request = new RoomRequest();
        request.setRoomNumber("503");
        request.setRoomType("Test Room");
        request.setPricePerNight(99.00);
        request.setMaxGuests(0);
        request.setDescription("Test room description.");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.createRoom(request));

        assertEquals("Max guests must be at least 1", exception.getMessage());
    }

    @Test
    void createRoomShouldRejectBlankDescription() {
        RoomRequest request = new RoomRequest();
        request.setRoomNumber("504");
        request.setRoomType("Test Room");
        request.setPricePerNight(99.00);
        request.setMaxGuests(2);
        request.setDescription("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.createRoom(request));

        assertEquals("Description is required", exception.getMessage());
    }

    @Test
    void createRoomShouldTrimTextFieldsBeforeSaving() {
        RoomRequest request = new RoomRequest();
        request.setRoomNumber(" 505 ");
        request.setRoomType(" Test Room ");
        request.setPricePerNight(99.00);
        request.setMaxGuests(2);
        request.setDescription(" Test room description. ");

        when(roomRepository.existsByRoomNumber("505")).thenReturn(false);

        roomService.createRoom(request);

        verify(roomRepository).save(any());
    }

    @Test
    void updateRoomShouldUpdateValidRoom() {
        Room existingRoom = createTestRoom(true);

        RoomUpdateRequest request = new RoomUpdateRequest();
        request.setRoomType("Updated Room");
        request.setPricePerNight(99.00);
        request.setMaxGuests(3);
        request.setDescription("Updated description.");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        roomService.updateRoom(1L, request);

        assertEquals("Updated Room", existingRoom.getRoomType());
        assertEquals(99.00, existingRoom.getPricePerNight());
        assertEquals(3, existingRoom.getMaxGuests());
        assertEquals("Updated description.", existingRoom.getDescription());

        verify(roomRepository, times(1)).save(existingRoom);
    }

    @Test
    void updateRoomShouldRejectRoomNotFound() {
        RoomUpdateRequest request = new RoomUpdateRequest();
        request.setRoomType("Updated Room");
        request.setPricePerNight(99.00);
        request.setMaxGuests(2);
        request.setDescription("Updated description.");

        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.updateRoom(999L, request));

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    void updateRoomShouldRejectInvalidPrice() {
        Room existingRoom = createTestRoom(true);

        RoomUpdateRequest request = new RoomUpdateRequest();
        request.setRoomType("Updated Room");
        request.setPricePerNight(0.00);
        request.setMaxGuests(2);
        request.setDescription("Updated description.");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.updateRoom(1L, request));

        assertEquals("Price per night must be greater than 0", exception.getMessage());
    }

    @Test
    void updateRoomShouldRejectInvalidMaxGuests() {
        Room existingRoom = createTestRoom(true);

        RoomUpdateRequest request = new RoomUpdateRequest();
        request.setRoomType("Updated Room");
        request.setPricePerNight(99.00);
        request.setMaxGuests(0);
        request.setDescription("Updated description.");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.updateRoom(1L, request));

        assertEquals("Max guests must be at least 1", exception.getMessage());
    }

    @Test
    void updateRoomShouldRejectBlankDescription() {
        Room existingRoom = createTestRoom(true);

        RoomUpdateRequest request = new RoomUpdateRequest();
        request.setRoomType("Updated Room");
        request.setPricePerNight(99.00);
        request.setMaxGuests(2);
        request.setDescription("");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomService.updateRoom(1L, request));

        assertEquals("Description is required", exception.getMessage());
    }

    @Test
    void updateRoomShouldTrimTextFieldsBeforeSaving() {
        Room existingRoom = createTestRoom(true);

        RoomUpdateRequest request = new RoomUpdateRequest();
        request.setRoomType(" Updated Room ");
        request.setPricePerNight(99.00);
        request.setMaxGuests(3);
        request.setDescription(" Updated description. ");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        roomService.updateRoom(1L, request);

        assertEquals("Updated Room", existingRoom.getRoomType());
        assertEquals("Updated description.", existingRoom.getDescription());

        verify(roomRepository).save(existingRoom);
    }

    @Test
    void deactivateRoomShouldSetActiveToFalse() {
        Room existingRoom = createTestRoom(true);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        roomService.deactivateRoom(1L);

        assertEquals(false, existingRoom.getActive());

        verify(roomRepository).save(existingRoom);
    }

    @Test
    void reactivateRoomShouldSetActiveToTrue() {
        Room existingRoom = createTestRoom(false);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        roomService.reactivateRoom(1L);

        assertEquals(true, existingRoom.getActive());

        verify(roomRepository).save(existingRoom);
    }

    private Room createTestRoom(Boolean active) {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setRoomType("Standard Room");
        room.setPricePerNight(89.00);
        room.setMaxGuests(2);
        room.setDescription("Original description.");
        room.setActive(active);

        return room;
    }
}