package com.trent.motelbooking.dto;

import lombok.Data;

@Data
public class BookingRequest {

	private String guestName;

	private String guestEmail;

	private String guestPhone;
	
	private Integer guestCount;

	private Long roomId;

	private String checkInDate;

	private String checkOutDate;
}