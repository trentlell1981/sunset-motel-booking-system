const API_BASE_URL = "http://localhost:8080";

let adminUsername = "";
let adminPassword = "";
let allBookings = [];
let currentFilter = "ALL";

function adminLogin(e) {
  e.preventDefault();

  const loginMessage = document.getElementById("adminLoginMessage");

  loginMessage.textContent = "";

  adminUsername = document.getElementById("adminUsername").value.trim();
  adminPassword = document.getElementById("adminPassword").value;

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

  document.getElementById("adminLoginMessage").textContent = "";
  document.getElementById("adminLoginForm").style.display = "block";
  document.getElementById("adminControls").style.display = "none";
}

function getAuthHeader() {
  return `Basic ${btoa(`${adminUsername}:${adminPassword}`)}`;
}

function getAuthHeaders() {
  return {
    "Authorization": getAuthHeader()
  };
}

function getJsonAuthHeaders() {
  return {
    "Content-Type": "application/json",
    "Authorization": getAuthHeader()
  };
}

function loadBookings(filter = "ALL") {
  currentFilter = filter;

  fetch(`${API_BASE_URL}/api/bookings`, {
    headers: getAuthHeaders()
  })
    .then(handleResponse)
    .then(bookings => {
      allBookings = bookings;

      document.getElementById("adminLoginForm").style.display = "none";
      document.getElementById("adminControls").style.display = "block";
      document.getElementById("adminLoginMessage").textContent = "";

      renderFilteredBookings();
    })
    .catch(error => {
      console.error("Error loading bookings:", error);

      document.getElementById("adminLoginMessage").textContent =
        "Login failed. Please check your username and password.";

      document.getElementById("adminLoginForm").style.display = "block";
      document.getElementById("adminControls").style.display = "none";
    });
}

function renderFilteredBookings() {
  const bookingGrid = document.getElementById("adminBookingGrid");
  const searchInput = document.getElementById("adminSearchInput");
  const searchTerm = searchInput ? searchInput.value.toLowerCase().trim() : "";

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
      ${booking.room?.roomNumber}
      ${booking.room?.roomType}
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

    const statusClass = booking.status === "CANCELLED"
      ? "status-cancelled"
      : "status-confirmed";

    const cancelButton = booking.status === "CANCELLED"
      ? "<button disabled>Already Cancelled</button>"
      : `<button onclick="cancelBooking(${booking.id})">Cancel Booking</button>`;

    bookingCard.innerHTML = `
      <h3>Booking #${escapeHtml(booking.id)}</h3>
      <p><strong>Guest:</strong> ${escapeHtml(booking.guestName)}</p>
      <p><strong>Email:</strong> ${escapeHtml(booking.guestEmail)}</p>
      <p><strong>Phone:</strong> ${escapeHtml(booking.guestPhone)}</p>
      <p><strong>Guests:</strong> ${escapeHtml(booking.guestCount)}</p>
      <p><strong>Room:</strong> ${escapeHtml(booking.room?.roomNumber)} - ${escapeHtml(booking.room?.roomType)}</p>
      <p><strong>Check-in:</strong> ${escapeHtml(booking.checkInDate)}</p>
      <p><strong>Check-out:</strong> ${escapeHtml(booking.checkOutDate)}</p>
      <p><strong>Total:</strong> $${escapeHtml(booking.totalPrice)}</p>
      <p>
        <strong>Status:</strong>
        <span class="status-badge ${statusClass}">
          ${escapeHtml(booking.status)}
        </span>
      </p>
      ${cancelButton}
    `;

    bookingGrid.appendChild(bookingCard);
  });
}

function cancelBooking(id) {
  const confirmed = confirm(`Are you sure you want to cancel Booking #${id}?`);

  if (!confirmed) {
    return;
  }

  fetch(`${API_BASE_URL}/api/bookings/${id}/cancel`, {
    method: "PUT",
    headers: getAuthHeaders()
  })
    .then(handleResponse)
    .then(booking => {
      alert(`Booking #${booking.id} has been cancelled.`);
      loadBookings(currentFilter);
    })
    .catch(error => {
      alert(error.message);
    });
}

