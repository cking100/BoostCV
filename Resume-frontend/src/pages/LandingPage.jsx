import React, { useState, useRef, useEffect } from "react";
import { 
  Upload, FileText, Sparkles, Shield, Loader2, 
  Check, Zap, AlertCircle, ArrowRight, Moon, Sun,
  Star, Clock, TrendingUp, CheckCircle, X
} from "lucide-react";
import api from "./api";
import "./LandingPage.css";

export default function LandingPage({ setCurrentView, setCurrentResume }) {
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState(null);
  const [darkMode, setDarkMode] = useState(false);
  const [scoreCount, setScoreCount] = useState(0);
  const fileInputRef = useRef(null);

  // Animate score
  useEffect(() => {
    const timer = setInterval(() => {
      setScoreCount(prev => {
        if (prev >= 87) {
          clearInterval(timer);
          return 87;
        }
        return prev + 2;
      });
    }, 30);
    return () => clearInterval(timer);
  }, []);

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    
    const files = e.dataTransfer.files;
    if (files.length > 0) {
      handleFileUpload(files[0]);
    }
  };

  const handleFileUpload = async (file) => {
    setUploadError(null);
    
    const validTypes = [
      'application/pdf', 
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ];
    
    if (!validTypes.includes(file.type)) {
      setUploadError('Please upload a PDF or DOCX file');
      return;
    }

    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      setUploadError('File size must be less than 10MB');
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      alert('Please sign in to upload and analyze your resume');
      setCurrentView("login");
      return;
    }

    setIsUploading(true);

    try {
      console.log('Uploading file:', file.name);
      const response = await api.uploadResume(file);
      console.log('Upload successful:', response);
      
      if (setCurrentResume) {
        setCurrentResume(response);
      }
      
      setCurrentView("results");
      
    } catch (error) {
      console.error('Upload failed:', error);
      setUploadError(error.message || 'Upload failed. Please try again.');
      setIsUploading(false);
    }
  };

  const handleUploadClick = (e) => {
    if (e) e.stopPropagation();
    if (!isUploading) {
      setUploadError(null);
      fileInputRef.current?.click();
    }
  };

  const handleFileChange = (e) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      handleFileUpload(files[0]);
    }
  };

  return (
    <div className={`landing-page ${darkMode ? 'dark' : ''}`}>
      <input
        ref={fileInputRef}
        type="file"
        accept=".pdf,.docx"
        onChange={handleFileChange}
        style={{ display: 'none' }}
      />

      {/* Navbar */}
      <nav className="navbar">
        <div className="nav-container">
          <div className="logo-section">
            <div className="logo-box">
              <Zap size={24} />
            </div>
            <span className="logo-text">BoostCV</span>
            <span className="version-badge">Beta</span>
          </div>
          
          <div className="nav-actions">
            <button className="theme-btn" onClick={toggleDarkMode}>
              {darkMode ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <button onClick={() => setCurrentView("login")} className="signin-btn">
              Sign In
            </button>
            <button onClick={() => setCurrentView("register")} className="cta-btn">
              Get Started
            </button>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <main className="hero">
        <div className="container">
          {/* Top badge */}
          <div className="announcement">
            <Sparkles size={16} />
            <span>AI-powered resume analysis • Free for students</span>
          </div>

          {/* Main heading */}
          <h1 className="heading">
            Stop getting <span className="highlight-text">rejected</span> by ATS systems
          </h1>
          
          <p className="subheading">
            Upload your resume and get instant feedback on what's blocking you from landing interviews. 
            Built by students, for students.
          </p>



          {/* Main content grid */}
          <div className="content-grid">
            {/* Left: Upload */}
            <div className="upload-section">
              <div
                className={`dropzone ${isDragging ? 'dragging' : ''} ${isUploading ? 'uploading' : ''}`}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onClick={!isUploading ? handleUploadClick : undefined}
              >
                {!isUploading ? (
                  <>
                    <div className="dropzone-icon">
                      <Upload size={32} />
                    </div>
                    <h3 className="dropzone-title">Drop your resume here</h3>
                    <p className="dropzone-subtitle">or click to browse files</p>
                    
                    {uploadError && (
                      <div className="error-box">
                        <AlertCircle size={16} />
                        <span>{uploadError}</span>
                        <button onClick={(e) => { e.stopPropagation(); setUploadError(null); }}>
                          <X size={14} />
                        </button>
                      </div>
                    )}

                    <button className="upload-btn" onClick={handleUploadClick}>
                      <Upload size={18} />
                      Analyze Resume
                    </button>

                    <div className="file-info">
                      <span>PDF or DOCX • Max 10MB</span>
                      <div className="privacy">
                        <Shield size={14} />
                        <span>Private & secure</span>
                      </div>
                    </div>
                  </>
                ) : (
                  <div className="loading-state">
                    <Loader2 className="spinner" size={40} />
                    <h3>Analyzing your resume...</h3>
                    <p>Checking ATS compatibility</p>
                    <div className="progress-bar">
                      <div className="progress-fill"></div>
                    </div>
                  </div>
                )}
              </div>

              {/* Features list */}
              <div className="features-list">
                <div className="feature-item">
                  <CheckCircle size={18} />
                  <span>ATS compatibility check</span>
                </div>
                <div className="feature-item">
                  <CheckCircle size={18} />
                  <span>Keyword optimization</span>
                </div>
                <div className="feature-item">
                  <CheckCircle size={18} />
                  <span>Format validation</span>
                </div>
                <div className="feature-item">
                  <CheckCircle size={18} />
                  <span>Industry benchmarking</span>
                </div>
              </div>
            </div>

            {/* Right: Preview */}
            <div className="preview-section">
              <div className="preview-card">
                <div className="card-top">
                  <div className="dots">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <span className="card-label">Resume Analysis</span>
                </div>

                <div className="score-container">
                  <div className="score-ring">
                    <svg viewBox="0 0 100 100">
                      <circle cx="50" cy="50" r="45" className="ring-bg" />
                      <circle 
                        cx="50" 
                        cy="50" 
                        r="45" 
                        className="ring-progress"
                        style={{
                          strokeDasharray: `${(scoreCount / 100) * 283} 283`
                        }}
                      />
                    </svg>
                    <div className="score-text">
                      <div className="score-num">{scoreCount}</div>
                      <div className="score-label">Score</div>
                    </div>
                  </div>
                </div>

                <div className="checks-list">
                  <div className="check-row success">
                    <Check size={16} />
                    <span>ATS Compatible</span>
                  </div>
                  <div className="check-row success">
                    <Check size={16} />
                    <span>Contact Info</span>
                  </div>
                  <div className="check-row warning">
                    <AlertCircle size={16} />
                    <span>Keywords</span>
                  </div>
                  <div className="check-row success">
                    <Check size={16} />
                    <span>Formatting</span>
                  </div>
                </div>

                <div className="insights">
                  <div className="insight">
                    <div className="insight-icon">💡</div>
                    <span>Add 3 more technical skills</span>
                  </div>
                  <div className="insight">
                    <div className="insight-icon">🎯</div>
                    <span>Strong action verbs used</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Features section */}
      <section className="features">
        <div className="container">
          <h2 className="section-title">Why BoostCV?</h2>
          <div className="features-grid">
            <div className="feature-box">
              <div className="feature-icon green">
                <Zap size={20} />
              </div>
              <h3>Lightning Fast</h3>
              <p>Get results in under 3 seconds. No waiting around.</p>
            </div>

            <div className="feature-box">
              <div className="feature-icon blue">
                <Shield size={20} />
              </div>
              <h3>Privacy First</h3>
              <p>Your resume is analyzed securely. We don't store anything.</p>
            </div>

            <div className="feature-box">
              <div className="feature-icon purple">
                <FileText size={20} />
              </div>
              <h3>ATS Optimized</h3>
              <p>Beat the robots. We check what recruiters' systems look for.</p>
            </div>

            <div className="feature-box">
              <div className="feature-icon orange">
                <TrendingUp size={20} />
              </div>
              <h3>Actionable Tips</h3>
              <p>Get specific improvements, not vague suggestions.</p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="cta">
        <div className="cta-box">
          <h2>Ready to improve your resume?</h2>
          <p>Get instant AI-powered feedback on your resume</p>
          <button className="cta-large" onClick={handleUploadClick}>
            Analyze My Resume
            <ArrowRight size={20} />
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <p>Made by students, for students 🎓</p>
          <p className="footer-note">BoostCV • Built with React & Spring Boot</p>
        </div>
      </footer>
    </div>
  );
}