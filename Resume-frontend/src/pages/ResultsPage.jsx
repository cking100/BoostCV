import React, { useState, useEffect } from "react";
import {
  ArrowLeft,
  Download,
  Upload,
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
  BarChart3,
  Users,
  Briefcase,
  BookOpen,
  Star,
  Activity,
  Type,
  Layout,
  Hash,
  Globe
} from "lucide-react";
import "./ResultsPage.css";

// ─── Category metadata ────────────────────────────────────────────────────────
const CATEGORY_META = {
  contactInfo: {
    label: "Contact Info",
    icon: Users,
    description: "Email, phone, location, LinkedIn",
    weight: 10,
  },
  impactMetrics: {
    label: "Impact & Metrics",
    icon: TrendingUp,
    description: "Quantified achievements with numbers, %, $",
    weight: 25,
  },
  actionVerbs: {
    label: "Action Verbs",
    icon: Type,
    description: "Strong verbs: Led, Built, Increased vs. Helped, Assisted",
    weight: 15,
  },
  formatting: {
    label: "ATS Formatting",
    icon: Layout,
    description: "Clean layout, standard headers, date consistency",
    weight: 10,
  },
  keywordDensity: {
    label: "Keyword Density",
    icon: Hash,
    description: "Industry keywords, tools, certifications",
    weight: 15,
  },
  experienceDepth: {
    label: "Experience Depth",
    icon: Briefcase,
    description: "Role scope, bullet quality, business impact",
    weight: 15,
  },
  education: {
    label: "Education",
    icon: BookOpen,
    description: "Degree, year, GPA, relevant coursework",
    weight: 5,
  },
  professionalPresence: {
    label: "Professional Presence",
    icon: Globe,
    description: "Summary, LinkedIn, portfolio, certifications",
    weight: 5,
  },
};

const CATEGORY_ORDER = [
  "impactMetrics",
  "actionVerbs",
  "keywordDensity",
  "experienceDepth",
  "contactInfo",
  "formatting",
  "professionalPresence",
  "education",
];

