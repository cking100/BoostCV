import React, { useState, useRef } from "react";
import { Upload, FileText, Sparkles, Shield, Loader2 } from "lucide-react";
import api from "./api"; // Import the API service
import "./LandingPage.css";

export default function LandingPage({ setCurrentView, setCurrentResume }) {
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState(null);
  const fileInputRef = useRef(null);

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
    
    // Validate file type
    const validTypes = [
      'application/pdf', 
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ];
    
    if (!validTypes.includes(file.type)) {
      setUploadError('Please upload a PDF or DOCX file');
      return;
    }

    // Validate file size (10MB max)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      setUploadError('File size must be less than 10MB');
      return;
    }

    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
      // Redirect to login/register
      alert('Please sign in to upload and analyze your resume');
      setCurrentView("login");
      return;
    }

    setIsUploading(true);

    try {
      console.log('Uploading file:', file.name);
      
      // Call the real backend API
      const response = await api.uploadResume(file);
      
      console.log('Upload successful:', response);
      
      // Store the resume data to pass to results page
      if (setCurrentResume) {
        setCurrentResume(response);
      }
      
      // Navigate to results page after successful upload
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
    <div className="landing-container">
      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept=".pdf,.docx"
        onChange={handleFileChange}
        style={{ display: 'none' }}
      />

      {/* HEADER */}
      <header className="landing-header">
        <div className="header-inner">
          <div className="logo">
            <div className="logo-icon">
              <Sparkles size={20} />
            </div>
            <span className="logo-title">CraftCV</span>
          </div>
          <nav className="nav">
            <button
              onClick={() => setCurrentView("login")}
              className="nav-btn"
            >
              Sign In
            </button>
            <button
              onClick={() => setCurrentView("register")}
              className="nav-btn-primary"
            >
              Get Started →
            </button>
          </nav>
        </div>
      </header>

      {/* HERO SECTION */}
      <div className="hero-section">
        {/* LEFT COLUMN */}
        <div className="hero-content">
          <div className="badge">
            <Sparkles size={14} />
            <span>AI-POWERED RESUME CHECKER</span>
          </div>
          
          <h1 className="hero-title">
            Is your resume <span className="highlight">good enough</span> to land interviews?
          </h1>
          
          <p className="hero-subtitle">
            Get instant AI feedback with 16 crucial checks. Ensure your resume passes ATS systems and catches recruiters' attention.
          </p>

          {/* UPLOAD BOX */}
          <div
            className={`upload-box ${isDragging ? "drag-active" : ""} ${isUploading ? "uploading" : ""}`}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={handleUploadClick}
            style={{ cursor: isUploading ? 'not-allowed' : 'pointer' }}
          >
            {!isUploading ? (
              <>
                <div className="upload-icon-wrapper">
                  <Upload className="upload-icon" />
                </div>
                
                <p className="upload-title">Drop your resume here</p>
                <p className="upload-hint">or click to browse files</p>
                
                {uploadError && (
                  <div className="upload-error">
                    ⚠️ {uploadError}
                  </div>
                )}
                
                <button
                  onClick={handleUploadClick}
                  className="upload-btn"
                  type="button"
                >
                  <Upload size={18} />
                  Upload Your Resume
                </button>
                
                <div className="upload-meta">
                  <span className="file-types">PDF & DOCX • Max 10MB</span>
                  <span className="privacy-badge">
                    <Shield size={14} />
                    Privacy guaranteed
                  </span>
                </div>
              </>
            ) : (
              <div className="upload-loading">
                <Loader2 className="spinner" size={48} />
                <p className="upload-title">Analyzing your resume...</p>
                <p className="upload-hint">Extracting text and checking ATS compatibility</p>
              </div>
            )}
          </div>
        </div>

        {/* RIGHT COLUMN */}
        <div className="hero-preview">
          <div className="preview-card">
            <div className="preview-header">
              <div className="preview-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <span className="preview-title">Resume Analysis</span>
            </div>
            
            <div className="preview-content">
              <div className="scan-animation">
                <div className="scan-line"></div>
              </div>
              
              <div className="preview-checks">
                <div className="check-item">
                  <div className="check-icon success"></div>
                  <span>ATS Compatibility</span>
                </div>
                <div className="check-item">
                  <div className="check-icon success"></div>
                  <span>Contact Information</span>
                </div>
                <div className="check-item">
                  <div className="check-icon warning"></div>
                  <span>Keywords Optimization</span>
                </div>
                <div className="check-item">
                  <div className="check-icon success"></div>
                  <span>Formatting Consistency</span>
                </div>
              </div>

              <div className="score-display">
                <div className="score-circle">
                  <span className="score-number">87</span>
                  <span className="score-label">/100</span>
                </div>
              </div>
            </div>
          </div>

          {/* Floating elements */}
          <div className="floating-element element-1">
            <FileText size={24} />
          </div>
          <div className="floating-element element-2">
            <Sparkles size={20} />
          </div>
        </div>
      </div>

      {/* Background decorative elements */}
      <div className="bg-blob blob-1"></div>
      <div className="bg-blob blob-2"></div>
      <div className="bg-blob blob-3"></div>
    </div>
  );
}