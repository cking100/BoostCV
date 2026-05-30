// src/pages/RegisterPage.jsx
import React, { useState } from 'react';
import { Mail, Lock, User, ArrowLeft, Eye, EyeOff, Loader2, Zap } from 'lucide-react';
import api from './api';
import './auth.css';

export default function RegisterPage({ setCurrentView, onRegisterSuccess }) {
  const [showPassword, setShowPassword]             = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading]                   = useState(false);
  const [error, setError]                           = useState('');
  const [formData, setFormData]                     = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreedToTerms: false,
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    if (name === 'name') {
      const parts     = value.trim().split(' ');
      const firstName = parts[0] || '';
      const lastName  = parts.slice(1).join(' ') || '';
      setFormData(prev => ({ ...prev, firstName, lastName, name: value }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: type === 'checkbox' ? checked : value,
      }));
    }
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.agreedToTerms) {
      setError('Please agree to the Terms & Conditions to continue.');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setError("Passwords don't match.");
      return;
    }

    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }

    setIsLoading(true);

    try {
      // api.register automatically saves token + user to localStorage
      await api.register(
        formData.email,
        formData.password,
        formData.firstName,
        formData.lastName
      );

      if (onRegisterSuccess) onRegisterSuccess();
      else setCurrentView('dashboard');
    } catch (err) {
      // Display user-facing error; no sensitive data logged
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      {/* ── Left decorative panel ── */}
      <div className="auth-panel-left">
        <div className="auth-brand">
          <div className="auth-brand-icon">
            <Zap size={22} />
          </div>
          <span className="auth-brand-name">ResumeAI</span>
          <span className="auth-brand-badge">Beta</span>
        </div>

        <div className="auth-hero-text">
          <h2>
            Your career,<br />
            <span>supercharged</span><br />
            by Gemini AI
          </h2>
          <p>
            Join thousands of job seekers using RAG-powered AI coaching,
            ATS analysis, and personalised career path planning.
          </p>
        </div>

        <div className="auth-features-list">
          <div className="auth-feature-item">
            <div className="auth-feature-dot indigo">🎯</div>
            <span className="auth-feature-label">Job Match Scoring</span>
          </div>
          <div className="auth-feature-item">
            <div className="auth-feature-dot purple">🤖</div>
            <span className="auth-feature-label">RAG Career Coach</span>
          </div>
          <div className="auth-feature-item">
            <div className="auth-feature-dot emerald">📄</div>
            <span className="auth-feature-label">AI Cover Letter Writer</span>
          </div>
          <div className="auth-feature-item">
            <div className="auth-feature-dot amber">🎤</div>
            <span className="auth-feature-label">Interview Coach</span>
          </div>
          <div className="auth-feature-item">
            <div className="auth-feature-dot rose">🗺️</div>
            <span className="auth-feature-label">Career Path Advisor</span>
          </div>
        </div>
      </div>

      {/* ── Right form panel ── */}
      <div className="auth-panel-right">
        <button
          className="auth-back-btn"
          onClick={() => setCurrentView('landing')}
          type="button"
          id="register-back-btn"
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
            <h1 className="auth-title">Create your account</h1>
            <p className="auth-subtitle">Start optimising your resume with AI today</p>
          </div>

          {error && (
            <div className="auth-error" role="alert" id="register-error-msg">
              {error}
            </div>
          )}

          <form
            onSubmit={handleSubmit}
            className="auth-form"
            id="register-form"
            noValidate
          >
            {/* Full Name */}
            <div className="form-group">
              <label htmlFor="register-name" className="form-label">Full Name</label>
              <div className="input-wrapper">
                <User className="input-icon" size={17} />
                <input
                  type="text"
                  id="register-name"
                  name="name"
                  value={formData.name || `${formData.firstName} ${formData.lastName}`.trim()}
                  onChange={handleChange}
                  placeholder="John Doe"
                  className="form-input"
                  required
                  disabled={isLoading}
                  autoComplete="name"
                />
              </div>
              <span className="form-hint">Enter your first and last name separated by a space</span>
            </div>

            {/* Email */}
            <div className="form-group">
              <label htmlFor="register-email" className="form-label">Email Address</label>
              <div className="input-wrapper">
                <Mail className="input-icon" size={17} />
                <input
                  type="email"
                  id="register-email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="you@example.com"
                  className="form-input"
                  required
                  disabled={isLoading}
                  autoComplete="email"
                />
              </div>
            </div>

            {/* Password */}
            <div className="form-group">
              <label htmlFor="register-password" className="form-label">Password</label>
              <div className="input-wrapper">
                <Lock className="input-icon" size={17} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="register-password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Minimum 8 characters"
                  className="form-input"
                  required
                  minLength={8}
                  disabled={isLoading}
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  id="register-toggle-password"
                >
                  {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                </button>
              </div>
            </div>

            {/* Confirm Password */}
            <div className="form-group">
              <label htmlFor="register-confirm-password" className="form-label">Confirm Password</label>
              <div className="input-wrapper">
                <Lock className="input-icon" size={17} />
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  id="register-confirm-password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  placeholder="Re-enter your password"
                  className="form-input"
                  required
                  minLength={8}
                  disabled={isLoading}
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                  id="register-toggle-confirm-password"
                >
                  {showConfirmPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                </button>
              </div>
            </div>

            {/* Terms */}
            <div className="form-footer" style={{ justifyContent: 'flex-start' }}>
              <label className="checkbox-wrapper">
                <input
                  type="checkbox"
                  name="agreedToTerms"
                  checked={formData.agreedToTerms}
                  onChange={handleChange}
                  disabled={isLoading}
                  id="register-terms-checkbox"
                />
                <span className="terms-text">
                  I agree to the{' '}
                  <a href="#terms" className="terms-link">Terms &amp; Conditions</a>
                </span>
              </label>
            </div>

            <button
              type="submit"
              className="auth-submit-btn"
              disabled={isLoading}
              id="register-submit-btn"
            >
              {isLoading ? (
                <>
                  <Loader2 size={18} className="spin" />
                  Creating account...
                </>
              ) : (
                'Create Account'
              )}
            </button>

            <div className="auth-divider">
              <span>or sign up with</span>
            </div>

            {/* Social — TODO(security): Implement OAuth provider flow server-side */}
            <div className="social-login">
              <button
                type="button"
                className="social-btn"
                disabled={isLoading}
                id="register-google-btn"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Google
              </button>
              <button
                type="button"
                className="social-btn"
                disabled={isLoading}
                id="register-github-btn"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="currentColor" d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.17 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.463-1.11-1.463-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.831.092-.646.35-1.086.636-1.336-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.137 20.167 22 16.418 22 12c0-5.523-4.477-10-10-10z"/>
                </svg>
                GitHub
              </button>
            </div>
          </form>

          <div className="auth-switch">
            Already have an account?{' '}
            <button
              onClick={() => setCurrentView('login')}
              disabled={isLoading}
              id="register-go-login"
            >
              Sign in
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}