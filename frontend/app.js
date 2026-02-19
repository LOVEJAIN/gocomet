const API = 'http://localhost:8080/v1';

const liveIndicator = document.getElementById('liveIndicator');
const ridesTable = document.getElementById('ridesTable');
const driversTable = document.getElementById('driversTable');
const activityLog = document.getElementById('activityLog');

function logActivity(message, type = 'info') {
  const row = document.createElement('div');
  row.className = `log-row ${type}`;
  row.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
  activityLog.prepend(row);
}

function setLive(ok) {
  if (ok) {
    liveIndicator.classList.add('live');
  } else {
    liveIndicator.classList.remove('live');
  }
}

async function api(method, path, body) {
  try {
    const res = await fetch(`${API}${path}`, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: body ? JSON.stringify(body) : undefined
    });
    const json = await res.json();
    if (!res.ok || json.success === false) {
      throw new Error(json.message || `Request failed: ${res.status}`);
    }
    return json;
  } catch (err) {
    logActivity(err.message, 'error');
    setLive(false);
    throw err;
  }
}

async function createRide() {
  const body = {
    riderId: +document.getElementById('riderId').value,
    pickupLat: +document.getElementById('pickupLat').value,
    pickupLng: +document.getElementById('pickupLng').value,
    dropLat: +document.getElementById('destLat').value,
    dropLng: +document.getElementById('destLng').value,
    pickupAddress: document.getElementById('pickupAddr').value,
    dropAddress: document.getElementById('destAddr').value,
    tier: document.getElementById('tier').value,
    paymentMethod: document.getElementById('paymentMethod').value,
    idempotencyKey: `ride-${Date.now()}`
  };

  const res = await api('POST', '/rides', body);
  logActivity(`Ride #${res.data.id} created (${res.data.status})`, 'success');
  document.getElementById('acceptRideId').value = res.data.id;
  document.getElementById('lifecycleRideId').value = res.data.id;
  document.getElementById('payRideId').value = res.data.id;
  await fetchRides();
}

async function updateDriverLocation() {
  const driverId = document.getElementById('driverId').value;
  const body = {
    latitude: +document.getElementById('driverLat').value,
    longitude: +document.getElementById('driverLng').value
  };
  await api('POST', `/drivers/${driverId}/location`, body);
  logActivity(`Driver #${driverId} location updated`, 'success');
  await fetchDrivers();
}

async function setDriverStatus(status) {
  const driverId = document.getElementById('driverId').value;
  await api('POST', `/drivers/${driverId}/status?status=${encodeURIComponent(status)}`);
  logActivity(`Driver #${driverId} set to ${status}`, 'success');
  await fetchDrivers();
}

async function acceptRide() {
  const driverId = document.getElementById('driverId').value;
  const rideId = document.getElementById('acceptRideId').value;
  await api('POST', `/drivers/${driverId}/accept?rideId=${encodeURIComponent(rideId)}`);
  logActivity(`Driver #${driverId} accepted Ride #${rideId}`, 'success');
  await fetchRides();
}

async function startTrip() {
  const rideId = document.getElementById('lifecycleRideId').value;
  await api('POST', `/trips/${rideId}/start`);
  logActivity(`Trip started for Ride #${rideId}`, 'success');
  await fetchRides();
}

async function endTrip() {
  const rideId = document.getElementById('lifecycleRideId').value;
  const res = await api('POST', `/trips/${rideId}/end`);
  logActivity(`Trip ended for Ride #${rideId}. Fare ₹${res.data?.fare}`, 'success');
  await fetchRides();
}

async function cancelRide() {
  const rideId = document.getElementById('lifecycleRideId').value;
  await api('POST', `/rides/${rideId}/cancel`);
  logActivity(`Ride #${rideId} cancelled`, 'warn');
  await fetchRides();
}

async function payRide() {
  const rideId = document.getElementById('payRideId').value;
  const body = {
    rideId: +rideId,
    riderId: +document.getElementById('riderId').value,
    idempotencyKey: `pay-${rideId}-${Date.now()}`
  };
  const res = await api('POST', `/payments`, body);
  logActivity(`Payment success. Ref ${res.data?.transactionRef}`, 'success');
}

async function registerDriver() {
  const body = {
    name: document.getElementById('driverName').value,
    phone: document.getElementById('driverPhone').value,
    vehicleType: document.getElementById('driverVehicleType').value,
    status: 'AVAILABLE',
    latitude: +document.getElementById('driverLat').value,
    longitude: +document.getElementById('driverLng').value
  };
  const res = await api('POST', '/drivers', body);
  document.getElementById('driverId').value = res.data.id;
  logActivity(`Driver #${res.data.id} registered`, 'success');
  await fetchDrivers();
}

async function fetchRides() {
  try {
    const res = await api('GET', '/rides');
    const rides = res.data || [];
    if (!rides.length) {
      ridesTable.innerHTML = '<p class="empty-msg">No rides yet.</p>';
      setLive(true);
      return;
    }

    rides.sort((a, b) => b.id - a.id);
    ridesTable.innerHTML = rides.slice(0, 20).map((r) => `
      <div class="row">
        <div class="row-title">Ride #${r.id} - ${r.status}</div>
        <div class="row-meta">
          ${r.pickupAddress || `${r.pickupLat},${r.pickupLng}`} →
          ${r.dropAddress || `${r.dropLat},${r.dropLng}`} |
          ${r.tier} | ${r.paymentMethod} | ${r.distanceKm} km
        </div>
      </div>
    `).join('');
    setLive(true);
  } catch {
    ridesTable.innerHTML = '<p class="empty-msg">Backend not reachable.</p>';
  }
}

async function fetchDrivers() {
  try {
    const res = await api('GET', '/drivers');
    const drivers = res.data || [];
    if (!drivers.length) {
      driversTable.innerHTML = '<p class="empty-msg">No drivers yet.</p>';
      setLive(true);
      return;
    }

    driversTable.innerHTML = drivers.slice(0, 20).map((d) => `
      <div class="row">
        <div class="row-title">Driver #${d.id} - ${d.status}</div>
        <div class="row-meta">
          ${d.name || 'N/A'} | ${d.phone || 'N/A'} | ${d.vehicleType || 'N/A'}
        </div>
      </div>
    `).join('');
    setLive(true);
  } catch {
    driversTable.innerHTML = '<p class="empty-msg">Backend not reachable.</p>';
  }
}

function clearLog() {
  activityLog.innerHTML = '';
}

setInterval(() => {
  fetchRides();
  fetchDrivers();
}, 5000);

fetchRides();
fetchDrivers();