export default function ResultsPage({ setCurrentView, currentResume }) {
  const [animatedScore, setAnimatedScore] = useState(0);
  const [copiedToClipboard, setCopiedToClipboard] = useState(false);
  const [expandedCategories, setExpandedCategories] = useState({});

  const atsDetails = parseAtsDetails(currentResume?.atsDetails);

  useEffect(() => {
    if (!currentResume) return;
    const targetScore = currentResume.atsScore || 0;
    const duration = 1800;
    const steps = 60;
    const increment = targetScore / steps;
    let current = 0;

    const timer = setInterval(() => {
      current += increment;
      if (current >= targetScore) {
        setAnimatedScore(targetScore);
        clearInterval(timer);
      } else {
        setAnimatedScore(Math.floor(current));
      }
    }, duration / steps);

    return () => clearInterval(timer);
  }, [currentResume?.atsScore]);

  if (!currentResume) {
    return (
      <div className="results-container">
        <div className="error-state">
          <FileText size={64} />
          <h2>No resume data available</h2>
          <p>Please upload a resume first</p>
          <button onClick={() => setCurrentView("dashboard")} className="back-btn">
            Go to Dashboard
          </button>
        </div>
      </div>
    );
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  function parseAtsDetails(raw) {
    if (!raw) return null;
    try {
      return typeof raw === "string" ? JSON.parse(raw) : raw;
    } catch {
      return null;
    }
  }

  function getScoreColor(score) {
    if (score >= 80) return "high";
    if (score >= 55) return "medium";
    return "low";
  }

  function getScoreLabel(score) {
    if (score >= 85) return "Excellent";
    if (score >= 70) return "Good";
    if (score >= 55) return "Fair";
    if (score >= 40) return "Needs Work";
    return "Critical";
  }

  function getScoreEmoji(score) {
    if (score >= 85) return "🚀";
    if (score >= 70) return "✅";
    if (score >= 55) return "⚡";
    if (score >= 40) return "⚠️";
    return "🔴";
  }

  function getCategoryScore(key) {
    if (atsDetails && atsDetails[key] && typeof atsDetails[key].score === "number") {
      return atsDetails[key].score;
    }
    // Fallback to basic checks
    if (key === "contactInfo") return currentResume.hasContactInfo ? 80 : 40;
    if (key === "experienceDepth") return currentResume.hasExperience ? 70 : 20;
    if (key === "education") return currentResume.hasEducation ? 70 : 20;
    if (key === "keywordDensity") {
      const kw = currentResume.keywords || [];
      return Math.min(100, 30 + kw.length * 5);
    }
    return 50;
  }

  function getCategoryIssues(key) {
    if (atsDetails && atsDetails[key] && Array.isArray(atsDetails[key].issues)) {
      return atsDetails[key].issues;
    }
    return [];
  }

  const overallScore = currentResume.atsScore || 0;
  const scoreColor = getScoreColor(overallScore);

  // Recommendations: either from AI or fallback
  const recommendations = (() => {
    if (atsDetails && Array.isArray(atsDetails.recommendations) && atsDetails.recommendations.length > 0) {
      return atsDetails.recommendations;
    }
    const recs = [];
    if (!currentResume.hasEmail) recs.push("Add a professional email address");
    if (!currentResume.hasPhone) recs.push("Add your phone number");
    if (!currentResume.hasExperience) recs.push("Add a Work Experience section");
    if (!currentResume.hasSkills) recs.push("Add a Skills section");
    recs.push("Add quantified achievements with numbers, percentages, and dollar values");
    recs.push("Use strong action verbs: Led, Built, Increased, Reduced, Launched");
    recs.push("Add a professional summary at the top");
    return recs;
  })();

  const keywords = Array.isArray(currentResume.keywords)
    ? currentResume.keywords
    : (() => {
        try {
          return JSON.parse(currentResume.keywords || "[]");
        } catch {
          return [];
        }
      })();

  const toggleCategory = (key) =>
    setExpandedCategories((prev) => ({ ...prev, [key]: !prev[key] }));

  const handleCopyScore = () => {
    navigator.clipboard.writeText(`My ATS Score: ${overallScore}/100`);
    setCopiedToClipboard(true);
    setTimeout(() => setCopiedToClipboard(false), 2000);
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: "My Resume ATS Score",
        text: `I scored ${overallScore}/100 on my ATS resume analysis!`,
      });
    }
  };

  // ── Render ──────────────────────────────────────────────────────────────────
  const circumference = 2 * Math.PI * 85;

  return (
    <div className="results-container">
      <header className="results-header">
        <button onClick={() => setCurrentView("dashboard")} className="back-btn">
          <ArrowLeft size={20} />
          Back to Dashboard
        </button>
        <div className="header-actions">
          <button className="header-action-btn" onClick={handleShare}>
            <Share2 size={18} />
            Share
          </button>
          <button className="header-action-btn" onClick={handleCopyScore}>
            {copiedToClipboard ? <CheckCircle size={18} /> : <Copy size={18} />}
            {copiedToClipboard ? "Copied!" : "Copy Score"}
          </button>
        </div>
      </header>

      <div className="results-content">

        {/* ── HERO SCORE ── */}
        <section className="score-overview">
          <div className="score-card-large">
            <div className="score-info">
              <div className="file-badge">
                <FileText size={20} />
                <div>
                  <p className="file-name">{currentResume.fileName}</p>
                  <p className="file-date">
                    Analyzed on{" "}
                    {currentResume.createdAt
                      ? new Date(currentResume.createdAt).toLocaleDateString("en-US", {
                          year: "numeric", month: "long", day: "numeric",
                        })
                      : "Recently"}
                  </p>
                </div>
              </div>

              <h1 className="score-title">
                ATS Analysis Results
                {overallScore >= 80 && <Sparkles size={28} className="sparkle-icon" />}
              </h1>
              <p className="score-description">
                {getScoreEmoji(overallScore)}{" "}
                {getScoreLabel(overallScore)} — {atsDetails ? "Deep AI analysis complete" : "Basic analysis complete"}
              </p>

              <div className="score-progress-bar">
                <div
                  className={`progress-fill ${scoreColor}`}
                  style={{ width: `${overallScore}%` }}
                />
              </div>

              {/* Mini category bars */}
              <div className="mini-bars">
                {CATEGORY_ORDER.slice(0, 4).map((key) => {
                  const meta = CATEGORY_META[key];
                  const s = getCategoryScore(key);
                  return (
                    <div className="mini-bar-row" key={key}>
                      <span className="mini-bar-label">{meta.label}</span>
                      <div className="mini-bar-track">
                        <div
                          className={`mini-bar-fill ${getScoreColor(s)}`}
                          style={{ width: `${s}%` }}
                        />
                      </div>
                      <span className={`mini-bar-score ${getScoreColor(s)}`}>{s}</span>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="score-display-large">
              <div className={`score-circle-large score-${scoreColor}`}>
                <svg viewBox="0 0 200 200" className="score-ring-svg">
                  <circle
                    cx="100" cy="100" r="85"
                    fill="none" stroke="#262626" strokeWidth="12"
                  />
                  <circle
                    cx="100" cy="100" r="85"
                    className="ring-progress"
                    fill="none" strokeWidth="12" strokeLinecap="round"
                    strokeDasharray={`${(animatedScore / 100) * circumference} ${circumference}`}
                    transform="rotate(-90 100 100)"
                  />
                </svg>
                <div className="score-inner">
                  <Award className="score-icon-lg" size={36} />
                  <span className="score-value-lg">{animatedScore}</span>
                  <span className="score-label-lg">/100</span>
                </div>
              </div>

              <div className="score-status">
                <Activity size={18} />
                <div>
                  <p className="status-label">ATS Rating</p>
                  <p className={`status-value ${scoreColor}`}>
                    {getScoreLabel(overallScore)}
                  </p>
                </div>
              </div>

              <button className="copy-score-btn" onClick={() => setCurrentView("aihub")}>
                <Sparkles size={16} />
                Go to AI Hub
              </button>
            </div>
          </div>
        </section>

        {/* ── DEEP CATEGORY BREAKDOWN ── */}
        <section className="detailed-analysis">
          <h2 className="section-heading">
            <BarChart3 size={24} />
            Detailed ATS Breakdown
          </h2>
          <p className="section-subtext">
            {atsDetails
              ? "Powered by Gemini AI — evaluating actual content quality, not just section presence"
              : "Basic section analysis — upload a new resume to get the deep AI breakdown"}
          </p>

          <div className="analysis-grid deep-grid">
            {CATEGORY_ORDER.map((key) => {
              const meta = CATEGORY_META[key];
              const Icon = meta.icon;
              const score = getCategoryScore(key);
              const issues = getCategoryIssues(key);
              const color = getScoreColor(score);
              const isExpanded = expandedCategories[key] !== false; // default open

              return (
                <div key={key} className={`analysis-card deep-card`}>
                  <div
                    className="analysis-header clickable"
                    onClick={() => toggleCategory(key)}
                  >
                    <div className="header-left">
                      <div className={`cat-icon-wrap ${color}`}>
                        <Icon size={18} />
                      </div>
                      <div>
                        <h3 className="analysis-category">{meta.label}</h3>
                        <p className="cat-description">{meta.description}</p>
                      </div>
                    </div>
                    <div className="header-right">
                      <div className="score-gauge-wrap">
                        <div className={`category-score-pill score-${color}`}>
                          {score}
                          <span className="score-denom">/100</span>
                        </div>
                        <div className="gauge-bar">
                          <div
                            className={`gauge-fill ${color}`}
                            style={{ width: `${score}%` }}
                          />
                        </div>
                      </div>
                      {isExpanded ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                    </div>
                  </div>

                  {isExpanded && (
                    <div className="analysis-items">
                      {issues.length === 0 ? (
                        <div className="analysis-item status-passed no-issues">
                          <CheckCircle size={16} />
                          <p>Looking good — no major issues found in this category</p>
                        </div>
                      ) : (
                        issues.map((issue, i) => (
                          <div key={i} className="analysis-item status-warning deep-issue">
                            <AlertCircle size={15} />
                            <p className="item-message">{issue}</p>
                          </div>
                        ))
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </section>

        {/* ── TOP RECOMMENDATIONS ── */}
        {recommendations.length > 0 && (
          <section className="recommendations">
            <h2 className="section-heading">
              <Lightbulb size={24} />
              AI Recommendations ({recommendations.length})
            </h2>
            <div className="recommendations-list">
              {recommendations.map((rec, idx) => {
                const priority = idx < 2 ? "high" : idx < 4 ? "medium" : "low";
                const icons = ["🎯", "⚡", "💡", "📈", "🔑", "✍️", "🧩"];
                return (
                  <div key={idx} className={`recommendation-card priority-${priority}`}>
                    <div className="rec-header">
                      <span className="rec-icon">{icons[idx % icons.length]}</span>
                      <div className="rec-info">
                        <div className="rec-title-row">
                          <h3 className="rec-title">Recommendation #{idx + 1}</h3>
                          <span className={`priority-badge priority-${priority}`}>
                            {priority} impact
                          </span>
                        </div>
                        <p className="rec-description">{rec}</p>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </section>
        )}

        {/* ── KEYWORDS ── */}
        {keywords.length > 0 && (
          <section className="keywords-section">
            <h2 className="section-heading">
              <Target size={24} />
              Detected Keywords ({keywords.length})
            </h2>
            <div className="keywords-list">
              {keywords.map((keyword, idx) => (
                <span key={idx} className="keyword-tag">
                  {keyword}
                </span>
              ))}
            </div>
          </section>
        )}

        {/* ── ACTION FOOTER ── */}
        <section className="action-footer">
          <div className="action-card">
            <Sparkles size={48} className="action-icon" />
            <h3 className="action-title">
              {overallScore >= 80
                ? "Great job! Ready to apply?"
                : "Want to improve your score?"}
            </h3>
            <p className="action-text">
              {overallScore >= 80
                ? "Your resume looks great! Head to AI Hub for cover letters & interview prep."
                : "Fix the issues above, then upload an improved version to rescore."}
            </p>
            <div className="action-buttons">
              <button onClick={() => setCurrentView("dashboard")} className="action-btn primary">
                <Upload size={20} />
                Upload New Version
              </button>
              <button onClick={() => setCurrentView("aihub")} className="action-btn ai-hub">
                <Sparkles size={20} />
                AI Hub
              </button>
              <button onClick={() => setCurrentView("dashboard")} className="action-btn secondary">
                Back to Dashboard
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}