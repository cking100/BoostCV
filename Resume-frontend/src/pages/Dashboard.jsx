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
  Zap
} from "lucide-react";
import api from "./api";
import "./Dashboard.css";

export default function Dashboard({ setCurrentView, setCurrentResume }) {
  const [resumes, setResumes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [sortBy, setSortBy] = useState("newest");
  const [toast, setToast] = useState(null);
  const [isDraggingOver, setIsDraggingOver] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => {
    fetchResumes();
  }, []);

  useEffect(() => {
    if(toast){
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const showToast = (message, type = 'success') => {
    setToast({message, type});
  };

  const handleDragOverDashboard = (e) => {
    e.preventDefault();
    setIsDraggingOver(true);
  };

  const handleDragLeaveDashboard = () => {
    setIsDraggingOver(false);
  };

  const handleDropDashboard = (e) => {
    e.preventDefault();
    setIsDraggingOver(false);
    const files = e.dataTransfer.files;
    if(files.length > 0) handleFileUpload(files[0]);
  };

  const fetchResumes = async () => {
    try{
      setIsLoading(true);
      const data = await api.getResumes();
      setResumes(data);
      setError(null);
    }catch(err){
      console.error('Error fetching resumes:', err);
      setError('Failed to load resumes');
      showToast('Failed to load resumes', 'error');
    }finally{
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    api.logout();
    setCurrentView("landing");
  };

  const handleFileUpload = async (file) => {
    const validTypes = ['application/pdf',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    
    if(!validTypes.includes(file.type)){
      showToast('Please upload a PDF or DOCX file', 'error');
      return;
    }

    if(file.size > 10 * 1024 * 1024){
      showToast('File size must be less than 10MB', 'error');
      return;
    }

    setIsUploading(true);

    try{
      console.log('Uploading file:', file.name);
      const response = await api.uploadResume(file);
      console.log('Upload successful:', response);
      
      await fetchResumes();
      showToast('Resume uploaded successfully!', 'success');
      
      if(setCurrentResume) setCurrentResume(response);
      setCurrentView("results");
      
    }catch(error){
      console.error('Upload failed:', error);
      showToast(error.message || 'Upload failed. Please try again.', 'error');
      setIsUploading(false);
    }
  };

  const handleUploadClick = () => {
    if(!isUploading) fileInputRef.current?.click();
  };

  const handleFileChange = (e) => {
    const files = e.target.files;
    if(files && files.length > 0) handleFileUpload(files[0]);
  };

  const handleViewResume = (resume) => {
    if(setCurrentResume) setCurrentResume(resume);
    setCurrentView("results");
  };

  const handleDeleteResume = async (resumeId) => {
    if(!window.confirm('Are you sure you want to delete this resume?')) return;

    try{
      await api.deleteResume(resumeId);
      await fetchResumes();
      showToast('Resume deleted successfully', 'success');
    }catch(error){
      console.error('Delete failed:', error);
      showToast('Failed to delete resume', 'error');
    }
  };

  const calculateAverageScore = () => {
    if(resumes.length === 0) return 0;
    const sum = resumes.reduce((acc, r) => acc + (r.atsScore || 0), 0);
    return Math.round(sum / resumes.length);
  };

  const getFilteredResumes = () => {
    let filtered = resumes.filter(resume => 
      resume.fileName.toLowerCase().includes(searchQuery.toLowerCase())
    );

    switch(sortBy){
      case 'newest':
        return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      case 'oldest':
        return filtered.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
      case 'highest':
        return filtered.sort((a, b) => (b.atsScore || 0) - (a.atsScore || 0));
      case 'lowest':
        return filtered.sort((a, b) => (a.atsScore || 0) - (b.atsScore || 0));
      case 'name':
        return filtered.sort((a, b) => a.fileName.localeCompare(b.fileName));
      default:
        return filtered;
    }
  };

  const filteredResumes = getFilteredResumes();

  return (
    <div 
      className="dashboard-container"
      onDragOver={handleDragOverDashboard}
      onDragLeave={handleDragLeaveDashboard}
      onDrop={handleDropDashboard}>
      <input
        ref={fileInputRef}
        type="file"
        accept=".pdf,.docx"
        onChange={handleFileChange}
        style={{display: 'none'}}
      />

      {toast && (
        <div className={`toast ${toast.type}`}>
          <div className="toast-content">
            {toast.type === 'success' ? '✓' : '✕'}
            <span>{toast.message}</span>
          </div>
        </div>
      )}

      {isDraggingOver && (
        <div className="drag-overlay">
          <div className="drag-content">
            <Upload size={64}/>
            <h3>Drop your resume here</h3>
            <p>PDF or DOCX files only</p>
          </div>
        </div>
      )}

      {isUploading && (
        <div className="upload-overlay">
          <div className="overlay-content">
            <Loader2 className="spinner-large" size={64}/>
            <h3>Analyzing your resume...</h3>
            <p>This might take a few seconds</p>
          </div>
        </div>
      )}

      <aside className="sidebar">
        <div className="sidebar-top">
          <div className="logo">
            <div className="logo-icon">
              <Zap size={22}/>
            </div>
            <span className="logo-name">BoostCV</span>
          </div>
        </div>

        <nav className="sidebar-menu">
          <button className="menu-item active">
            <FileText size={20}/>
            <span>My Resumes</span>
          </button>
          <button className="menu-item">
            <BarChart3 size={20}/>
            <span>Analytics</span>
          </button>
          <button className="menu-item">
            <Settings size={20}/>
            <span>Settings</span>
          </button>
        </nav>

        <button onClick={handleLogout} className="logout-btn">
          <LogOut size={20}/>
          <span>Logout</span>
        </button>
      </aside>

      <main className="main-content">
        <header className="page-header">
          <div className="header-left">
            <h1 className="page-title">My Resumes</h1>
            <p className="page-subtitle">
              Manage and analyze your resume collection
            </p>
          </div>
          <button
            onClick={handleUploadClick}
            className="upload-btn-header"
            disabled={isUploading}>
            {isUploading ? (
              <>
                <Loader2 className="btn-spinner" size={20}/>
                Uploading...
              </>
            ) : (
              <>
                <Upload size={20}/>
                Upload New
              </>
            )}
          </button>
        </header>

        <div className="stats-row">
          <div className="stat-box">
            <div className="stat-icon green">
              <FileText size={24}/>
            </div>
            <div className="stat-info">
              <p className="stat-label">Total Resumes</p>
              <p className="stat-number">{resumes.length}</p>
            </div>
          </div>

          <div className="stat-box">
            <div className="stat-icon blue">
              <TrendingUp size={24}/>
            </div>
            <div className="stat-info">
              <p className="stat-label">Average Score</p>
              <p className="stat-number">{calculateAverageScore()}</p>
            </div>
          </div>

          <div className="stat-box">
            <div className="stat-icon purple">
              <Clock size={24}/>
            </div>
            <div className="stat-info">
              <p className="stat-label">This Month</p>
              <p className="stat-number">{resumes.length}</p>
            </div>
          </div>
        </div>

        {error && (
          <div className="error-banner">
            <p>{error}</p>
            <button onClick={fetchResumes}>Retry</button>
          </div>
        )}

        <div className="resumes-container">
          <div className="resumes-header">
            <h2 className="section-heading">Recent Resumes</h2>
            <div className="header-controls">
              <input
                type="text"
                placeholder="Search resumes..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="search-input"
              />
              <select 
                value={sortBy} 
                onChange={(e) => setSortBy(e.target.value)}
                className="sort-select">
                <option value="newest">Newest First</option>
                <option value="oldest">Oldest First</option>
                <option value="highest">Highest Score</option>
                <option value="lowest">Lowest Score</option>
                <option value="name">Name A-Z</option>
              </select>
            </div>
          </div>

          <div className="filter-tabs">
            <button className="tab active">All</button>
            <button className="tab">High Score</button>
            <button className="tab">Needs Work</button>
          </div>

          {isLoading ? (
            <div className="loading-box">
              <div className="skeleton-grid">
                {[1,2,3].map(i => (
                  <div key={i} className="skeleton-card">
                    <div className="skeleton-icon"></div>
                    <div className="skeleton-text">
                      <div className="skeleton-line"></div>
                      <div className="skeleton-line short"></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ) : filteredResumes.length === 0 ? (
            <div className="empty-box">
              {searchQuery ? (
                <>
                  <FileText size={64}/>
                  <h3>No resumes found</h3>
                  <p>Try adjusting your search query</p>
                  <button onClick={() => setSearchQuery("")} className="empty-btn">
                    Clear Search
                  </button>
                </>
              ) : (
                <>
                  <FileText size={64}/>
                  <h3>No resumes yet</h3>
                  <p>Upload your first resume to get started</p>
                  <button onClick={handleUploadClick} className="empty-btn">
                    <Upload size={20}/>
                    Upload Resume
                  </button>
                </>
              )}
            </div>
          ) : (
            <div className="resume-grid">
              {filteredResumes.map((resume) => (
                <div key={resume.id} className="resume-item">
                  <div className="resume-left">
                    <div className="file-icon">
                      <FileText size={28}/>
                    </div>

                    <div className="file-details">
                      <h3 className="file-name">{resume.fileName}</h3>
                      <p className="file-date">
                        {new Date(resume.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>

                  <div className="resume-right">
                    <div className={`score-pill ${
                        resume.atsScore >= 80 ? "high" :
                        resume.atsScore >= 60 ? "medium" : "low"
                      }`}>
                      <span className="score-val">{resume.atsScore || 0}</span>
                      <span className="score-max">/100</span>
                    </div>

                    <div className="action-btns">
                      <button
                        onClick={() => handleViewResume(resume)}
                        className="icon-btn primary"
                        title="View">
                        <Eye size={18}/>
                      </button>
                      <button className="icon-btn" title="Download">
                        <Download size={18}/>
                      </button>
                      <button 
                        onClick={() => handleDeleteResume(resume.id)}
                        className="icon-btn danger" 
                        title="Delete">
                        <Trash2 size={18}/>
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {!isLoading && resumes.length > 0 && (
          <div className="quick-upload">
            <div className="upload-card">
              <Sparkles className="card-icon" size={48}/>
              <h3 className="card-title">Ready for another analysis?</h3>
              <p className="card-text">
                Upload your resume and get instant feedback
              </p>
              <button
                onClick={handleUploadClick}
                className="card-btn"
                disabled={isUploading}>
                {isUploading ? (
                  <>
                    <Loader2 className="btn-spinner" size={20}/>
                    Uploading...
                  </>
                ) : (
                  <>
                    <Upload size={20}/>
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