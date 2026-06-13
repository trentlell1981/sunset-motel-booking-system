let adminUsername = "";
let adminPassword = "";
let allBookings = [];
let currentFilter = "ALL";

function adminLogin(e) {
  e.preventDefault();

  document.getElementById("adminLoginMessage").textContent = "";

  adminUsername = document.getElementById("adminUsername").value;
  adminPassword = document.getElementById("adminPassword").value;

  document.getElementById("adminLoginForm").style.display = "none";
  document.getElementById("adminControls").style.display = "block";

  loadBookings("ALL");
}

function adminLogout() {
  adminUsername = "";
  adminPassword = "";
  allBookings = [];
  currentFilter = "ALL";

  document.getElementById("adminUsername").value = "";
  document.getElementById("adminPassword").value = "";
  document.getElementById("adminBookingGrid").innerHTML = "";
  document.getElementById("adminRoomGrid").innerHTML = "";
  document.getElementById("adminSearchInput").value = "";

  document.getElementById("adminLoginForm").style.display = "block";
  document.getElementById("adminControls").style.display = "none";
}

function getAuthHeader() {
  return "Basic " + btoa(`${adminUsername}:${adminPassword}`);
}

function loadBookings(filter = "ALL") {
  currentFilter = filter;

  fetch("http://localhost:8080/api/bookings", {
    headers: {
      "Authorization": getAuthHeader()
    }
  })
    .then(response => {
      if (!response.ok) {
        return response.text().then(message => {
          throw new Error(message || "Login failed");
        });
      }

      return response.json();
    })
    .then(bookings => {
      allBookings = bookings;
      renderFilteredBookings();
    })
    .catch(error => {
      document.getElementById("adminLoginMessage").textContent =
        "Login failed. Please check your username and password.";

      document.getElementById("adminLoginForm").style.display = "block";
      document.getElementById("adminControls").style.display = "none";
    });
}

function renderFilteredBookings() {
  const bookingGrid = document.getElementById("adminBookingGrid");
  const searchInput = document.getElementById("adminSearchInput");
  const searchTerm = searchInput ? searchInput.value.toLowerCase() : "";

  bookingGrid.innerHTML = "";

  let filteredBookings = currentFilter === "ALL"
    ? allBookings
    : allBookings.filter(booking => booking.status === currentFilter);

  filteredBookings = filteredBookings.filter(booking => {
    const bookingText = `
      ${booking.id}
      ${booking.guestName}
      ${booking.guestEmail}
      ${booking.guestPhone}
      ${booking.guestCount}
      ${booking.room.roomNumber}
      ${booking.room.roomType}
      ${booking.checkInDate}
      ${booking.checkOutDate}
      ${booking.totalPrice}
      ${booking.status}
    `.toLowerCase();

    return bookingText.includes(searchTerm);
  });

  if (filteredBookings.length === 0) {
    bookingGrid.innerHTML = "<p>No matching bookings found.</p>";
    return;
  }

  filteredBookings.forEach(booking => {
    const bookingCard = document.createElement("div");
    bookingCard.classList.add("available-room-card");

    if (booking.status === "CANCELLED") {
      bookingCard.classList.add("cancelled-booking");
    }

    bookingCard.innerHTML = `
      <h3>Booking #${booking.id}</h3>
      <p><strong>Guest:</strong> ${booking.guestName}</p>
      <p><strong>Email:</strong> ${booking.guestEmail}</p>
      <p><strong>Phone:</strong> ${booking.guestPhone}</p>
      <p><strong>Guests:</strong> ${booking.guestCount}</p>
      <p><strong>Room:</strong> ${booking.room.roomNumber} - ${booking.room.roomType}</p>
      <p><strong>Check-in:</strong> ${booking.checkInDate}</p>
      <p><strong>Check-out:</strong> ${booking.checkOutDate}</p>
      <p><strong>Total:</strong> $${booking.totalPrice}</p>
      <p>
        <strong>Status:</strong>
        <span class="status-badge ${booking.status === "CANCELLED" ? "status-cancelled" : "status-confirmed"}">
          ${booking.status}
        </span>
      </p>
      ${booking.status === "CANCELLED"
        ? "<button disabled>Already Cancelled</button>"
        : `<button onclick="cancelBooking(${booking.id})">Cancel Booking</button>`
      }
    `;

    bookingGrid.appendChild(bookingCard);
  });
}

function cancelBooking(id) {
  const confirmed = confirm(`Are you sure you want to cancel Booking #${id}?`);

  if (!confirmed) {
    return;
  }

  fetch(`http://localhost:8080/api/bookings/${id}/cancel`, {
    method: "PUT",
    headers: {
      "Authorization": getAuthHeader()
    }
  })
    .then(response => {
      if (!response.ok) {
        return response.text().then(message => {
          throw new Error(message || "Failed to cancel booking");
        });
      }

      return response.json();
    })
    .then(booking => {
      alert(`Booking #${booking.id} has been cancelled.`);
      loadBookings("ALL");
    })
    .catch(error => {
      alert(error.message);
    });
}

