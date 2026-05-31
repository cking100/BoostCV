import React, { useState, useEffect } from "react";
import {
  ArrowLeft,
  BarChart3,
  TrendingUp,
  TrendingDown,
  FileText,
  Award,
  Target,
  Calendar,
  Zap,
  Brain,
  Settings,
  LogOut,
  Activity,
  Star,
  Clock,
  CheckCircle,
  AlertCircle,
  Upload,
} from "lucide-react";
import api from "./api";
import "./AnalyticsPage.css";

export default function AnalyticsPage({ setCurrentView, onLogout }) {
  const [resumes, setResumes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    api
      .getResumes()
      .then((data) => setResumes(data || []))
      .catch(() => setResumes([]))
      .finally(() => setIsLoading(false));
  }, []);

  // ── Derived stats ─────────────────────────────────────────────────────────
  const totalResumes = resumes.length;
  const scores = resumes.map((r) => r.atsScore || 0);
  const avgScore = totalResumes
    ? Math.round(scores.reduce((a, b) => a + b, 0) / totalResumes)
    : 0;
  const bestScore = totalResumes ? Math.max(...scores) : 0;
  const latestScore = totalResumes ? scores[0] : 0;
  const prevScore = scores.length > 1 ? scores[1] : null;
  const scoreChange =
    prevScore !== null ? latestScore - prevScore : null;

  // Score distribution buckets
  const buckets = { excellent: 0, good: 0, fair: 0, needsWork: 0 };
  scores.forEach((s) => {
    if (s >= 85) buckets.excellent++;
    else if (s >= 70) buckets.good++;
    else if (s >= 55) buckets.fair++;
    else buckets.needsWork++;
  });

  // Monthly upload counts (last 6 months)
  const monthlyData = (() => {
    const months = [];
    for (let i = 5; i >= 0; i--) {
      const d = new Date();
      d.setMonth(d.getMonth() - i);
      const label = d.toLocaleString("default", { month: "short" });
      const count = resumes.filter((r) => {
        const rd = new Date(r.createdAt);
        return (
          rd.getMonth() === d.getMonth() &&
          rd.getFullYear() === d.getFullYear()
        );
      }).length;
      months.push({ label, count });
    }
    return months;
  })();

  const maxMonthCount = Math.max(...monthlyData.map((m) => m.count), 1);

  // Score over time (sorted oldest → newest, last 8)
  const scoreTrend = [...resumes]
    .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
    .slice(-8);

  // Insights
  const insights = [];
  if (avgScore >= 80)
    insights.push({
      type: "success",
      text: "Your average ATS score is excellent! Keep uploading optimised resumes.",
    });
  else if (avgScore >= 60)
    insights.push({
      type: "warning",
      text: "Your average score is decent. Focus on impact metrics and keywords.",
    });
  else if (totalResumes > 0)
    insights.push({
      type: "error",
      text: "Your scores need improvement. Check the AI Hub for tailored suggestions.",
    });

  if (scoreChange !== null && scoreChange > 0)
    insights.push({
      type: "success",
      text: `Your latest upload improved by +${scoreChange} points — great progress!`,
    });
  else if (scoreChange !== null && scoreChange < 0)
    insights.push({
      type: "warning",
      text: `Your last upload dropped by ${scoreChange} points. Review the breakdown.`,
    });

  if (totalResumes >= 3)
    insights.push({
      type: "info",
      text: `You've uploaded ${totalResumes} resumes. Use the Results page to compare detailed breakdowns.`,
    });

  const handleLogout = () => {
    api.logout();
    if (onLogout) onLogout();
  };

  return (
    <div className="analytics-container">
      {/* ── Sidebar ── */}
      <aside className="sidebar">
        <div className="sidebar-top">
          <div className="logo">
            <div className="logo-icon">
              <Zap size={22} />
            </div>
            <span className="logo-name">BoostCV</span>
          </div>
        </div>

        <nav className="sidebar-menu">
          <button
            className="menu-item"
            onClick={() => setCurrentView("dashboard")}
          >
            <FileText size={20} />
            <span>My Resumes</span>
          </button>
          <button
            className="menu-item ai-hub-btn"
            onClick={() => setCurrentView("aihub")}
          >
            <Brain size={20} />
            <span>AI Hub</span>
          </button>
          <button className="menu-item active">
            <BarChart3 size={20} />
            <span>Analytics</span>
          </button>
          <button
            className="menu-item"
            onClick={() => setCurrentView("settings")}
          >
            <Settings size={20} />
            <span>Settings</span>
          </button>
        </nav>

        <button onClick={handleLogout} className="logout-btn">
          <LogOut size={20} />
          <span>Logout</span>
        </button>
      </aside>

      {/* ── Main ── */}
      <main className="analytics-main">
        <header className="analytics-header">
          <div>
            <h1 className="analytics-title">Analytics</h1>
            <p className="analytics-subtitle">
              Track your resume performance over time
            </p>
          </div>
          <button
            className="upload-shortcut"
            onClick={() => setCurrentView("dashboard")}
          >
            <Upload size={18} />
            Upload Resume
          </button>
        </header>

        {isLoading ? (
          <div className="analytics-loading">
            <div className="loading-spinner" />
            <p>Loading your analytics…</p>
          </div>
        ) : totalResumes === 0 ? (
          <div className="empty-analytics">
            <BarChart3 size={64} className="empty-icon" />
            <h2>No data yet</h2>
            <p>Upload your first resume to see analytics here.</p>
            <button
              className="empty-cta"
              onClick={() => setCurrentView("dashboard")}
            >
              <Upload size={18} />
              Upload Now
            </button>
          </div>
        ) : (
          <>
            {/* ── KPI Cards ── */}
            <div className="kpi-grid">
              <div className="kpi-card indigo">
                <div className="kpi-icon">
                  <FileText size={22} />
                </div>
                <div className="kpi-info">
                  <p className="kpi-label">Total Resumes</p>
                  <p className="kpi-value">{totalResumes}</p>
                </div>
              </div>

              <div className="kpi-card purple">
                <div className="kpi-icon">
                  <Activity size={22} />
                </div>
                <div className="kpi-info">
                  <p className="kpi-label">Average Score</p>
                  <p className="kpi-value">{avgScore}</p>
                </div>
              </div>

              <div className="kpi-card emerald">
                <div className="kpi-icon">
                  <Award size={22} />
                </div>
                <div className="kpi-info">
                  <p className="kpi-label">Best Score</p>
                  <p className="kpi-value">{bestScore}</p>
                </div>
              </div>

              <div
                className={`kpi-card ${
                  scoreChange === null
                    ? "amber"
                    : scoreChange >= 0
                    ? "emerald"
                    : "rose"
                }`}
              >
                <div className="kpi-icon">
                  {scoreChange !== null && scoreChange >= 0 ? (
                    <TrendingUp size={22} />
                  ) : (
                    <TrendingDown size={22} />
                  )}
                </div>
                <div className="kpi-info">
                  <p className="kpi-label">Score Change</p>
                  <p className="kpi-value">
                    {scoreChange === null
                      ? "—"
                      : scoreChange >= 0
                      ? `+${scoreChange}`
                      : scoreChange}
                  </p>
                </div>
              </div>
            </div>

            {/* ── Charts Row ── */}
            <div className="charts-row">
              {/* Score Trend */}
              <div className="chart-card wide">
                <h2 className="chart-title">
                  <TrendingUp size={18} />
                  Score Over Time
                </h2>
                {scoreTrend.length < 2 ? (
                  <p className="chart-empty">
                    Upload more resumes to see a trend.
                  </p>
                ) : (
                  <div className="line-chart">
                    <div className="line-chart-inner">
                      {scoreTrend.map((r, i) => {
                        const pct = (r.atsScore || 0);
                        const color =
                          pct >= 80
                            ? "#10b981"
                            : pct >= 60
                            ? "#f59e0b"
                            : "#ef4444";
                        return (
                          <div key={r.id} className="trend-bar-wrap">
                            <div className="trend-bar-track">
                              <div
                                className="trend-bar-fill"
                                style={{
                                  height: `${pct}%`,
                                  background: color,
                                }}
                              />
                            </div>
                            <span className="trend-score">{pct}</span>
                            <span className="trend-label">
                              #{i + 1}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                    <div className="chart-legend">
                      <span className="legend-dot emerald" /> ≥80
                      <span className="legend-dot amber" /> 60–79
                      <span className="legend-dot rose" /> &lt;60
                    </div>
                  </div>
                )}
              </div>

              {/* Monthly uploads */}
              <div className="chart-card">
                <h2 className="chart-title">
                  <Calendar size={18} />
                  Monthly Uploads
                </h2>
                <div className="bar-chart">
                  {monthlyData.map((m) => (
                    <div key={m.label} className="bar-col">
                      <div className="bar-track">
                        <div
                          className="bar-fill"
                          style={{
                            height: `${(m.count / maxMonthCount) * 100}%`,
                          }}
                        />
                      </div>
                      <span className="bar-count">{m.count}</span>
                      <span className="bar-label">{m.label}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* ── Distribution + Insights Row ── */}
            <div className="bottom-row">
              {/* Score Distribution */}
              <div className="dist-card">
                <h2 className="chart-title">
                  <Target size={18} />
                  Score Distribution
                </h2>
                <div className="dist-list">
                  {[
                    {
                      label: "Excellent (85+)",
                      count: buckets.excellent,
                      color: "emerald",
                    },
                    {
                      label: "Good (70–84)",
                      count: buckets.good,
                      color: "indigo",
                    },
                    {
                      label: "Fair (55–69)",
                      count: buckets.fair,
                      color: "amber",
                    },
                    {
                      label: "Needs Work (<55)",
                      count: buckets.needsWork,
                      color: "rose",
                    },
                  ].map((b) => (
                    <div key={b.label} className="dist-row">
                      <div className="dist-meta">
                        <span className={`dist-dot ${b.color}`} />
                        <span className="dist-label">{b.label}</span>
                        <span className="dist-count">{b.count}</span>
                      </div>
                      <div className="dist-bar-track">
                        <div
                          className={`dist-bar-fill ${b.color}`}
                          style={{
                            width: totalResumes
                              ? `${(b.count / totalResumes) * 100}%`
                              : "0%",
                          }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Insights */}
              <div className="insights-card">
                <h2 className="chart-title">
                  <Star size={18} />
                  Insights
                </h2>
                {insights.length === 0 ? (
                  <p className="chart-empty">
                    Upload more resumes to generate insights.
                  </p>
                ) : (
                  <div className="insights-list">
                    {insights.map((ins, i) => (
                      <div key={i} className={`insight-item ${ins.type}`}>
                        {ins.type === "success" ? (
                          <CheckCircle size={16} />
                        ) : (
                          <AlertCircle size={16} />
                        )}
                        <p>{ins.text}</p>
                      </div>
                    ))}
                  </div>
                )}

                {/* Recent activity */}
                <h3 className="recent-title">
                  <Clock size={14} />
                  Recent Activity
                </h3>
                <div className="recent-list">
                  {resumes.slice(0, 5).map((r) => {
                    const s = r.atsScore || 0;
                    const color =
                      s >= 80 ? "emerald" : s >= 60 ? "amber" : "rose";
                    return (
                      <div key={r.id} className="recent-item">
                        <FileText size={14} />
                        <span className="recent-name">{r.fileName}</span>
                        <span className={`recent-score ${color}`}>{s}</span>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
