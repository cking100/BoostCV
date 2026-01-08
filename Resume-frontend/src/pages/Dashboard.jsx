import React, { useState, useRef, useEffect } from "react";
import {
  Upload,
  FileText,
  TrendingUp,
  Clock,
  Download,
  Eye,
  Trash2,
  LogOut,
  Settings,
  Sparkles,
  BarChart3,
  Loader2,
} from "lucide-react";
import api from "./api";
import "./Dashboard.css";

export default function Dashboard({ setCurrentView, setCurrentResume }) {
  const [resumes, setResumes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  // Fetch resumes on component mount
  useEffect(() => {
    fetchResumes();
  }, []);

  const fetchResumes = async () => {
    try {
      setIsLoading(true);
      const data = await api.getResumes();
      setResumes(data);
      setError(null);
    } catch (err) {
      console.error('Error fetching resumes:', err);
      setError('Failed to load resumes');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    api.logout();
    setCurrentView("landing");
  };

  const handleFileUpload = async (file) => {
    // Validate file type
    const validTypes = [
      'application/pdf',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ];
    
    if (!validTypes.includes(file.type)) {
      alert('Please upload a PDF or DOCX file');
      return;
    }

    // Validate file size (10MB max)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      alert('File size must be less than 10MB');
      return;
    }

    setIsUploading(true);

    try {
      console.log('Uploading file:', file.name);
      
      // Call the real backend API
      const response = await api.uploadResume(file);
      
      console.log('Upload successful:', response);
      
      // Refresh the resumes list
      await fetchResumes();
      
      // Store the resume data to pass to results page
      if (setCurrentResume) {
        setCurrentResume(response);
      }
      
      // Navigate to results page
      setCurrentView("results");
      
    } catch (error) {
      console.error('Upload failed:', error);
      alert(error.message || 'Upload failed. Please try again.');
      setIsUploading(false);
    }
  };

  const handleUploadClick = () => {
    if (!isUploading) {
      fileInputRef.current?.click();
    }
  };

  const handleFileChange = (e) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      handleFileUpload(files[0]);
    }
  };

  const handleViewResume = (resume) => {
    if (setCurrentResume) {
      setCurrentResume(resume);
    }
    setCurrentView("results");
  };

  const handleDeleteResume = async (resumeId) => {
    if (!window.confirm('Are you sure you want to delete this resume?')) {
      return;
    }

    try {
      await api.deleteResume(resumeId);
      // Refresh the list
      await fetchResumes();
    } catch (error) {
      console.error('Delete failed:', error);
      alert('Failed to delete resume');
    }
  };

  const calculateAverageScore = () => {
    if (resumes.length === 0) return 0;
    const sum = resumes.reduce((acc, r) => acc + (r.atsScore || 0), 0);
    return Math.round(sum / resumes.length);
  };

  return (
    <div className="dashboard-container">
      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept=".pdf,.docx"
        onChange={handleFileChange}
        style={{ display: 'none' }}
      />

      {/* Loading Overlay */}
      {isUploading && (
        <div className="upload-overlay">
          <div className="upload-overlay-content">
            <Loader2 className="spinner-large" size={64} />
            <h3>Analyzing your resume...</h3>
            <p>Extracting text and checking ATS compatibility</p>
          </div>
        </div>
      )}

      {/* Sidebar */}
      <aside className="dashboard-sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <div className="sidebar-logo-icon">
              <Sparkles size={20} />
            </div>
            <span className="sidebar-logo-title">CraftCV</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <button className="sidebar-nav-item active">
            <FileText size={20} />
            <span>My Resumes</span>
          </button>
          <button className="sidebar-nav-item">
            <BarChart3 size={20} />
            <span>Analytics</span>
          </button>
          <button className="sidebar-nav-item">
            <Settings size={20} />
            <span>Settings</span>
          </button>
        </nav>

        <button onClick={handleLogout} className="sidebar-logout">
          <LogOut size={20} />
          <span>Logout</span>
        </button>
      </aside>

      {/* Main Content */}
      <main className="dashboard-main">
        {/* Header */}
        <header className="dashboard-header">
          <div className="header-content">
            <h1 className="dashboard-title">My Resumes</h1>
            <p className="dashboard-subtitle">
              Manage and analyze your resume collection
            </p>
          </div>
          <button
            onClick={handleUploadClick}
            className="upload-new-btn"
            disabled={isUploading}
          >
            {isUploading ? (
              <>
                <Loader2 className="spinner-small" size={20} />
                Uploading...
              </>
            ) : (
              <>
                <Upload size={20} />
                Upload New Resume
              </>
            )}
          </button>
        </header>

        {/* Stats Cards */}
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon" style={{ background: "rgba(16, 185, 129, 0.2)" }}>
              <FileText size={24} style={{ color: "#10b981" }} />
            </div>
            <div className="stat-content">
              <p className="stat-label">Total Resumes</p>
              <p className="stat-value">{resumes.length}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon" style={{ background: "rgba(59, 130, 246, 0.2)" }}>
              <TrendingUp size={24} style={{ color: "#3b82f6" }} />
            </div>
            <div className="stat-content">
              <p className="stat-label">Average Score</p>
              <p className="stat-value">{calculateAverageScore()}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon" style={{ background: "rgba(168, 85, 247, 0.2)" }}>
              <Clock size={24} style={{ color: "#a855f7" }} />
            </div>
            <div className="stat-content">
              <p className="stat-label">Analyzed This Month</p>
              <p className="stat-value">{resumes.length}</p>
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="error-message">
            <p>{error}</p>
            <button onClick={fetchResumes}>Retry</button>
          </div>
        )}

        {/* Resumes List */}
        <div className="resumes-section">
          <div className="section-header">
            <h2 className="section-title">Recent Resumes</h2>
            <div className="section-filters">
              <button className="filter-btn active">All</button>
              <button className="filter-btn">High Score</button>
              <button className="filter-btn">Needs Work</button>
            </div>
          </div>

          {isLoading ? (
            <div className="loading-state">
              <Loader2 className="spinner" size={48} />
              <p>Loading your resumes...</p>
            </div>
          ) : resumes.length === 0 ? (
            <div className="empty-state">
              <FileText size={64} />
              <h3>No resumes yet</h3>
              <p>Upload your first resume to get started with AI-powered analysis</p>
              <button onClick={handleUploadClick} className="empty-state-btn">
                <Upload size={20} />
                Upload Resume
              </button>
            </div>
          ) : (
            <div className="resumes-list">
              {resumes.map((resume) => (
                <div key={resume.id} className="resume-card">
                  <div className="resume-icon">
                    <FileText size={32} />
                  </div>

                  <div className="resume-info">
                    <h3 className="resume-name">{resume.fileName}</h3>
                    <p className="resume-date">
                      Uploaded on {new Date(resume.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="resume-score">
                    <div
                      className={`score-badge ${
                        resume.atsScore >= 80
                          ? "score-high"
                          : resume.atsScore >= 60
                          ? "score-medium"
                          : "score-low"
                      }`}
                    >
                      <span className="score-number">{resume.atsScore || 0}</span>
                      <span className="score-label">/100</span>
                    </div>
                  </div>

                  <div className="resume-actions">
                    <button
                      onClick={() => handleViewResume(resume)}
                      className="action-btn primary"
                      title="View Analysis"
                    >
                      <Eye size={18} />
                    </button>
                    <button className="action-btn" title="Download">
                      <Download size={18} />
                    </button>
                    <button 
                      onClick={() => handleDeleteResume(resume.id)}
                      className="action-btn danger" 
                      title="Delete"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Quick Upload Section */}
        {!isLoading && resumes.length > 0 && (
          <div className="quick-upload-section">
            <div className="quick-upload-card">
              <Sparkles className="quick-upload-icon" size={48} />
              <h3 className="quick-upload-title">Ready to analyze another resume?</h3>
              <p className="quick-upload-text">
                Upload your resume and get instant AI-powered feedback
              </p>
              <button
                onClick={handleUploadClick}
                className="quick-upload-btn"
                disabled={isUploading}
              >
                {isUploading ? (
                  <>
                    <Loader2 className="spinner-small" size={20} />
                    Uploading...
                  </>
                ) : (
                  <>
                    <Upload size={20} />
                    Upload Resume
                  </>
                )}
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}