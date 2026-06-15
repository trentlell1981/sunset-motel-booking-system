package com.trent.motelbooking.dto;

import lombok.Data;

@Data
public class RoomUpdateRequest {

    private String roomType;

    private Double pricePerNight;

    private Integer maxGuests;

    private String description;
}