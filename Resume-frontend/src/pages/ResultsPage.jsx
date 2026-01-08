import React from "react";
import {
  ArrowLeft,
  Download,
  CheckCircle,
  AlertCircle,
  XCircle,
  TrendingUp,
  FileText,
  Target,
  Zap,
  Award,
} from "lucide-react";
import "./ResultsPage.css";

export default function ResultsPage({ setCurrentView, currentResume }) {
  // If no resume data is passed, show error
  if (!currentResume) {
    return (
      <div className="results-container">
        <div className="error-state">
          <h2>No resume data available</h2>
          <p>Please upload a resume first</p>
          <button onClick={() => setCurrentView("dashboard")} className="back-btn">
            Go to Dashboard
          </button>
        </div>
      </div>
    );
  }

  function calculateContentScore(resume) {
    let score = 0;
    if (resume.hasExperience) score += 40;
    if (resume.hasEducation) score += 40;
    if (resume.hasSkills) score += 20;
    return score;
  }

  // Map backend data to frontend structure
  const analysisResults = {
    overallScore: currentResume.atsScore || 0,
    fileName: currentResume.fileName,
    uploadDate: new Date(currentResume.createdAt).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    }),
    checks: [
      {
        category: "ATS Compatibility",
        score: currentResume.atsScore || 0,
        status: (currentResume.atsScore || 0) >= 70 ? "passed" : "warning",
        items: [
          {
            name: "File Format",
            status: "passed",
            message: "Resume format is ATS-friendly"
          },
          {
            name: "Text Extraction",
            status: currentResume.extractedText ? "passed" : "failed",
            message: currentResume.extractedText ? "Text successfully extracted" : "Failed to extract text"
          },
        ],
      },
      {
        category: "Contact Information",
        score: currentResume.hasContactInfo ? 100 : 50,
        status: currentResume.hasContactInfo ? "passed" : "warning",
        items: [
          {
            name: "Email Address",
            status: currentResume.hasEmail ? "passed" : "failed",
            message: currentResume.hasEmail ? "Valid email found" : "No email detected"
          },
          {
            name: "Phone Number",
            status: currentResume.hasPhone ? "passed" : "failed",
            message: currentResume.hasPhone ? "Phone number present" : "No phone number detected"
          },
          {
            name: "Links/Profile",
            status: currentResume.hasLinks ? "passed" : "warning",
            message: currentResume.hasLinks ? "Professional links included" : "Consider adding LinkedIn or portfolio"
          },
        ],
      },
      {
        category: "Content Structure",
        score: calculateContentScore(currentResume),
        status: calculateContentScore(currentResume) >= 70 ? "passed" : "warning",
        items: [
          {
            name: "Experience Section",
            status: currentResume.hasExperience ? "passed" : "failed",
            message: currentResume.hasExperience ? "Experience section found" : "No experience section detected"
          },
          {
            name: "Education Section",
            status: currentResume.hasEducation ? "passed" : "failed",
            message: currentResume.hasEducation ? "Education section found" : "No education section detected"
          },
          {
            name: "Skills Section",
            status: currentResume.hasSkills ? "passed" : "warning",
            message: currentResume.hasSkills ? "Skills section found" : "Consider adding a skills section"
          },
        ],
      },
    ],
    feedback: currentResume.feedback || "Analysis completed successfully",
    keywords: currentResume.keywords ? JSON.parse(currentResume.keywords) : [],
  };

  const getScoreColor = (score) => {
    if (score >= 80) return "high";
    if (score >= 60) return "medium";
    return "low";
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case "passed":
        return <CheckCircle size={18} />;
      case "warning":
        return <AlertCircle size={18} />;
      case "failed":
        return <XCircle size={18} />;
      default:
        return null;
    }
  };

  return (
    <div className="results-container">
      {/* Header */}
      <header className="results-header">
        <button
          onClick={() => setCurrentView("dashboard")}
          className="back-btn"
        >
          <ArrowLeft size={20} />
          Back to Dashboard
        </button>

        <div className="header-actions">
          <button className="header-action-btn">
            <Download size={20} />
            Download Report
          </button>
        </div>
      </header>

      {/* Main Content */}
      <div className="results-content">
        {/* Score Overview */}
        <section className="score-overview">
          <div className="score-card-large">
            <div className="score-info">
              <div className="file-badge">
                <FileText size={20} />
                <div>
                  <p className="file-name">{analysisResults.fileName}</p>
                  <p className="file-date">Analyzed on {analysisResults.uploadDate}</p>
                </div>
              </div>

              <h1 className="score-title">Resume Analysis Results</h1>
              <p className="score-description">
                Your resume has been analyzed for ATS compatibility and content quality
              </p>
            </div>

            <div className="score-display-large">
              <div className={`score-circle-large score-${getScoreColor(analysisResults.overallScore)}`}>
                <div className="score-inner">
                  <Award className="score-icon-lg" size={40} />
                  <span className="score-value-lg">{analysisResults.overallScore}</span>
                  <span className="score-label-lg">/100</span>
                </div>
              </div>
              <div className="score-status">
                <TrendingUp size={24} />
                <div>
                  <p className="status-label">ATS Score</p>
                  <p className="status-value">
                    {analysisResults.overallScore >= 80
                      ? "Excellent"
                      : analysisResults.overallScore >= 60
                      ? "Good"
                      : "Needs Improvement"}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Feedback Section */}
        {analysisResults.feedback && (
          <section className="feedback-section">
            <h2 className="section-heading">
              <Zap size={24} />
              AI Feedback
            </h2>
            <div className="feedback-card">
              <p>{analysisResults.feedback}</p>
            </div>
          </section>
        )}

        {/* Keywords Section */}
        {analysisResults.keywords && analysisResults.keywords.length > 0 && (
          <section className="keywords-section">
            <h2 className="section-heading">
              <Target size={24} />
              Detected Keywords
            </h2>
            <div className="keywords-list">
              {analysisResults.keywords.map((keyword, idx) => (
                <span key={idx} className="keyword-tag">{keyword}</span>
              ))}
            </div>
          </section>
        )}

        {/* Detailed Analysis */}
        <section className="detailed-analysis">
          <h2 className="section-heading">
            <Target size={24} />
            Detailed Analysis
          </h2>

          <div className="analysis-grid">
            {analysisResults.checks.map((check, idx) => (
              <div key={idx} className="analysis-card">
                <div className="analysis-header">
                  <h3 className="analysis-category">{check.category}</h3>
                  <div className={`category-score score-${getScoreColor(check.score)}`}>
                    {check.score}%
                  </div>
                </div>

                <div className="analysis-items">
                  {check.items.map((item, itemIdx) => (
                    <div key={itemIdx} className={`analysis-item status-${item.status}`}>
                      <div className="item-status">
                        {getStatusIcon(item.status)}
                      </div>
                      <div className="item-content">
                        <p className="item-name">{item.name}</p>
                        <p className="item-message">{item.message}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Action Footer */}
        <section className="action-footer">
          <div className="action-card">
            <h3 className="action-title">Want more detailed analysis?</h3>
            <p className="action-text">
              Match your resume against specific job descriptions for tailored feedback
            </p>
            <div className="action-buttons">
              <button
                onClick={() => setCurrentView("dashboard")}
                className="action-btn primary"
              >
                Back to Dashboard
              </button>
              <button
                onClick={() => setCurrentView("landing")}
                className="action-btn secondary"
              >
                Upload Another Resume
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}