package com.trent.motelbooking.service;

import java.util.List;

import com.trent.motelbooking.dto.RoomRequest;
import com.trent.motelbooking.dto.RoomUpdateRequest;
import com.trent.motelbooking.entity.Room;

public interface RoomService {

	List<Room> getAllRooms();

	List<Room> getAvailableRooms(String checkIn, String checkOut, Integer guests);

	Room createRoom(RoomRequest request);

	Room updateRoom(Long id, RoomUpdateRequest request);

	Room deactivateRoom(Long id);

	Room reactivateRoom(Long id);
}