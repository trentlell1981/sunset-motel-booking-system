package com.trent.motelbooking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.trent.motelbooking.dto.RoomRequest;
import com.trent.motelbooking.dto.RoomUpdateRequest;
import com.trent.motelbooking.entity.Room;
import com.trent.motelbooking.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/available")
    public List<Room> getAvailableRooms(
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam int guests) {

        return roomService.getAvailableRooms(checkIn, checkOut, guests);
    }

    @PostMapping
    public Room createRoom(@RequestBody RoomRequest request) {
        return roomService.createRoom(request);
    }

    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable Long id, @RequestBody RoomUpdateRequest request) {
        return roomService.updateRoom(id, request);
    }

    @PutMapping("/{id}/deactivate")
    public Room deactivateRoom(@PathVariable Long id) {
        return roomService.deactivateRoom(id);
    }

    @PutMapping("/{id}/reactivate")
    public Room reactivateRoom(@PathVariable Long id) {
        return roomService.reactivateRoom(id);
    }
}