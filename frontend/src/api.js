
const API = (
  import.meta.env.VITE_API_URL ||
  "http://localhost:8081/api"
).replace(/\/+$/, "");

// Get authentication token
function auth() {
  const token = sessionStorage.getItem('rhToken');

  return token
    ? { Authorization: `Bearer ${token}` }
    : {};
}


// Handle API responses
async function handle(response) {
  // Only treat 401 as an expired or invalid login session.
  // A 403 is a permission/security error and should remain visible.
  if (response.status === 401) {
    sessionStorage.removeItem('rhToken');
    sessionStorage.removeItem('rhUser');
    sessionStorage.removeItem('rhLastActivity');

    if (
      location.pathname !== '/login' &&
      location.pathname !== '/register'
    ) {
      location.href = '/login';
    }

    throw new Error('Session expired. Please login again.');
  }

  if (!response.ok) {
    let message = `Request failed (${response.status})`;

    try {
      const data = await response.json();

      if (data.message) {
        message = data.message;
      } else if (data.error) {
        message = data.error;
      }
    } catch {
      // Keep the default error message if the response isn't JSON.
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}


// General API request helper
async function req(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...auth(),
      ...(options.headers || {})
    }
  });

  return handle(response);
}


// Get all properties or search properties
export const getProperties = (search) =>
  req(
    `${API}/properties${
      search ? `?search=${encodeURIComponent(search)}` : ''
    }`
  );


// Create property (admin)
export const createProperty = (property) =>
  req(`${API}/properties`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(property)
  });


// Update property (admin)
export const updateProperty = (id, property) =>
  req(`${API}/properties/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(property)
  });


// Delete property (admin)
export const deleteProperty = (id) =>
  req(`${API}/properties/${id}`, {
    method: 'DELETE'
  });


// Register a new user
export const register = async (name, email, password) => {
  const response = await fetch(`${API}/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name: name.trim(),
      email: email.trim(),
      password
    })
  });

  return handle(response);
};


// Login user
export const login = async (email, password) => {
  const response = await fetch(`${API}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: email.trim(),
      password
    })
  });

  const data = await handle(response);

  const user = {
    ...data.user,
    token: data.token
  };

  sessionStorage.setItem('rhToken', data.token);
  sessionStorage.setItem('rhUser', JSON.stringify(user));
  sessionStorage.setItem(
    'rhLastActivity',
    String(Date.now())
  );

  return user;
};


// Logout user
export const logout = () => {
  sessionStorage.removeItem('rhToken');
  sessionStorage.removeItem('rhUser');
  sessionStorage.removeItem('rhLastActivity');
};


// Apply for a property
export const applyForProperty = (propertyId) =>
  req(`${API}/applications`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ propertyId })
  });


// Get applications for a user
export const getUserApplications = (id) =>
  req(`${API}/applications/user/${id}`);


// Get all applications (admin)
export const getAllApplications = () =>
  req(`${API}/applications`);


// Approve application (admin)
export const approveApplication = (id) =>
  req(`${API}/applications/${id}/approve`, {
    method: 'PUT'
  });


// Reject application (admin)
export const rejectApplication = (id) =>
  req(`${API}/applications/${id}/reject`, {
    method: 'PUT'
  });


// Cancel application
export const cancelApplication = (id) =>
  req(`${API}/applications/${id}/cancel`, {
    method: 'PUT'
  });


// Cancel approval (admin)
export const cancelApproval = (id) =>
  req(`${API}/applications/${id}/cancel-approval`, {
    method: 'PUT'
  });


// Get users (admin)
export const getUsers = () =>
  req(`${API}/users`);


// Delete a user (admin)
export const deleteUser = (id) =>
  req(`${API}/users/${id}`, {
    method: 'DELETE'
  });


// Delete the logged-in user's account
export const deleteMyAccount = (id) =>
  req(`${API}/users/${id}`, {
    method: 'DELETE'
  });