function loadAdminRooms() {
  fetch("http://localhost:8080/api/rooms")
    .then(response => {
      if (!response.ok) {
        throw new Error("Failed to load rooms");
      }

      return response.json();
    })
    .then(rooms => {
      const roomGrid = document.getElementById("adminRoomGrid");

      roomGrid.innerHTML = "";

      if (rooms.length === 0) {
        roomGrid.innerHTML = "<p>No rooms found.</p>";
        return;
      }

      rooms.forEach(room => {
        const roomCard = document.createElement("div");
        roomCard.classList.add("available-room-card");

        if (!room.active) {
          roomCard.classList.add("cancelled-booking");
        }

        roomCard.innerHTML = `
          <h3>Room ${room.roomNumber}</h3>
          <p><strong>Type:</strong> ${room.roomType}</p>
          <p><strong>Price:</strong> $${room.pricePerNight} per night</p>
          <p><strong>Max Guests:</strong> ${room.maxGuests}</p>
          <p><strong>Description:</strong> ${room.description}</p>
          <p>
            <strong>Status:</strong>
            <span class="status-badge ${room.active ? "status-confirmed" : "status-cancelled"}">
              ${room.active ? "ACTIVE" : "INACTIVE"}
            </span>
          </p>

          <button onclick="showEditRoomForm(${room.id})">Edit Room</button>

          ${room.active
            ? `<button onclick="deactivateRoom(${room.id})">Deactivate Room</button>`
            : `<button onclick="reactivateRoom(${room.id})">Reactivate Room</button>`
          }

          <div id="editRoomForm-${room.id}" class="edit-room-form" style="display: none;">
            <h4>Edit Room ${room.roomNumber}</h4>

            <label>Room Type</label>
            <input type="text" id="editRoomType-${room.id}" value="${room.roomType}">

            <label>Price Per Night</label>
            <input type="number" id="editRoomPrice-${room.id}" min="1" step="0.01" value="${room.pricePerNight}">

            <label>Max Guests</label>
            <input type="number" id="editRoomMaxGuests-${room.id}" min="1" value="${room.maxGuests}">

            <label>Description</label>
            <input type="text" id="editRoomDescription-${room.id}" value="${room.description}">

            <button onclick="updateRoom(${room.id})">Save Changes</button>
          </div>
        `;

        roomGrid.appendChild(roomCard);
      });
    })
    .catch(error => {
      alert(error.message);
    });
}

function deactivateRoom(id) {
  const confirmed = confirm(`Are you sure you want to deactivate Room #${id}?`);

  if (!confirmed) {
    return;
  }

  fetch(`http://localhost:8080/api/rooms/${id}/deactivate`, {
    method: "PUT",
    headers: {
      "Authorization": getAuthHeader()
    }
  })
    .then(response => {
      if (!response.ok) {
        return response.text().then(message => {
          throw new Error(message || "Failed to deactivate room");
        });
      }

      return response.json();
    })
    .then(room => {
      alert(`Room ${room.roomNumber} has been deactivated.`);
      loadAdminRooms();
    })
    .catch(error => {
      alert(error.message);
    });
}

function reactivateRoom(id) {
  const confirmed = confirm(`Are you sure you want to reactivate Room #${id}?`);

  if (!confirmed) {
    return;
  }

  fetch(`http://localhost:8080/api/rooms/${id}/reactivate`, {
    method: "PUT",
    headers: {
      "Authorization": getAuthHeader()
    }
  })
    .then(response => {
      if (!response.ok) {
        return response.text().then(message => {
          throw new Error(message || "Failed to reactivate room");
        });
      }

      return response.json();
    })
    .then(room => {
      alert(`Room ${room.roomNumber} has been reactivated.`);
      loadAdminRooms();
    })
    .catch(error => {
      alert(error.message);
    });
}

function submitNewRoom(e) {
  e.preventDefault();

  const addRoomMessage = document.getElementById("addRoomMessage");

  addRoomMessage.textContent = "";
  addRoomMessage.style.color = "";

  const room = {
    roomNumber: document.getElementById("newRoomNumber").value,
    roomType: document.getElementById("newRoomType").value,
    pricePerNight: Number(document.getElementById("newRoomPrice").value),
    maxGuests: Number(document.getElementById("newRoomMaxGuests").value),
    description: document.getElementById("newRoomDescription").value
  };

  fetch("http://localhost:8080/api/rooms", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": getAuthHeader()
    },
    body: JSON.stringify(room)
  })
    .then(response => {
      if (!response.ok) {
        return response.text().then(message => {
          throw new Error(message || "Failed to add room");
        });
      }

      return response.json();
    })
    .then(room => {
      const message = `Room ${room.roomNumber} has been added.`;

      addRoomMessage.textContent = message;
      addRoomMessage.style.color = "green";

      alert(message);

      document.getElementById("newRoomNumber").value = "";
      document.getElementById("newRoomType").value = "";
      document.getElementById("newRoomPrice").value = "";
      document.getElementById("newRoomMaxGuests").value = "";
      document.getElementById("newRoomDescription").value = "";

      loadAdminRooms();
    })
    .catch(error => {
      addRoomMessage.textContent = error.message;
      addRoomMessage.style.color = "red";

      alert(error.message);
    });
}

function showEditRoomForm(id) {
  const editForm = document.getElementById(`editRoomForm-${id}`);

  if (editForm.style.display === "none") {
    editForm.style.display = "block";
  } else {
    editForm.style.display = "none";
  }
}

function updateRoom(id) {
  const room = {
    roomType: document.getElementById(`editRoomType-${id}`).value,
    pricePerNight: Number(document.getElementById(`editRoomPrice-${id}`).value),
    maxGuests: Number(document.getElementById(`editRoomMaxGuests-${id}`).value),
    description: document.getElementById(`editRoomDescription-${id}`).value
  };

  fetch(`http://localhost:8080/api/rooms/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "Authorization": getAuthHeader()
    },
    body: JSON.stringify(room)
  })
    .then(response => {
      if (!response.ok) {
        return response.text().then(message => {
          throw new Error(message || "Failed to update room");
        });
      }

      return response.json();
    })
    .then(room => {
      alert(`Room ${room.roomNumber} has been updated.`);
      loadAdminRooms();
    })
    .catch(error => {
      alert(error.message);
    });
}