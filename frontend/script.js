const API_BASE_URL = "http://localhost:8080";

function showRoom(name, price, details) {
  alert(`${name}\n${price}\n\n${details}`);
}

function selectRoom(roomId, roomType, roomNumber) {
  const selectedRoomIdInput = document.getElementById("selectedRoomId");
  const selectedRoomMessage = document.getElementById("selectedRoomMessage");
  const bookingConfirmationMessage = document.getElementById("bookingConfirmationMessage");
  const guestBookingForm = document.getElementById("guestBookingForm");
  const submitButton = document.querySelector("#guestBookingForm button");

  selectedRoomIdInput.value = roomId;
  selectedRoomMessage.textContent = `You selected Room ${roomNumber}: ${roomType}`;
  bookingConfirmationMessage.textContent = "";

  guestBookingForm.style.display = "block";

  submitButton.disabled = false;
  submitButton.textContent = "Confirm Booking";

  guestBookingForm.scrollIntoView({
    behavior: "smooth"
  });
}

function submitBooking(e) {
  e.preventDefault();

  const bookingConfirmationMessage = document.getElementById("bookingConfirmationMessage");
  const submitButton = document.querySelector("#guestBookingForm button");

  const bookingRequest = {
    guestName: document.getElementById("guestName").value.trim(),
    guestEmail: document.getElementById("guestEmail").value.trim(),
    guestPhone: document.getElementById("guestPhone").value.trim(),
    guestCount: Number(document.getElementById("guests").value),
    roomId: Number(document.getElementById("selectedRoomId").value),
    checkInDate: document.getElementById("checkIn").value,
    checkOutDate: document.getElementById("checkOut").value
  };

  fetch(`${API_BASE_URL}/api/bookings`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(bookingRequest)
  })
    .then(handleResponse)
    .then(booking => {
      bookingConfirmationMessage.textContent =
        `Booking confirmed for ${booking.guestName}. Confirmation number: ${booking.id}.`;

      document.getElementById("guestName").value = "";
      document.getElementById("guestEmail").value = "";
      document.getElementById("guestPhone").value = "";

      submitButton.disabled = true;
      submitButton.textContent = "Booking Confirmed";
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
  const message = document.getElementById("availabilityMessage");
  const roomGrid = document.getElementById("availableRoomGrid");
  const bookingConfirmationMessage = document.getElementById("bookingConfirmationMessage");
  const guestBookingForm = document.getElementById("guestBookingForm");

  if (!checkIn || !checkOut) {
    alert("Please select check-in and check-out dates.");
    return;
  }

  if (checkOut <= checkIn) {
    alert("Check-out date must be after check-in date.");
    return;
  }

  const availabilityUrl =
    `${API_BASE_URL}/api/rooms/available` +
    `?checkIn=${encodeURIComponent(checkIn)}` +
    `&checkOut=${encodeURIComponent(checkOut)}` +
    `&guests=${encodeURIComponent(guests)}`;

  fetch(availabilityUrl)
    .then(handleResponse)
    .then(rooms => {
      roomGrid.innerHTML = "";
      bookingConfirmationMessage.textContent = "";
      guestBookingForm.style.display = "none";

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
          <button onclick='selectRoom(${room.id}, ${JSON.stringify(room.roomType)}, ${JSON.stringify(room.roomNumber)})'>
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
      alert(error.message || "Something went wrong while checking availability.");
    });
}

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
  fetch(`${API_BASE_URL}/api/rooms`)
    .then(handleResponse)
    .then(rooms => {
      const roomGrid = document.getElementById("roomGrid");
      const uniqueRoomTypes = [];

      roomGrid.innerHTML = "";

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
          <button onclick='showRoom(${JSON.stringify(room.roomType)}, "$${room.pricePerNight} / night", ${JSON.stringify(room.description)})'>
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

function handleResponse(response) {
  if (!response.ok) {
    return response.text().then(message => {
      throw new Error(message || "Request failed.");
    });
  }

  return response.json();
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

loadRooms();