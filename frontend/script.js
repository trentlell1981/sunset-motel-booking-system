function showRoom(name, price, details) {
  alert(
    name + "\n" +
    price + "\n\n" +
    details
  );
}

function selectRoom(roomId, roomType, roomNumber) {
  document.getElementById("selectedRoomId").value = roomId;

  document.getElementById("selectedRoomMessage").textContent =
    `You selected Room ${roomNumber}: ${roomType}`;

  document.getElementById("bookingConfirmationMessage").textContent = "";

  document.getElementById("guestBookingForm").style.display = "block";

  document.querySelector("#guestBookingForm button").disabled = false;
  document.querySelector("#guestBookingForm button").textContent = "Confirm Booking";

  document.getElementById("guestBookingForm").scrollIntoView({
    behavior: "smooth"
  });
}

function submitBooking(e) {
  e.preventDefault();

  const bookingRequest = {
    guestName: document.getElementById("guestName").value,
    guestEmail: document.getElementById("guestEmail").value,
    guestPhone: document.getElementById("guestPhone").value,
    guestCount: Number(document.getElementById("guests").value),
    roomId: Number(document.getElementById("selectedRoomId").value),
    checkInDate: document.getElementById("checkIn").value,
    checkOutDate: document.getElementById("checkOut").value
  };

  fetch("http://localhost:8080/api/bookings", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(bookingRequest)
  })
    .then(response => {
      if (!response.ok) {
        return response.text().then(message => {
          throw new Error(message);
        });
      }

      return response.json();
    })
    .then(booking => {
      document.getElementById("bookingConfirmationMessage").textContent =
        `Booking confirmed for ${booking.guestName}. Confirmation number: ${booking.id}.`;

      document.getElementById("guestName").value = "";
      document.getElementById("guestEmail").value = "";
      document.getElementById("guestPhone").value = "";

      document.querySelector("#guestBookingForm button").disabled = true;
      document.querySelector("#guestBookingForm button").textContent = "Booking Confirmed";
    })
    .catch(error => {
      alert(error.message);
    });
}

function checkAvailability(e) {
  e.preventDefault();

  const checkIn = document.getElementById("checkIn").value;
  const checkOut = document.getElementById("checkOut").value;
  const guests = document.getElementById("guests").value;

  if (!checkIn || !checkOut) {
    alert("Please select check-in and check-out dates.");
    return;
  }

  if (checkOut <= checkIn) {
    alert("Check-out date must be after check-in date.");
    return;
  }

  fetch(`http://localhost:8080/api/rooms/available?checkIn=${checkIn}&checkOut=${checkOut}&guests=${guests}`)
    .then(response => response.json())
    .then(rooms => {
      console.log("Available rooms:", rooms);

      const message = document.getElementById("availabilityMessage");
      const roomGrid = document.getElementById("availableRoomGrid");

      roomGrid.innerHTML = "";

      if (rooms.length === 0) {
        message.textContent = "No rooms are available for those dates.";
        return;
      }

      message.textContent = `Found ${rooms.length} available room(s):`;

      rooms.forEach(room => {
        const roomCard = document.createElement("div");
        roomCard.classList.add("available-room-card");

        roomCard.innerHTML = `
          <h3>${room.roomType}</h3>
          <p><strong>Room:</strong> ${room.roomNumber}</p>
          <p><strong>Max Guests:</strong> ${room.maxGuests}</p>
          <p><strong>Description:</strong> ${room.description}</p>
          <p class="price">$${room.pricePerNight} / night</p>
          <button onclick="selectRoom(${room.id}, '${room.roomType}', '${room.roomNumber}')">
            Book This Room
          </button>
        `;

        roomGrid.appendChild(roomCard);
      });

      document.getElementById("availabilityResults").scrollIntoView({
        behavior: "smooth"
      });
    })
    .catch(error => {
      console.error("Error checking availability:", error);
      alert("Something went wrong while checking availability.");
    });
}

const scrollItems = document.querySelectorAll(
  ".features div, .room-card, .amenity-grid div, .location, .review-grid div, .cta"
);

const scrollObserver = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add("show");
    }
  });
}, {
  threshold: 0.2
});

scrollItems.forEach(item => {
  item.classList.add("hidden-scroll");
  scrollObserver.observe(item);
});

function getRoomImageClass(roomType) {
  if (roomType.includes("Double")) {
    return "room-two";
  }

  if (roomType.includes("Deluxe")) {
    return "room-three";
  }

  return "room-one";
}

function loadRooms() {
  fetch("http://localhost:8080/api/rooms")
    .then(response => response.json())
    .then(rooms => {
      const roomGrid = document.getElementById("roomGrid");

      roomGrid.innerHTML = "";

      const uniqueRoomTypes = [];

      rooms.forEach(room => {
        const alreadyAdded = uniqueRoomTypes.some(
          existingRoom => existingRoom.roomType === room.roomType
        );

        if (!alreadyAdded) {
          uniqueRoomTypes.push(room);
        }
      });

      uniqueRoomTypes.forEach(room => {
        const roomCard = document.createElement("div");
        roomCard.classList.add("room-card");

        roomCard.innerHTML = `
          <div class="room-img ${getRoomImageClass(room.roomType)}"></div>
          <h3>${room.roomType}</h3>
          <p><i class="fa-solid fa-bed"></i> ${room.maxGuests} Guests</p>
          <p><i class="fa-solid fa-wifi"></i> ${room.description}</p>
          <p class="price">$${room.pricePerNight} / night</p>
          <button onclick="showRoom('${room.roomType}', '$${room.pricePerNight} / night', '${room.description}')">
            View Details
          </button>
        `;

        roomGrid.appendChild(roomCard);
      });
    })
    .catch(error => {
      console.error("Error loading rooms:", error);
    });
}

loadRooms();