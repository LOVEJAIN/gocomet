const API = 'http://localhost:8080/v1';

function showToast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 3000);
}

function setMsg(id, msg, type) {
  const el = document.getElementById(id);
  el.textContent = msg;
  el.className = 'msg ' + (type || '');
}

async function api(method, path, body) {
  try {
    const res = await fetch(API + path, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: body ? JSON.stringify(body) : undefined
    });
    return await res.json();
  } catch (e) {
    return { success: false, message: 'Cannot reach backend' };
  }
}

// ---- Ride Request ----
document.getElementById('rideForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const body = {
    riderId: +document.getElementById('riderId').value,
    pickupLat: +document.getElementById('pickupLat').value,
    pickupLng: +document.getElementById('pickupLng').value,
    dropLat: +document.getElementById('dropLat').value,
    dropLng: +document.getElementById('dropLng').value,
    pickupAddress: document.getElementById('pickupAddr').value,
    dropAddress: document.getElementById('dropAddr').value,
    tier: document.getElementById('tier').value,
    paymentMethod: document.getElementById('paymentMethod').value,
    idempotencyKey: 'key-' + Date.now()
  };
  const res = await api('POST', '/rides', body);
  if (res.success) {
    showToast(`Ride #${res.data.id} created! Status: ${res.data.status}`);
    document.getElementById('actionRideId').value = res.data.id;
    fetchRides();
  } else {
    showToast('Error: ' + res.message);
  }
});

// ---- Fetch & Render Rides ----
async function fetchRides() {
  const res = await api('GET', '/rides');
  if (!res.success) return;
  const rides = res.data || [];
  const el = document.getElementById('ridesList');
  if (!rides.length) { el.innerHTML = '<p class="empty-state">No rides yet.</p>'; return; }

  // Show latest first
  rides.sort((a, b) => b.id - a.id);
  el.innerHTML = rides.slice(0, 20).map(r => `
    <div class="ride-card">
      <div class="ride-card-top">
        <span class="ride-id">Ride #${r.id}</span>
        <span class="status-badge s-${r.status.toLowerCase()}">${r.status.replace('_', ' ')}</span>
      </div>
      <div class="ride-info">
        <span>📍 ${r.pickupAddress || r.pickupLat + ',' + r.pickupLng}</span>
        <span>→ ${r.dropAddress || r.dropLat + ',' + r.dropLng}</span><br>
        <span>🚗 ${r.tier}</span>
        <span>💳 ${r.paymentMethod}</span>
        <span>📏 ${r.distanceKm} km</span>
        ${r.driverId ? `<span>👤 Driver #${r.driverId}</span>` : ''}
        <span class="fare">₹${r.fare} ${r.surgeMultiplier > 1 ? `(${r.surgeMultiplier}x surge)` : ''}</span>
      </div>
    </div>
  `).join('');
}

// ---- Driver ----
async function createDriver() {
  const body = {
    name: document.getElementById('driverName').value,
    phone: document.getElementById('driverPhone').value,
    vehicleType: document.getElementById('vehicleType').value,
    status: 'AVAILABLE',
    latitude: +document.getElementById('driverLat').value,
    longitude: +document.getElementById('driverLng').value
  };
  const res = await api('POST', '/drivers', body);
  if (res.success) {
    setMsg('driverMsg', `Driver registered! ID: ${res.data.id}`, 'success');
    document.getElementById('driverIdInput').value = res.data.id;
    showToast(`Driver #${res.data.id} registered`);
  } else {
    setMsg('driverMsg', 'Error: ' + res.message, 'error');
  }
}

async function sendLocation() {
  const driverId = document.getElementById('driverIdInput').value;
  const body = {
    latitude: +document.getElementById('driverLat').value,
    longitude: +document.getElementById('driverLng').value
  };
  const res = await api('POST', `/drivers/${driverId}/location`, body);
  if (res.success) {
    setMsg('driverMsg', `Location updated for Driver #${driverId}`, 'success');
  } else {
    setMsg('driverMsg', 'Error: ' + res.message, 'error');
  }
}

// ---- Ride Actions ----
async function startTrip() {
  const id = document.getElementById('actionRideId').value;
  if (!id) { setMsg('actionMsg', 'Enter Ride ID', 'error'); return; }
  const res = await api('POST', `/trips/${id}/start`);
  setMsg('actionMsg', res.success ? `Ride #${id} started!` : res.message, res.success ? 'success' : 'error');
  if (res.success) fetchRides();
}

async function endTrip() {
  const id = document.getElementById('actionRideId').value;
  if (!id) { setMsg('actionMsg', 'Enter Ride ID', 'error'); return; }
  const res = await api('POST', `/trips/${id}/end`);
  setMsg('actionMsg', res.success ? `Ride #${id} ended! Fare: ₹${res.data?.fare}` : res.message, res.success ? 'success' : 'error');
  if (res.success) fetchRides();
}

async function processPayment() {
  const id = document.getElementById('actionRideId').value;
  if (!id) { setMsg('actionMsg', 'Enter Ride ID', 'error'); return; }
  const body = { rideId: +id, riderId: +document.getElementById('riderId').value, idempotencyKey: 'pay-' + id };
  const res = await api('POST', `/payments`, body);
  setMsg('actionMsg', res.success ? `Payment done! Ref: ${res.data?.transactionRef}` : res.message, res.success ? 'success' : 'error');
}

async function cancelRide() {
  const id = document.getElementById('actionRideId').value;
  if (!id) { setMsg('actionMsg', 'Enter Ride ID', 'error'); return; }
  const res = await api('POST', `/rides/${id}/cancel`);
  setMsg('actionMsg', res.success ? `Ride #${id} cancelled` : res.message, res.success ? 'success' : 'error');
  if (res.success) fetchRides();
}

// Live polling every 5 seconds
setInterval(fetchRides, 5000);
fetchRides();
