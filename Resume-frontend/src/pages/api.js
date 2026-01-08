// src/services/api.js (or src/api.js)

const API_BASE_URL = 'http://localhost:8080/api';

// Get auth token from localStorage
const getAuthToken = () => {
  return localStorage.getItem('token');
};

// Helper function to make authenticated requests
const authFetch = async (url, options = {}) => {
  const token = getAuthToken();
  
  const headers = {
    ...options.headers,
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  // Don't set Content-Type for FormData, browser will set it with boundary
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }
  
  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  });
  
  if (response.status === 401) {
    // Token expired or invalid
    localStorage.removeItem('token');
    window.location.href = '/login';
    throw new Error('Authentication required');
  }
  
  return response;
};

// API functions
export const api = {
  // Auth endpoints
  login: async (email, password) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Login failed');
    }
    
    const data = await response.json();
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
    }));
    return data;
  },
  
  register: async (email, password, firstName, lastName) => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, firstName, lastName }),
    });
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Registration failed');
    }
    
    const data = await response.json();
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
    }));
    return data;
  },
  
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
  
  // Resume endpoints
  uploadResume: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await authFetch('/resumes/upload', {
      method: 'POST',
      body: formData,
    });
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Upload failed');
    }
    
    return await response.json();
  },
  
  getResumes: async () => {
    const response = await authFetch('/resumes');
    
    if (!response.ok) {
      throw new Error('Failed to fetch resumes');
    }
    
    return await response.json();
  },
  
  getResume: async (id) => {
    const response = await authFetch(`/resumes/${id}`);
    
    if (!response.ok) {
      throw new Error('Failed to fetch resume');
    }
    
    return await response.json();
  },
  
  deleteResume: async (id) => {
    const response = await authFetch(`/resumes/${id}`, {
      method: 'DELETE',
    });
    
    if (!response.ok) {
      throw new Error('Failed to delete resume');
    }
    
    return await response.json();
  },
};

export default api;