// src/pages/LoginPage.jsx
import React, { useState, useEffect } from 'react';
import { Mail, Lock, Eye, EyeOff, ArrowLeft, Zap, Loader2 } from 'lucide-react';
import axios from 'axios';
import './auth.css';

// ── SVG icons for OAuth providers ────────────────────────────────────────────
const GoogleIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="#FBBC05"/>
    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
  </svg>
);

const GitHubIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
    <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/>
  </svg>
);


export default function LoginPage({ setCurrentView, onLoginSuccess }) {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const savedEmail   = localStorage.getItem('rememberedEmail');
    const wasRemembered = localStorage.getItem('rememberMe') === 'true';
    // TODO(security): Never store passwords in localStorage. Only email is recalled.
    if (wasRemembered && savedEmail) {
      setFormData(prev => ({ ...prev, email: savedEmail, rememberMe: true }));
    }
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    if (!formData.email || !formData.password) {
      setError('Please fill in all fields');
      setIsLoading(false);
      return;
    }

    try {
      const response = await axios.post('/api/auth/login', {
        email: formData.email,
        password: formData.password
      });

      if (response.data && response.data.token) {
        // TODO(security): Migrate token storage from localStorage to HttpOnly cookies for production.
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify({
          email: response.data.email,
          firstName: response.data.firstName,
          lastName: response.data.lastName
        }));

        if (formData.rememberMe) {
          localStorage.setItem('rememberedEmail', formData.email);
          // NOTE: Password is intentionally NOT stored — security risk.
          localStorage.setItem('rememberMe', 'true');
        } else {
          localStorage.removeItem('rememberedEmail');
          localStorage.removeItem('rememberMe');
        }

        if (onLoginSuccess) onLoginSuccess();
      }
    } catch (err) {
      // Log minimal info — no credentials in logs
      const msg = err.response?.data?.message || err.response?.data || 'Login failed. Please check your credentials.';
      setError(typeof msg === 'string' ? msg : 'Login failed. Please check your credentials.');
      setIsLoading(false);
    }
  };

  // ── OAuth social login ───────────────────────────────────────────────────
  // Redirects to Spring Security OAuth2 endpoints which handle the full flow.
  // Backend must have spring-boot-starter-oauth2-client configured.
  const handleOAuth = (provider) => {
    const BACKEND = '';
    // The redirect_uri on the backend should send back a JWT via
    // a redirect to /oauth2/callback?token=... on the frontend.
    window.location.href = `${BACKEND}/oauth2/authorization/${provider}`;
  };

  return (
    <div className="auth-page">
      {/* ── Left decorative panel ── */}
      <div className="auth-panel-left">
        <div className="auth-brand">
          <div className="auth-brand-icon">
            <Zap size={22} />
          </div>
          <span className="auth-brand-name">BoostV</span>
          <span className="auth-brand-badge">Beta</span>
        </div>

        <div className="auth-hero-text">
          <h2>
            Land more<br />
            <span>interviews</span><br />
            with AI-power
          </h2>
          <p>
             analyses your resume against ATS systems,
            matches you to jobs, and coaches you — all in one place.
          </p>
        </div>

        <div className="auth-features-list">
          <div className="auth-feature-item">
            <div className="auth-feature-dot indigo">🎯</div>
            <span className="auth-feature-label">ATS Score & Keyword Analysis</span>
          </div>
          <div className="auth-feature-item">
            <div className="auth-feature-dot purple">🤖</div>
            <span className="auth-feature-label">RAG Career Coach (AI Chat)</span>
          </div>
          <div className="auth-feature-item">
            <div className="auth-feature-dot emerald">📄</div>
            <span className="auth-feature-label">Cover Letter Generator</span>
          </div>
          <div className="auth-feature-item">
            <div className="auth-feature-dot amber">🎤</div>
            <span className="auth-feature-label">Interview Q&A Coach</span>
          </div>
          <div className="auth-feature-item">
            <div className="auth-feature-dot rose">🗺️</div>
            <span className="auth-feature-label">90-Day Career Path Plans</span>
          </div>
        </div>
      </div>

      {/* ── Right form panel ── */}
      <div className="auth-panel-right">
        <button
          className="auth-back-btn"
          onClick={() => setCurrentView('landing')}
          type="button"
          id="login-back-btn"
        >
          <ArrowLeft size={16} />
          Back to Home
        </button>

        <div className="auth-card">
          {/* Mobile logo */}
          <div className="auth-logo-mobile">
            <div className="auth-logo-mobile-icon">
              <Zap size={20} />
            </div>
            <span className="auth-logo-mobile-name">ResumeAI</span>
          </div>

          <div className="auth-header">
            <h1 className="auth-title">Welcome back</h1>
            <p className="auth-subtitle">Sign in to your account to continue</p>
          </div>

          {error && (
            <div className="auth-error" role="alert" id="login-error-msg">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="auth-form" id="login-form" noValidate>
            <div className="form-group">
              <label htmlFor="login-email" className="form-label">Email</label>
              <div className="input-wrapper">
                <Mail className="input-icon" size={17} />
                <input
                  type="email"
                  id="login-email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="your.email@example.com"
                  className="form-input"
                  disabled={isLoading}
                  autoComplete="email"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="login-password" className="form-label">Password</label>
              <div className="input-wrapper">
                <Lock className="input-icon" size={17} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="login-password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Enter your password"
                  className="form-input"
                  disabled={isLoading}
                  autoComplete="current-password"
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  id="login-toggle-password"
                >
                  {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                </button>
              </div>
            </div>

            <div className="form-footer">
              <label className="checkbox-wrapper">
                <input
                  type="checkbox"
                  name="rememberMe"
                  checked={formData.rememberMe}
                  onChange={handleChange}
                  disabled={isLoading}
                  id="login-remember-me"
                />
                Remember me
              </label>
              <button
                type="button"
                className="forgot-link"
                id="login-forgot-password"
              >
                Forgot password?
              </button>
            </div>

            <button
              type="submit"
              className="auth-submit-btn"
              disabled={isLoading}
              id="login-submit-btn"
            >
              {isLoading ? (
                <>
                  <Loader2 size={18} className="spin" />
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          {/* ── Social login ── */}
          <div className="auth-divider">or continue with</div>
          <div className="social-login">
            <button
              type="button"
              className="social-btn"
              onClick={() => handleOAuth('google')}
              disabled={isLoading}
              id="login-google-btn"
            >
              <GoogleIcon />
              Google
            </button>
            <button
              type="button"
              className="social-btn"
              onClick={() => handleOAuth('github')}
              disabled={isLoading}
              id="login-github-btn"
            >
              <GitHubIcon />
              GitHub
            </button>
          </div>

          <div className="auth-switch">
            Don't have an account?{' '}
            <button
              onClick={() => setCurrentView('register')}
              id="login-go-register"
            >
              Create one
            </button>
          </div>
        </div>
      </div>

    </div>
  );
}