import React, { useState, useEffect } from 'react';
import { Mail, Lock, Eye, EyeOff, Zap, ArrowLeft } from 'lucide-react';
import axios from 'axios';

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
    const savedEmail = localStorage.getItem('rememberedEmail');
    const savedPassword = localStorage.getItem('rememberedPassword');
    const wasRemembered = localStorage.getItem('rememberMe') === 'true';
    
    if(wasRemembered && savedEmail){
      setFormData({
        email: savedEmail,
        password: savedPassword || '',
        rememberMe: true
      });
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

    if(!formData.email || !formData.password){
      setError('Please fill in all fields');
      setIsLoading(false);
      return;
    }

    try{
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        email: formData.email,
        password: formData.password
      });

      if(response.data && response.data.token){
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify({
          email: response.data.email,
          firstName: response.data.firstName,
          lastName: response.data.lastName
        }));

        if(formData.rememberMe){
          localStorage.setItem('rememberedEmail', formData.email);
          localStorage.setItem('rememberedPassword', formData.password);
          localStorage.setItem('rememberMe', 'true');
        }else{
          localStorage.removeItem('rememberedEmail');
          localStorage.removeItem('rememberedPassword');
          localStorage.removeItem('rememberMe');
        }

        console.log('Login successful, calling onLoginSuccess');
        if(onLoginSuccess) onLoginSuccess();
      }
    }catch(err){
      console.error('Login error:', err);
      setError(err.response?.data?.message || err.response?.data || 'Login failed. Please check your credentials.');
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <button 
        className="back-to-home"
        onClick={() => setCurrentView('landing')}>
        <ArrowLeft size={20}/>
        <span>Back to Home</span>
      </button>

      <div className="auth-box">
        <div className="auth-logo">
          <div className="auth-logo-icon">
            <Zap size={32}/>
          </div>
          <h1>BoostCV</h1>
        </div>

        <div className="auth-header">
          <h2>Welcome Back</h2>
          <p>Sign in to continue analyzing your resumes</p>
        </div>

        {error && (
          <div className="auth-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Email</label>
            <div className="input-with-icon">
              <Mail size={20}/>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="your.email@example.com"
                disabled={isLoading}
                autoComplete="email"
              />
            </div>
          </div>

          <div className="form-group">
            <label>Password</label>
            <div className="input-with-icon">
              <Lock size={20}/>
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Enter your password"
                disabled={isLoading}
                autoComplete="current-password"
              />
              <button
                type="button"
                className="toggle-password"
                onClick={() => setShowPassword(!showPassword)}>
                {showPassword ? <EyeOff size={20}/> : <Eye size={20}/>}
              </button>
            </div>
          </div>

          <div className="form-options">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="rememberMe"
                checked={formData.rememberMe}
                onChange={handleChange}
                disabled={isLoading}
              />
              <span>Remember me</span>
            </label>
            <button type="button" className="forgot-password">
              Forgot Password?
            </button>
          </div>

          <button 
            type="submit" 
            className="auth-submit-btn"
            disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Don't have an account?{' '}
            <button onClick={() => setCurrentView('register')}>
              Sign Up
            </button>
          </p>
        </div>
      </div>

      <style jsx>{`
        .auth-container {
          min-height: 100vh;
          background: #000000;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 20px;
          position: relative;
        }

        .back-to-home {
          position: absolute;
          top: 24px;
          left: 24px;
          display: flex;
          align-items: center;
          gap: 8px;
          background: transparent;
          border: 1px solid #262626;
          color: #ffffff;
          padding: 10px 16px;
          border-radius: 8px;
          cursor: pointer;
          font-size: 14px;
          transition: all 0.2s;
        }

        .back-to-home:hover {
          background: #111111;
          border-color: #10b981;
        }

        .auth-box {
          width: 100%;
          max-width: 420px;
          background: #0a0a0a;
          border: 1px solid #262626;
          border-radius: 16px;
          padding: 40px;
        }

        .auth-logo {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 32px;
          justify-content: center;
        }

        .auth-logo-icon {
          width: 48px;
          height: 48px;
          background: #10b981;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
        }

        .auth-logo h1 {
          font-size: 28px;
          font-weight: 800;
          color: #ffffff;
        }

        .auth-header {
          text-align: center;
          margin-bottom: 32px;
        }

        .auth-header h2 {
          font-size: 24px;
          font-weight: 700;
          color: #ffffff;
          margin-bottom: 8px;
        }

        .auth-header p {
          color: #a3a3a3;
          font-size: 14px;
        }

        .auth-error {
          background: rgba(239, 68, 68, 0.15);
          border: 1px solid rgba(239, 68, 68, 0.4);
          color: #ff6b6b;
          padding: 12px 16px;
          border-radius: 8px;
          margin-bottom: 24px;
          font-size: 14px;
        }

        .auth-form {
          display: flex;
          flex-direction: column;
          gap: 20px;
        }

        .form-group {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .form-group label {
          font-size: 14px;
          font-weight: 600;
          color: #ffffff;
        }

        .input-with-icon {
          position: relative;
          display: flex;
          align-items: center;
        }

        .input-with-icon svg {
          position: absolute;
          left: 14px;
          color: #737373;
        }

        .input-with-icon input {
          width: 100%;
          padding: 12px 14px 12px 44px;
          background: #111111;
          border: 1px solid #262626;
          border-radius: 8px;
          color: #ffffff;
          font-size: 14px;
          transition: all 0.2s;
        }

        .input-with-icon input:focus {
          outline: none;
          border-color: #10b981;
          background: #0a0a0a;
        }

        .input-with-icon input:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .toggle-password {
          position: absolute;
          right: 14px;
          background: none;
          border: none;
          color: #737373;
          cursor: pointer;
          padding: 4px;
          display: flex;
        }

        .toggle-password:hover {
          color: #ffffff;
        }

        .form-options {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .checkbox-label {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: #a3a3a3;
          cursor: pointer;
        }

        .checkbox-label input {
          cursor: pointer;
        }

        .forgot-password {
          background: none;
          border: none;
          color: #10b981;
          font-size: 14px;
          cursor: pointer;
          font-weight: 500;
        }

        .forgot-password:hover {
          text-decoration: underline;
        }

        .auth-submit-btn {
          width: 100%;
          padding: 14px;
          background: #10b981;
          border: none;
          border-radius: 8px;
          color: white;
          font-size: 15px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;
        }

        .auth-submit-btn:hover:not(:disabled) {
          background: #059669;
          transform: translateY(-1px);
        }

        .auth-submit-btn:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .auth-footer {
          margin-top: 24px;
          text-align: center;
        }

        .auth-footer p {
          color: #a3a3a3;
          font-size: 14px;
        }

        .auth-footer button {
          background: none;
          border: none;
          color: #10b981;
          font-weight: 600;
          cursor: pointer;
          font-size: 14px;
        }

        .auth-footer button:hover {
          text-decoration: underline;
        }

        @media (max-width: 480px) {
          .auth-box {
            padding: 24px;
          }

          .back-to-home {
            top: 16px;
            left: 16px;
          }
        }
      `}</style>
    </div>
  );
}