import React, { useState, useEffect } from "react";
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
  Sparkles,
  Share2,
  Copy,
  ChevronDown,
  ChevronUp,
  Lightbulb,
  BarChart3
} from "lucide-react";
import "./ResultsPage.css";

export default function ResultsPage({ setCurrentView, currentResume }) {
  const [animatedScore, setAnimatedScore] = useState(0);
  const [showRecommendations, setShowRecommendations] = useState(true);
  const [copiedToClipboard, setCopiedToClipboard] = useState(false);
  const [expandedSections, setExpandedSections] = useState({});

  if (!currentResume) {
    return (
      <div className="results-container">
        <div className="error-state">
          <FileText size={64}/>
          <h2>No resume data available</h2>
          <p>Please upload a resume first</p>
          <button onClick={() => setCurrentView("dashboard")} className="back-btn">
            Go to Dashboard
          </button>
        </div>
      </div>
    );
  }

  useEffect(() => {
    const targetScore = currentResume.atsScore || 0;
    const duration = 2000;
    const steps = 60;
    const increment = targetScore / steps;
    let current = 0;

    const timer = setInterval(() => {
      current += increment;
      if(current >= targetScore){
        setAnimatedScore(targetScore);
        clearInterval(timer);
      }else{
        setAnimatedScore(Math.floor(current));
      }
    }, duration / steps);

    return () => clearInterval(timer);
  }, [currentResume.atsScore]);

  function calculateContentScore(resume) {
    let score = 0;
    if (resume.hasExperience) score += 40;
    if (resume.hasEducation) score += 40;
    if (resume.hasSkills) score += 20;
    return score;
  }

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
            message: "Resume format is ATS-friendly",
            impact: "high"
          },
          {
            name: "Text Extraction",
            status: currentResume.extractedText ? "passed" : "failed",
            message: currentResume.extractedText ? "Text successfully extracted" : "Failed to extract text",
            impact: "high"
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
            message: currentResume.hasEmail ? "Valid email found" : "No email detected",
            impact: "high"
          },
          {
            name: "Phone Number",
            status: currentResume.hasPhone ? "passed" : "failed",
            message: currentResume.hasPhone ? "Phone number present" : "No phone number detected",
            impact: "high"
          },
          {
            name: "Links/Profile",
            status: currentResume.hasLinks ? "passed" : "warning",
            message: currentResume.hasLinks ? "Professional links included" : "Consider adding LinkedIn or portfolio",
            impact: "medium"
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
            message: currentResume.hasExperience ? "Experience section found" : "No experience section detected",
            impact: "high"
          },
          {
            name: "Education Section",
            status: currentResume.hasEducation ? "passed" : "failed",
            message: currentResume.hasEducation ? "Education section found" : "No education section detected",
            impact: "high"
          },
          {
            name: "Skills Section",
            status: currentResume.hasSkills ? "passed" : "warning",
            message: currentResume.hasSkills ? "Skills section found" : "Consider adding a skills section",
            impact: "medium"
          },
        ],
      },
    ],
    feedback: currentResume.feedback || "Analysis completed successfully",
    keywords: currentResume.keywords ? JSON.parse(currentResume.keywords) : [],
  };

  const recommendations = generateRecommendations(analysisResults);

  function generateRecommendations(results) {
    const recs = [];
    
    if(!currentResume.hasEmail){
      recs.push({
        priority: "high",
        title: "Add Email Address",
        description: "Include a professional email address at the top of your resume",
        icon: "✉️"
      });
    }
    
    if(!currentResume.hasPhone){
      recs.push({
        priority: "high",
        title: "Add Phone Number",
        description: "Include your phone number so recruiters can easily contact you",
        icon: "📱"
      });
    }
    
    if(!currentResume.hasExperience){
      recs.push({
        priority: "high",
        title: "Add Work Experience",
        description: "Include your work history with bullet points highlighting achievements",
        icon: "💼"
      });
    }
    
    if(!currentResume.hasSkills){
      recs.push({
        priority: "medium",
        title: "Add Skills Section",
        description: "List relevant technical and soft skills to improve ATS matching",
        icon: "⚡"
      });
    }
    
    if(analysisResults.keywords.length < 5){
      recs.push({
        priority: "medium",
        title: "Increase Keywords",
        description: "Add more industry-specific keywords to improve ATS score",
        icon: "🎯"
      });
    }
    
    if(results.overallScore < 80){
      recs.push({
        priority: "low",
        title: "Optimize for ATS",
        description: "Review formatting and use standard section headings",
        icon: "🔍"
      });
    }
    
    return recs;
  }

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

  const handleShare = () => {
    if(navigator.share){
      navigator.share({
        title: 'My Resume Score',
        text: `I got a ${analysisResults.overallScore}/100 ATS score on BoostCV!`,
      });
    }
  };

  const handleCopyScore = () => {
    navigator.clipboard.writeText(`My ATS Score: ${analysisResults.overallScore}/100`);
    setCopiedToClipboard(true);
    setTimeout(() => setCopiedToClipboard(false), 2000);
  };

  const toggleSection = (category) => {
    setExpandedSections(prev => ({
      ...prev,
      [category]: !prev[category]
    }));
  };

  const passedCount = analysisResults.checks.reduce((sum, check) => 
    sum + check.items.filter(item => item.status === 'passed').length, 0
  );
  const totalCount = analysisResults.checks.reduce((sum, check) => 
    sum + check.items.length, 0
  );

  return (
    <div className="results-container">
      <header className="results-header">
        <button
          onClick={() => setCurrentView("dashboard")}
          className="back-btn">
          <ArrowLeft size={20} />
          Back to Dashboard
        </button>

        <div className="header-actions">
          <button className="header-action-btn" onClick={handleShare}>
            <Share2 size={20} />
            Share
          </button>
          <button className="header-action-btn">
            <Download size={20} />
            Download
          </button>
        </div>
      </header>

      <div className="results-content">
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

              <h1 className="score-title">
                Resume Analysis Results
                {analysisResults.overallScore >= 80 && <Sparkles size={28} className="sparkle-icon"/>}
              </h1>
              <p className="score-description">
                Your resume scored {passedCount}/{totalCount} checks
              </p>

              <div className="score-progress-bar">
                <div 
                  className={`progress-fill ${getScoreColor(analysisResults.overallScore)}`}
                  style={{width: `${analysisResults.overallScore}%`}}
                />
              </div>
            </div>

            <div className="score-display-large">
              <div className={`score-circle-large score-${getScoreColor(analysisResults.overallScore)}`}>
                <svg viewBox="0 0 200 200" className="score-ring-svg">
                  <circle 
                    cx="100" 
                    cy="100" 
                    r="85" 
                    className="ring-bg"
                    fill="none"
                    stroke="#262626"
                    strokeWidth="12"
                  />
                  <circle 
                    cx="100" 
                    cy="100" 
                    r="85" 
                    className="ring-progress"
                    fill="none"
                    strokeWidth="12"
                    strokeLinecap="round"
                    strokeDasharray={`${(animatedScore / 100) * 534} 534`}
                    transform="rotate(-90 100 100)"
                  />
                </svg>
                <div className="score-inner">
                  <Award className="score-icon-lg" size={40} />
                  <span className="score-value-lg">{animatedScore}</span>
                  <span className="score-label-lg">/100</span>
                </div>
              </div>

              <button className="copy-score-btn" onClick={handleCopyScore}>
                {copiedToClipboard ? (
                  <>
                    <CheckCircle size={16}/>
                    Copied!
                  </>
                ) : (
                  <>
                    <Copy size={16}/>
                    Copy Score
                  </>
                )}
              </button>

              <div className="score-status">
                <TrendingUp size={20} />
                <div>
                  <p className="status-label">ATS Rating</p>
                  <p className={`status-value ${getScoreColor(analysisResults.overallScore)}`}>
                    {analysisResults.overallScore >= 80
                      ? "Excellent"
                      : analysisResults.overallScore >= 60
                      ? "Good"
                      : "Needs Work"}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {recommendations.length > 0 && (
          <section className="recommendations">
            <div className="section-header-toggle">
              <h2 className="section-heading">
                <Lightbulb size={24} />
                Quick Wins ({recommendations.length})
              </h2>
              <button 
                className="toggle-btn"
                onClick={() => setShowRecommendations(!showRecommendations)}>
                {showRecommendations ? <ChevronUp size={20}/> : <ChevronDown size={20}/>}
              </button>
            </div>

            {showRecommendations && (
              <div className="recommendations-list">
                {recommendations.map((rec, idx) => (
                  <div key={idx} className={`recommendation-card priority-${rec.priority}`}>
                    <div className="rec-header">
                      <span className="rec-icon">{rec.icon}</span>
                      <div className="rec-info">
                        <div className="rec-title-row">
                          <h3 className="rec-title">{rec.title}</h3>
                          <span className={`priority-badge priority-${rec.priority}`}>
                            {rec.priority}
                          </span>
                        </div>
                        <p className="rec-description">{rec.description}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {analysisResults.feedback && (
          <section className="feedback-section">
            <h2 className="section-heading">
              <Zap size={24} />
              AI Feedback
            </h2>
            <div className="feedback-card">
              <div className="feedback-icon">🤖</div>
              <p>{analysisResults.feedback}</p>
            </div>
          </section>
        )}

        {analysisResults.keywords && analysisResults.keywords.length > 0 && (
          <section className="keywords-section">
            <h2 className="section-heading">
              <Target size={24} />
              Detected Keywords ({analysisResults.keywords.length})
            </h2>
            <div className="keywords-list">
              {analysisResults.keywords.map((keyword, idx) => (
                <span key={idx} className="keyword-tag">
                  {keyword}
                </span>
              ))}
            </div>
          </section>
        )}

        <section className="detailed-analysis">
          <h2 className="section-heading">
            <BarChart3 size={24} />
            Detailed Breakdown
          </h2>

          <div className="analysis-grid">
            {analysisResults.checks.map((check, idx) => (
              <div key={idx} className="analysis-card">
                <div 
                  className="analysis-header clickable"
                  onClick={() => toggleSection(check.category)}>
                  <div className="header-left">
                    <h3 className="analysis-category">{check.category}</h3>
                    <span className="items-count">{check.items.length} items</span>
                  </div>
                  <div className="header-right">
                    <div className={`category-score score-${getScoreColor(check.score)}`}>
                      {check.score}%
                    </div>
                    {expandedSections[check.category] ? 
                      <ChevronUp size={20}/> : <ChevronDown size={20}/>
                    }
                  </div>
                </div>

                {(expandedSections[check.category] !== false) && (
                  <div className="analysis-items">
                    {check.items.map((item, itemIdx) => (
                      <div key={itemIdx} className={`analysis-item status-${item.status}`}>
                        <div className="item-status">
                          {getStatusIcon(item.status)}
                        </div>
                        <div className="item-content">
                          <div className="item-header">
                            <p className="item-name">{item.name}</p>
                            {item.impact && (
                              <span className={`impact-badge impact-${item.impact}`}>
                                {item.impact} impact
                              </span>
                            )}
                          </div>
                          <p className="item-message">{item.message}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </section>

        <section className="action-footer">
          <div className="action-card">
            <Sparkles size={48} className="action-icon"/>
            <h3 className="action-title">
              {analysisResults.overallScore >= 80 
                ? "Great job! Ready to apply?" 
                : "Want to improve your score?"}
            </h3>
            <p className="action-text">
              {analysisResults.overallScore >= 80
                ? "Your resume looks great! Upload another or start applying."
                : "Upload an updated version after making improvements"}
            </p>
            <div className="action-buttons">
              <button
                onClick={() => setCurrentView("dashboard")}
                className="action-btn primary">
                <Upload size={20}/>
                Upload New Version
              </button>
              <button
                onClick={() => setCurrentView("dashboard")}
                className="action-btn secondary">
                Back to Dashboard
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}