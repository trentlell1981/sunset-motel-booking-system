package com.trent.motelbooking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trent.motelbooking.dto.RoomRequest;
import com.trent.motelbooking.repository.BookingRepository;
import com.trent.motelbooking.repository.RoomRepository;
import com.trent.motelbooking.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import java.util.Optional;
import com.trent.motelbooking.dto.RoomUpdateRequest;
import com.trent.motelbooking.entity.Room;

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

		when(roomRepository.existsByRoomNumber("502")).thenReturn(false);

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

		when(roomRepository.existsByRoomNumber("503")).thenReturn(false);

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

		when(roomRepository.existsByRoomNumber("504")).thenReturn(false);

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
		Room existingRoom = new Room();
		existingRoom.setId(1L);
		existingRoom.setRoomNumber("101");
		existingRoom.setRoomType("Standard Room");
		existingRoom.setPricePerNight(89.00);
		existingRoom.setMaxGuests(2);
		existingRoom.setDescription("Original description.");
		existingRoom.setActive(true);

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
		Room existingRoom = new Room();
		existingRoom.setId(1L);
		existingRoom.setRoomNumber("101");
		existingRoom.setRoomType("Standard Room");
		existingRoom.setPricePerNight(89.00);
		existingRoom.setMaxGuests(2);
		existingRoom.setDescription("Original description.");
		existingRoom.setActive(true);

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
		Room existingRoom = new Room();
		existingRoom.setId(1L);
		existingRoom.setRoomNumber("101");
		existingRoom.setRoomType("Standard Room");
		existingRoom.setPricePerNight(89.00);
		existingRoom.setMaxGuests(2);
		existingRoom.setDescription("Original description.");
		existingRoom.setActive(true);

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
		Room existingRoom = new Room();
		existingRoom.setId(1L);
		existingRoom.setRoomNumber("101");
		existingRoom.setRoomType("Standard Room");
		existingRoom.setPricePerNight(89.00);
		existingRoom.setMaxGuests(2);
		existingRoom.setDescription("Original description.");
		existingRoom.setActive(true);

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
		Room existingRoom = new Room();
		existingRoom.setId(1L);
		existingRoom.setRoomNumber("101");
		existingRoom.setRoomType("Standard Room");
		existingRoom.setPricePerNight(89.00);
		existingRoom.setMaxGuests(2);
		existingRoom.setDescription("Original description.");
		existingRoom.setActive(true);

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
		Room existingRoom = new Room();
		existingRoom.setId(1L);
		existingRoom.setRoomNumber("101");
		existingRoom.setRoomType("Standard Room");
		existingRoom.setPricePerNight(89.00);
		existingRoom.setMaxGuests(2);
		existingRoom.setDescription("Original description.");
		existingRoom.setActive(true);

		when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

		roomService.deactivateRoom(1L);

		assertEquals(false, existingRoom.getActive());

		verify(roomRepository).save(existingRoom);
	}
	@Test
	void reactivateRoomShouldSetActiveToTrue() {
	    Room existingRoom = new Room();
	    existingRoom.setId(1L);
	    existingRoom.setRoomNumber("101");
	    existingRoom.setRoomType("Standard Room");
	    existingRoom.setPricePerNight(89.00);
	    existingRoom.setMaxGuests(2);
	    existingRoom.setDescription("Original description.");
	    existingRoom.setActive(false);

	    when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

	    roomService.reactivateRoom(1L);

	    assertEquals(true, existingRoom.getActive());

	    verify(roomRepository).save(existingRoom);
	}
}