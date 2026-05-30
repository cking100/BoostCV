// src/pages/LandingPage.jsx
import React, { useState, useRef } from 'react';
import {
  Upload, FileText, Sparkles, Shield, Loader2,
  Check, Zap, AlertCircle, ArrowRight, X,
  Bot, Target, Mic, Map, Users, Brain, ChevronRight,
  CheckCircle, AlertCircle as WarnIcon
} from 'lucide-react';
import api from './api';
import './LandingPage.css';

// ── All 7 GenAI features derived from backend ─────────────────────
const AI_FEATURES = [
  {
    id: 'ats',
    icon: '🎯',
    iconClass: 'indigo',
    accent: 'indigo',
    title: 'ATS Score & Analysis',
    desc: 'Instant ATS compatibility scoring with detailed breakdown of what\'s passing, failing, and flagged — before a recruiter ever sees your resume.',
    tag: 'gemini',
    tagLabel: 'Gemini AI',
  },
  {
    id: 'job-match',
    icon: '🔗',
    iconClass: 'purple',
    accent: 'purple',
    title: 'Job Match Analyzer',
    desc: 'Paste any job description and get a match score, identified keyword gaps, content suggestions and an improvement plan specific to that role.',
    tag: 'gemini',
    tagLabel: 'Gemini AI',
  },
  {
    id: 'rag-coach',
    icon: '🤖',
    iconClass: 'emerald',
    accent: 'emerald',
    title: 'RAG Career Coach',
    desc: 'Chat with an AI coach that has actually read your resume. Answers grounded in your real experience — not generic advice — using vector RAG retrieval.',
    tag: 'rag',
    tagLabel: 'RAG Pipeline',
  },
  {
    id: 'cover-letter',
    icon: '📄',
    iconClass: 'amber',
    accent: 'amber',
    title: 'Cover Letter Generator',
    desc: 'Generates a tailored, professional cover letter under 400 words that maps your resume achievements directly to the job requirements.',
    tag: 'gemini',
    tagLabel: 'Gemini AI',
  },
  {
    id: 'interview',
    icon: '🎤',
    iconClass: 'rose',
    accent: 'rose',
    title: 'Interview Coach',
    desc: '10 likely interview questions with model answers grounded in your actual resume — mix of behavioral, technical and situational Qs.',
    tag: 'gemini',
    tagLabel: 'Gemini AI',
  },
  {
    id: 'career-paths',
    icon: '🗺️',
    iconClass: 'sky',
    accent: 'sky',
    title: 'Career Path Advisor',
    desc: '3 distinct career trajectories you can pursue, with skill gaps to close, example target roles, and a concrete 90-day action plan for each.',
    tag: 'gemini',
    tagLabel: 'Gemini AI',
  },
  {
    id: 'candidate-match',
    icon: '👥',
    iconClass: 'violet',
    accent: 'violet',
    title: 'Candidate Matching',
    desc: 'Global vector similarity search across all indexed resumes. Recruiters can find the best-matching candidates for any job description instantly.',
    tag: 'vector',
    tagLabel: 'Vector Search',
  },
];

// ── Preview tab config ────────────────────────────────────────────
const PREVIEW_TABS = [
  { id: 'ats',       label: 'ATS Check',      Icon: Sparkles  },
  { id: 'job-match', label: 'Job Match',       Icon: Target    },
  { id: 'coach',     label: 'AI Coach',        Icon: Bot       },
  { id: 'cover',     label: 'Cover Letter',    Icon: FileText  },
  { id: 'interview', label: 'Interview',       Icon: Mic       },
  { id: 'paths',     label: 'Career Paths',    Icon: Map       },
];