function loadAdminRooms() {
  fetch(`${API_BASE_URL}/api/rooms`)
    .then(handleResponse)
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

        const statusClass = room.active ? "status-confirmed" : "status-cancelled";
        const statusText = room.active ? "ACTIVE" : "INACTIVE";

        const statusButton = room.active
          ? `<button onclick="deactivateRoom(${room.id})">Deactivate Room</button>`
          : `<button onclick="reactivateRoom(${room.id})">Reactivate Room</button>`;

        roomCard.innerHTML = `
          <h3>Room ${escapeHtml(room.roomNumber)}</h3>
          <p><strong>Type:</strong> ${escapeHtml(room.roomType)}</p>
          <p><strong>Price:</strong> $${escapeHtml(room.pricePerNight)} per night</p>
          <p><strong>Max Guests:</strong> ${escapeHtml(room.maxGuests)}</p>
          <p><strong>Description:</strong> ${escapeHtml(room.description)}</p>
          <p>
            <strong>Status:</strong>
            <span class="status-badge ${statusClass}">
              ${statusText}
            </span>
          </p>

          <button onclick="showEditRoomForm(${room.id})">Edit Room</button>
          ${statusButton}

          <div id="editRoomForm-${room.id}" class="edit-room-form" style="display: none;">
            <h4>Edit Room ${escapeHtml(room.roomNumber)}</h4>

            <label>Room Type</label>
            <input type="text" id="editRoomType-${room.id}" value="${escapeAttribute(room.roomType)}">

            <label>Price Per Night</label>
            <input type="number" id="editRoomPrice-${room.id}" min="1" step="0.01" value="${escapeAttribute(room.pricePerNight)}">

            <label>Max Guests</label>
            <input type="number" id="editRoomMaxGuests-${room.id}" min="1" value="${escapeAttribute(room.maxGuests)}">

            <label>Description</label>
            <input type="text" id="editRoomDescription-${room.id}" value="${escapeAttribute(room.description)}">

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

  fetch(`${API_BASE_URL}/api/rooms/${id}/deactivate`, {
    method: "PUT",
    headers: getAuthHeaders()
  })
    .then(handleResponse)
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

  fetch(`${API_BASE_URL}/api/rooms/${id}/reactivate`, {
    method: "PUT",
    headers: getAuthHeaders()
  })
    .then(handleResponse)
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
    roomNumber: document.getElementById("newRoomNumber").value.trim(),
    roomType: document.getElementById("newRoomType").value.trim(),
    pricePerNight: Number(document.getElementById("newRoomPrice").value),
    maxGuests: Number(document.getElementById("newRoomMaxGuests").value),
    description: document.getElementById("newRoomDescription").value.trim()
  };

  fetch(`${API_BASE_URL}/api/rooms`, {
    method: "POST",
    headers: getJsonAuthHeaders(),
    body: JSON.stringify(room)
  })
    .then(handleResponse)
    .then(savedRoom => {
      const message = `Room ${savedRoom.roomNumber} has been added.`;

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

  editForm.style.display = editForm.style.display === "none"
    ? "block"
    : "none";
}

function updateRoom(id) {
  const room = {
    roomType: document.getElementById(`editRoomType-${id}`).value.trim(),
    pricePerNight: Number(document.getElementById(`editRoomPrice-${id}`).value),
    maxGuests: Number(document.getElementById(`editRoomMaxGuests-${id}`).value),
    description: document.getElementById(`editRoomDescription-${id}`).value.trim()
  };

  fetch(`${API_BASE_URL}/api/rooms/${id}`, {
    method: "PUT",
    headers: getJsonAuthHeaders(),
    body: JSON.stringify(room)
  })
    .then(handleResponse)
    .then(updatedRoom => {
      alert(`Room ${updatedRoom.roomNumber} has been updated.`);
      loadAdminRooms();
    })
    .catch(error => {
      alert(error.message);
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

function escapeHtml(value) {
  if (value === null || value === undefined) {
    return "";
  }

  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttribute(value) {
  return escapeHtml(value);
}