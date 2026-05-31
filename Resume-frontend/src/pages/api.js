// src/pages/api.js

const API_BASE_URL = '/api';

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

  // Don't set Content-Type for FormData — browser sets it with boundary
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  });

  if (response.status === 401 || response.status === 403) {
    localStorage.removeItem('token');
    window.location.href = '/login';
    throw new Error('Authentication required');
  }

  return response;
};

export const api = {

  // ─────────────────────────────────────────────────────────────────
  //  AUTH
  // ─────────────────────────────────────────────────────────────────

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

  // ─────────────────────────────────────────────────────────────────
  //  RESUMES
  // ─────────────────────────────────────────────────────────────────

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
    if (!response.ok) throw new Error('Failed to fetch resumes');
    return await response.json();
  },

  getResume: async (id) => {
    const response = await authFetch(`/resumes/${id}`);
    if (!response.ok) throw new Error('Failed to fetch resume');
    return await response.json();
  },

  deleteResume: async (id) => {
    const response = await authFetch(`/resumes/${id}`, { method: 'DELETE' });
    if (!response.ok) throw new Error('Failed to delete resume');
    return await response.text();
  },

  // ─────────────────────────────────────────────────────────────────
  //  JOB ANALYSIS  (AnalysisController)
  // ─────────────────────────────────────────────────────────────────

  analyzeForJob: async (resumeId, jobDescription, jobTitle = '', company = '', requirements = '') => {
    const response = await authFetch('/analysis/match-job', {
      method: 'POST',
      body: JSON.stringify({ resumeId, jobDescription, jobTitle, company, requirements }),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Analysis failed');
    }
    return await response.json();
  },

  getResumeAnalyses: async (resumeId) => {
    const response = await authFetch(`/analysis/resume/${resumeId}`);
    if (!response.ok) throw new Error('Failed to fetch analyses');
    return await response.json();
  },

  // ─────────────────────────────────────────────────────────────────
  //  RAG & AI  (AIController)
  // ─────────────────────────────────────────────────────────────────

  /** RAG Career Coaching — grounded in resume vector chunks */
  ragCoach: async (resumeId, query) => {
    const response = await authFetch('/ai/coaching', {
      method: 'POST',
      body: JSON.stringify({ resumeId, query }),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Coaching request failed');
    }
    return await response.json();
  },

  /** Candidate Matching — global vector search for a job description */
  matchCandidates: async (jobDescription, limit = 5) => {
    const response = await authFetch('/ai/match-candidates', {
      method: 'POST',
      body: JSON.stringify({ jobDescription, limit }),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Candidate matching failed');
    }
    return await response.json();
  },

  /** Re-index a resume's embeddings in the vector store */
  indexResume: async (resumeId) => {
    const response = await authFetch(`/ai/index/${resumeId}`, { method: 'POST' });
    if (!response.ok) throw new Error('Failed to index resume');
    return await response.json();
  },

  /** Cover Letter Generator */
  generateCoverLetter: async (resumeId, jobTitle, company, jobDescription) => {
    const response = await authFetch('/ai/cover-letter', {
      method: 'POST',
      body: JSON.stringify({ resumeId, jobTitle, company, jobDescription }),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Cover letter generation failed');
    }
    return await response.json();
  },

  /** Interview Coach — 10 Q&A pairs tailored to resume + job */
  getInterviewCoach: async (resumeId, jobDescription) => {
    const response = await authFetch('/ai/interview-coach', {
      method: 'POST',
      body: JSON.stringify({ resumeId, jobDescription }),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Interview coach request failed');
    }
    return await response.json();
  },

  /** Career Path Advisor — 3 paths with 90-day action plans */
  getCareerPaths: async (resumeId) => {
    const response = await authFetch('/ai/career-paths', {
      method: 'POST',
      body: JSON.stringify({ resumeId }),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Career paths request failed');
    }
    return await response.json();
  },
};

export default api;