export default function LandingPage({ setCurrentView, setCurrentResume }) {
  const [isDragging, setIsDragging]     = useState(false);
  const [isUploading, setIsUploading]   = useState(false);
  const [uploadError, setUploadError]   = useState(null);
  const [activeTab, setActiveTab]       = useState('ats');
  const fileInputRef                    = useRef(null);

  const handleDragOver  = (e) => { e.preventDefault(); setIsDragging(true); };
  const handleDragLeave = ()  => setIsDragging(false);

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    const files = e.dataTransfer.files;
    if (files.length > 0) handleFileUpload(files[0]);
  };

  const handleFileUpload = async (file) => {
    setUploadError(null);

    const validTypes = [
      'application/pdf',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    ];

    if (!validTypes.includes(file.type)) {
      setUploadError('Please upload a PDF or DOCX file');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      setUploadError('File size must be less than 10MB');
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      setUploadError('Please sign in to upload and analyse your resume');
      setTimeout(() => setCurrentView('login'), 1200);
      return;
    }

    setIsUploading(true);
    try {
      const response = await api.uploadResume(file);
      if (setCurrentResume) setCurrentResume(response);
      setCurrentView('results');
    } catch (error) {
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
    if (files && files.length > 0) handleFileUpload(files[0]);
  };

  return (
    <div className="landing-page">
      <input
        ref={fileInputRef}
        type="file"
        accept=".pdf,.docx"
        onChange={handleFileChange}
        style={{ display: 'none' }}
        id="landing-file-input"
      />

      {/* ── Navbar ── */}
      <nav className="lp-nav">
        <div className="lp-nav-inner">
          <div className="lp-logo">
            <div className="lp-logo-icon">
              <Zap size={20} />
            </div>
            <span className="lp-logo-name">ResumeAI</span>
            <span className="lp-badge">Beta</span>
          </div>

          <div className="lp-nav-actions">
            <button
              onClick={() => setCurrentView('login')}
              className="lp-nav-sign-in"
              id="landing-signin-btn"
            >
              Sign In
            </button>
            <button
              onClick={() => setCurrentView('register')}
              className="lp-nav-cta"
              id="landing-get-started-btn"
            >
              Get Started Free
            </button>
          </div>
        </div>
      </nav>

      {/* ── Hero ── */}
      <main>
        <section className="lp-hero">
          <div className="lp-container">
            <div className="lp-eyebrow">
              <Sparkles size={14} />
              <span>Powered by Gemini AI · RAG · Vector Search</span>
            </div>

            <h1 className="lp-heading">
              Land more interviews.<br />
              <span className="lp-heading-accent">Stop getting rejected</span><br />
              by ATS systems.
            </h1>

            <p className="lp-subheading">
              Upload your resume and get instant AI analysis — ATS score, job matching,
              personalized coaching, cover letter generation, interview prep, and career
              path planning. All powered by Gemini AI and RAG pipelines.
            </p>

            <div className="lp-hero-actions">
              <button
                className="lp-btn-primary"
                onClick={handleUploadClick}
                disabled={isUploading}
                id="landing-upload-cta"
              >
                {isUploading ? (
                  <><Loader2 size={18} className="lp-spinner" /> Analysing...</>
                ) : (
                  <><Upload size={18} /> Analyse My Resume</>
                )}
              </button>
              <button
                className="lp-btn-secondary"
                onClick={() => setCurrentView('login')}
                id="landing-aihub-cta"
              >
                <Brain size={17} />
                Open AI Hub
                <ChevronRight size={15} />
              </button>
            </div>

            <div className="lp-trust-strip">
              <div className="lp-trust-item">
                <div className="lp-trust-dot"></div>
                <span>ATS Compatibility Check</span>
              </div>
              <div className="lp-trust-item">
                <div className="lp-trust-dot"></div>
                <span>7 AI-powered tools</span>
              </div>
              <div className="lp-trust-item">
                <div className="lp-trust-dot"></div>
                <span>Built on Gemini 1.5 Flash</span>
              </div>
              <div className="lp-trust-item">
                <div className="lp-trust-dot"></div>
                <span>RAG vector pipeline</span>
              </div>
            </div>
          </div>
        </section>

        {/* ── Upload zone ── */}
        <div className="lp-container" style={{ paddingLeft: 24, paddingRight: 24 }}>
          <div
            className={`lp-upload-zone ${isDragging ? 'dragging' : ''} ${isUploading ? 'uploading' : ''}`}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={!isUploading ? handleUploadClick : undefined}
            role="button"
            tabIndex={0}
            aria-label="Upload resume"
            id="landing-dropzone"
          >
            {!isUploading ? (
              <>
                <div className="lp-upload-icon-ring">
                  <Upload size={30} />
                </div>
                <h3 className="lp-upload-title">Drop your resume here</h3>
                <p className="lp-upload-subtitle">or click to browse files</p>

                {uploadError && (
                  <div className="lp-upload-error">
                    <AlertCircle size={15} />
                    <span>{uploadError}</span>
                    <button
                      onClick={(e) => { e.stopPropagation(); setUploadError(null); }}
                      id="landing-clear-error"
                    >
                      <X size={14} />
                    </button>
                  </div>
                )}

                <button
                  className="lp-btn-primary"
                  onClick={handleUploadClick}
                  id="landing-analyze-btn"
                  style={{ margin: '0 auto' }}
                >
                  <Upload size={17} />
                  Analyse Resume
                </button>

                <div className="lp-upload-meta">
                  <span>PDF or DOCX · Max 10MB</span>
                  <div className="lp-upload-secure">
                    <Shield size={13} />
                    <span>Private &amp; secure</span>
                  </div>
                </div>
              </>
            ) : (
              <div className="lp-upload-loading">
                <Loader2 size={44} className="lp-spinner" />
                <h3>Analysing your resume…</h3>
                <p>Running ATS check and AI scoring</p>
                <div className="lp-progress-bar">
                  <div className="lp-progress-fill" />
                </div>
              </div>
            )}
          </div>
        </div>

        {/* ── GenAI Feature Showcase (Interactive Tabs) ── */}
        <div className="lp-container" style={{ paddingLeft: 24, paddingRight: 24 }}>
          <div className="lp-feature-showcase">
            <p className="lp-showcase-label">Live preview — 7 AI-powered tools inside</p>

            <div className="lp-tabs" role="tablist" id="landing-preview-tabs">
              {PREVIEW_TABS.map(tab => {
                const Icon = tab.Icon;
                return (
                  <button
                    key={tab.id}
                    role="tab"
                    aria-selected={activeTab === tab.id}
                    className={`lp-tab-btn ${activeTab === tab.id ? 'active' : ''}`}
                    onClick={() => setActiveTab(tab.id)}
                    id={`landing-tab-${tab.id}`}
                  >
                    <Icon size={14} />
                    {tab.label}
                  </button>
                );
              })}
            </div>

            <div className="lp-preview-card" role="tabpanel">
              <div className="lp-preview-topbar">
                <div className="lp-preview-dots">
                  <span /><span /><span />
                </div>
                <span className="lp-preview-tab-label">
                  {activeTab === 'ats'       && 'ATS Score Report — Live Mockup'}
                  {activeTab === 'job-match' && 'Job Match Analyzer — Mock Output'}
                  {activeTab === 'coach'     && 'RAG Career Coach — Grounded in your resume'}
                  {activeTab === 'cover'     && 'Cover Letter Builder — AI-written draft'}
                  {activeTab === 'interview' && 'Interview Coach — Q&A preparation'}
                  {activeTab === 'paths'     && 'Career Path Advisor — 3 personalised paths'}
                </span>
              </div>

              <div className="lp-preview-body">
                {/* ATS Check */}
                {activeTab === 'ats' && (
                  <>
                    <div className="ats-preview-score">
                      <div className="ats-score-ring">
                        <svg viewBox="0 0 120 120" width="120" height="120">
                          <circle cx="60" cy="60" r="52" fill="none" stroke="rgba(255,255,255,0.06)" strokeWidth="8" />
                          <circle cx="60" cy="60" r="52" fill="none" stroke="#6366f1" strokeWidth="8"
                            strokeLinecap="round"
                            strokeDasharray="326.7"
                            strokeDashoffset="49"
                            transform="rotate(-90 60 60)"
                          />
                        </svg>
                        <div className="ats-score-overlay">
                          <span className="ats-score-num">85</span>
                          <span className="ats-score-denom">/100</span>
                        </div>
                      </div>
                    </div>
                    <div className="ats-check-list">
                      <div className="ats-check-row ok"><CheckCircle size={15} /><span>File format friendly</span></div>
                      <div className="ats-check-row ok"><CheckCircle size={15} /><span>Work experience found</span></div>
                      <div className="ats-check-row ok"><CheckCircle size={15} /><span>Contact info parsed</span></div>
                      <div className="ats-check-row warn"><WarnIcon size={15} /><span>No portfolio link</span></div>
                    </div>
                  </>
                )}

                {/* Job Match */}
                {activeTab === 'job-match' && (
                  <>
                    <div className="jm-match-header">
                      <div className="jm-company-logo">S</div>
                      <div className="jm-role-info">
                        <h4>Backend Software Engineer</h4>
                        <p>Stripe · Dublin, IE</p>
                      </div>
                      <div className="jm-match-score">
                        <span className="jm-score-val">92%</span>
                        <span className="jm-score-lbl">Match</span>
                      </div>
                    </div>
                    <div className="jm-kw-section">
                      <h5>✅ Matched Keywords</h5>
                      <div className="jm-tags">
                        <span className="jm-tag match">Java</span>
                        <span className="jm-tag match">Spring Boot</span>
                        <span className="jm-tag match">REST APIs</span>
                        <span className="jm-tag match">SQL</span>
                      </div>
                    </div>
                    <div className="jm-kw-section">
                      <h5>❌ Missing Keywords</h5>
                      <div className="jm-tags">
                        <span className="jm-tag miss">Docker</span>
                        <span className="jm-tag miss">Kubernetes</span>
                        <span className="jm-tag miss">CI/CD</span>
                      </div>
                    </div>
                  </>
                )}

                {/* RAG Coach */}
                {activeTab === 'coach' && (
                  <>
                    <div className="coach-preview-messages">
                      <div className="coach-bubble assistant">
                        <div className="coach-avatar"><Bot size={13} /></div>
                        <div className="coach-msg">
                          I've read your resume. Solid Java and Hibernate experience. What role are we targeting?
                        </div>
                      </div>
                      <div className="coach-bubble user">
                        <div className="coach-msg">How can I make my Spring Boot projects sound more impactful?</div>
                      </div>
                      <div className="coach-bubble assistant">
                        <div className="coach-avatar"><Bot size={13} /></div>
                        <div className="coach-msg">
                          Quantify results! Instead of <em>"wrote backend APIs"</em>, try:{' '}
                          <strong>"Architected 12 Spring Boot endpoints, reducing API response time by 32%."</strong>
                        </div>
                      </div>
                    </div>
                    <div className="coach-preview-input">
                      <span>Ask your AI coach anything…</span>
                      <div className="coach-send-mock"><ArrowRight size={14} /></div>
                    </div>
                  </>
                )}

                {/* Cover Letter */}
                {activeTab === 'cover' && (
                  <div className="cl-preview">
                    <p className="cl-date">May 27, 2026</p>
                    <p className="cl-company">Hiring Team · Stripe Inc.</p>
                    <p className="cl-salutation">Dear Stripe Hiring Team,</p>
                    <p className="cl-body">
                      I am excited to apply for the Backend Software Engineer role at Stripe.
                      With hands-on experience building low-latency, scalable applications in Java and Spring Boot,
                      I am confident in my ability to contribute to Stripe's payment infrastructure.
                      In my previous projects, I architected structured REST APIs handling high-concurrency…
                    </p>
                  </div>
                )}

                {/* Interview */}
                {activeTab === 'interview' && (
                  <div className="interview-preview">
                    {[
                      { q: 'Tell me about a high-traffic system you\'ve worked on.', a: 'In my last project I designed a REST API serving 50K req/day using Spring Boot and connection pooling to keep P95 under 120ms.' },
                      { q: 'How do you handle database performance issues?', a: 'I start with EXPLAIN ANALYZE, add targeted indexes, consider caching layers like Redis, and review N+1 query patterns from Hibernate.' },
                    ].map((item, i) => (
                      <div key={i} className="interview-qa-item">
                        <div className="interview-q">
                          <span className="interview-q-num">Q{i + 1}</span>
                          <span className="interview-q-text">{item.q}</span>
                        </div>
                        <div className="interview-a">{item.a}</div>
                      </div>
                    ))}
                  </div>
                )}

                {/* Career Paths */}
                {activeTab === 'paths' && (
                  <div className="career-paths-preview">
                    {[
                      { label: 'Senior Backend Engineer', sub: 'Close skill gap: Docker, K8s, System Design', cls: 'p1' },
                      { label: 'Solutions Architect', sub: 'Close skill gap: Cloud certifications, stakeholder comms', cls: 'p2' },
                      { label: 'Engineering Manager', sub: 'Close skill gap: People leadership, OKR planning', cls: 'p3' },
                    ].map((p, i) => (
                      <div key={i} className="career-path-item">
                        <div className={`career-path-num ${p.cls}`}>{i + 1}</div>
                        <div className="career-path-info">
                          <h5>{p.label}</h5>
                          <p>{p.sub}</p>
                        </div>
                        <ChevronRight size={16} className="career-path-arrow" />
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* ── How it works ── */}
        <section className="lp-how">
          <div className="lp-container">
            <p className="lp-section-eyebrow">How it works</p>
            <h2 className="lp-section-title">From upload to offer-ready in minutes</h2>
            <p className="lp-section-sub">
              Our Gemini AI and RAG vector pipeline analyses your resume end-to-end
              and surfaces exactly what you need to act on.
            </p>

            <div className="lp-steps">
              {[
                { n: '1', title: 'Upload your resume', desc: 'Drop a PDF or DOCX. We extract text, detect sections, and run ATS scoring in seconds.' },
                { n: '2', title: 'AI analyses & indexes', desc: 'Gemini 1.5 Flash scores your resume. Our RAG pipeline chunks and embeds it in the vector store.' },
                { n: '3', title: 'Pick your AI tool', desc: 'Open the AI Hub — run job matching, generate cover letters, practice interviews, and plan your career.' },
                { n: '4', title: 'Apply with confidence', desc: 'Act on targeted suggestions, re-upload updated versions, and track your ATS score improving.' },
              ].map(step => (
                <div key={step.n} className="lp-step">
                  <div className="lp-step-num">{step.n}</div>
                  <h3>{step.title}</h3>
                  <p>{step.desc}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ── All GenAI Features Grid ── */}
        <section className="lp-ai-features">
          <div className="lp-container">
            <p className="lp-section-eyebrow">AI-powered features</p>
            <h2 className="lp-section-title">Everything you need to land the job</h2>
            <p className="lp-section-sub">
              7 distinct AI tools, all running on Gemini 1.5 Flash with RAG pipelines and
              vector similarity search built on real embeddings.
            </p>

            <div className="lp-features-grid">
              {AI_FEATURES.map(f => (
                <div
                  key={f.id}
                  className="lp-feature-card"
                  data-accent={f.accent}
                  id={`landing-feature-${f.id}`}
                >
                  <div className={`lp-feature-icon ${f.iconClass}`}>
                    {f.icon}
                  </div>
                  <h3>{f.title}</h3>
                  <p>{f.desc}</p>
                  <span className={`lp-feature-tag ${f.tag}`}>
                    <Sparkles size={10} />
                    {f.tagLabel}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ── CTA ── */}
        <section className="lp-cta">
          <div className="lp-container">
            <div className="lp-cta-box">
              <h2>Ready to improve your resume?</h2>
              <p>
                Get instant AI-powered feedback — ATS score, keyword gaps, job matching
                and career coaching. Free to start.
              </p>
              <div className="lp-cta-actions">
                <button
                  className="lp-btn-primary"
                  onClick={handleUploadClick}
                  id="landing-cta-analyze"
                >
                  <Upload size={17} />
                  Analyse My Resume
                </button>
                <button
                  className="lp-btn-secondary"
                  onClick={() => setCurrentView('register')}
                  id="landing-cta-register"
                >
                  Create Free Account
                  <ArrowRight size={16} />
                </button>
              </div>
            </div>
          </div>
        </section>

        {/* ── Footer ── */}
        <footer className="lp-footer">
          <div className="lp-container">
            <p>Made by students, for students 🎓</p>
            <p className="lp-footer-note">ResumeAI · Built with React &amp; Spring Boot · Powered by Gemini AI</p>
          </div>
        </footer>
      </main>
    </div>
  );
}